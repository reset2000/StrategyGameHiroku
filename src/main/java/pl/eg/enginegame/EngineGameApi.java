package pl.eg.enginegame;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import pl.eg.enginegame.services.Entity;
import pl.eg.enginegame.services.FirebaseService;
import pl.eg.enginegame.services.Player;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.eg.enginegame.services.Tile;

@RestController("/api")
public class EngineGameApi {

    static class ResponsePlayerInfo {
        public int id;

        public int[] base;
        public int[] mine;

        public int money;
        public int baseHP;

        public List<Entity> entities;
    }

    static class ResponseGameInfo {
        public int errorCode;
        public int stateCode;
        public String errorMessage;
        public String stateMessage;

        public Tile[][] coordinates;

        public ResponsePlayerInfo currPlayer = new ResponsePlayerInfo();
        public ResponsePlayerInfo enemyPlayer = new ResponsePlayerInfo();
    }

    static class ResponseGameState {
        public int errorCode;
        public int stateCode;
        public String errorMessage;
        public String stateMessage;
    }

    static class ResponseGameSessions {
        public int errorCode;
        public int stateCode;
        public String errorMessage;
        public String stateMessage;

        public List<ManagerSessions.SessionIDs> sessions;
    }

    @Autowired
    ManagerSessions sessionsManager;
    @Autowired
    ManagerGame egm;
    @Autowired
    FirebaseService fbs;
    @Value("${enginegame.sleepBetweenActions:250}")
    public int sleepBetweenActions;
    @Value("${enginegame.maxTurnCounter:250}")
    private int maxTurnCounter;
    private static boolean gameIsStarted = false;
    private static final Logger LOGGER=LoggerFactory.getLogger(EngineGameApp.class);

    @GetMapping("/")
    public String gameServerStarted() throws ExecutionException, InterruptedException {
        if (!gameIsStarted) {
            fbs.addLog("Game Server Started", ":-)");
            gameIsStarted = true;
        }

        return "Game Server Started";
    }

    @RequestMapping("/cfg")
    public String checkConfig() {
        StringBuilder sb = new StringBuilder();

        sb.append("Sleep between actions: ");
        sb.append(sleepBetweenActions);
        sb.append("<br/>");
        sb.append("Sleep between actions: ");
        sb.append(maxTurnCounter);
        sb.append("<br/>");

        return sb.toString();
    }

    @GetMapping("/new-game/{botId}")
    @ResponseBody
    public String newGame(@PathVariable int botId) throws IOException, ExecutionException, InterruptedException {
        String json;
        var error = MsgError.OK;

        Session session = sessionsManager.createNewSession(botId);

        error = checkRegisteredBot(botId, error);
        error = checkSession(session, botId, error);
        error = checkNotExistBot(session, botId, error);

        if (error != MsgError.OK) {
            json = jsonPrepareGameState(error, MsgState.IS_ERROR);
        } else {
            int stateCode = (botId == session.getCurrentBotId()) ? MsgState.OK : MsgState.WAIT_FOR_OPPONENT;
            List<ManagerSessions.SessionIDs> keys = new ArrayList<>();
            keys.add(new ManagerSessions.SessionIDs(session.getSessionId(), session.getSessionUUID()));
            json = jsonPrepareGameSessions(MsgError.OK, stateCode, keys);
        }

        fbs.addLog("\n\t:+> New game(OUT): {}", json);
        return json;
    }

    @GetMapping("/waiting-sessions")
    @ResponseBody
    public String getJoinSessions() throws IOException, ExecutionException, InterruptedException {
        String json;
        var error = MsgError.OK;

        if (error != MsgError.OK) {
            json = jsonPrepareGameState(error, MsgState.IS_ERROR);
        } else {
            List<ManagerSessions.SessionIDs> keys = sessionsManager.getWaitingSessionsIDs();
            json = jsonPrepareGameSessions(MsgError.OK, MsgState.OK, keys);
        }

        fbs.addLog("\n\t:+> Sessions waiting for second player(OUT): {}", json);
        return json;
    }

    @GetMapping("/active-sessions")
    @ResponseBody
    public String getGameSessionsList() throws IOException, ExecutionException, InterruptedException {
        String json;
        var error = MsgError.OK;

        if (error != MsgError.OK) {
            json = jsonPrepareGameState(error, MsgState.IS_ERROR);
        } else {
            List<ManagerSessions.SessionIDs> keys = sessionsManager.getActiveSessionsIDs();
            json = jsonPrepareGameSessions(MsgError.OK, MsgState.OK, keys);
        }

        fbs.addLog("\n\t:+> Active game sessions(OUT): {}", json);
        return json;
    }

    @GetMapping("/join/{botId}/{sessionId}")
    @ResponseBody
    public String joinGame(@PathVariable int botId, @PathVariable int sessionId) throws IOException, ExecutionException, InterruptedException {
        String json;
        var error = MsgError.OK;

        Session session = sessionsManager.getSession(sessionId);

        error = checkRegisteredBot(botId, error);
        error = checkSession(session, botId, error);
        error = checkNotExistBot(session, botId, error);

        if (error == MsgError.OK) {
            if ( session.botsIDs.size() == CONST.MAX_BOTS_IN_SESSION || session.winnerID != CONST.GAME_IN_ACTION ) {
                error = MsgError.INVALID_JOIN;
            }
        }

        if(error == MsgError.OK) {
            session.addBot(botId );
            int currBot = session.getCurrentBotId();
            int joinBot = botId;

            Player p1 = session.getBots().get( currBot );
            Player p2 = session.getBots().get( joinBot );

            p1.setStartResource(session, sessionsManager);
            p2.setStartResource(session, sessionsManager);
        }

        json = jsonPrepareGameState(error, (error != MsgError.OK) ? MsgState.IS_ERROR : MsgState.WAIT_FOR_YOUR_TURN);
        fbs.addLog("\n\t:+> Join to game(OUT): {}", json);
        return json;
    }

    @RequestMapping(value = "/get-session/{botId}/{sessionId}")
    @ResponseBody
    public String getSession(@PathVariable int botId, @PathVariable int sessionId) throws JsonProcessingException, ExecutionException, InterruptedException {
        String json;
        var error = MsgError.OK;

        Session session = sessionsManager.getSession(sessionId);

        error = checkRegisteredBot(botId, error);
        error = checkSession(session, botId, error);
        error = checkExistBot(session, botId, error);

        if (error == MsgError.OK) {
            if ( botId != session.getCurrentBotId() ) {
                error = MsgError.STATUS_WAIT_FOR_YOUR_TURN;
            }
        }

        error = getEndGameStatus(session, error);

        if (error != MsgError.OK) {
            json = jsonPrepareGameState(error, MsgState.getStatusCodeByError(error));
        } else {
            json = jsonPrepareGameInfo(session, MsgError.OK, MsgState.getStatusCodeByError(error));
        }
        fbs.addLog("\n\t:+> Session(OUT): {}", json);
        return json;
    }

    @RequestMapping(value = "/turn/{botId}/{sessionId}", method = RequestMethod.POST)
    @ResponseBody
    public String turn(@PathVariable int botId, @PathVariable int sessionId, @RequestBody String turn) throws JsonProcessingException, ExecutionException, InterruptedException {
        String json;
        int endGameStatus = MsgState.OK;
        var error = MsgError.OK;

        Session session = sessionsManager.getSession(sessionId);

        error = checkRegisteredBot(botId, error);
        error = checkSession(session, botId, error);
        error = checkExistBot(session, botId, error);

        if (error == MsgError.OK) {
            if ( botId != session.getCurrentBotId() ) {
                error = MsgError.STATUS_WAIT_FOR_YOUR_TURN;
            }
        }

        error = getEndGameStatus(session, error);

        if (error == MsgError.OK) {
            TurnAction[] turnActions;

            ObjectMapper mapper = new ObjectMapper();
            turnActions = mapper.readValue(turn, TurnAction[].class);

            error = parseToServices(session, turnActions);
            error = getEndGameStatus(session, error);
            session.switchCurrentBot();
        }

        if (error != MsgError.OK) {
            json = jsonPrepareGameState(error, MsgState.getStatusCodeByError(error));
        } else {
            json = jsonPrepareGameInfo(session, MsgError.OK, MsgState.getStatusCodeByError(error));
            session.tileManager.print();
        }

        fbs.addLog("\n\t:+> Turn(OUT): {}", json);
        return json;
    }

    private int parseToServices(Session session, TurnAction[] turnActions) throws ExecutionException, InterruptedException, JsonProcessingException {
        int errorCode = MsgError.OK;

        errorCode = ManagerGame.updateResources(session, errorCode);

        if (errorCode == MsgError.OK) {
            session.incrementTurnCouner();
            for (TurnAction action : turnActions) {
                switch (action.getAction()) {
                    case CONST.ACTION_PASS:
                        break;
                    case CONST.ACTION_MOVE:
                        errorCode = ManagerGame.moveUnit(session, action);
                        break;
                    case CONST.ACTION_ATTACK:
                        errorCode = ManagerGame.attackUnit(session, action);
                        break;
                    case CONST.ACTION_RECRUIT_UNIT:
                        errorCode = ManagerGame.recruitUnit(session, action);
                        break;
                    default:
                        errorCode = CONST.ERR_WRONG_ACTION;
                }

                fbs.registerSessionActon(session, action, errorCode);
                if (errorCode != MsgError.OK) {
                    break;
                }

                waitBetweenActions(sleepBetweenActions);
            }

            errorCode = ManagerGame.checkGameEnd(session, errorCode);
        }

        return errorCode;
    }


    private int getEndGameStatus(Session session, int errorCode) {
        int statusCode = MsgState.OK;

        if (session.getWinnerID() != CONST.GAME_IN_ACTION) {
            if (session.getWinnerID() == 0) {
                statusCode = MsgState.YOU_DRAW;
            } else if (session.getWinnerID() == session.getCurrentBotId()) {
                statusCode = MsgState.YOU_WIN;
            } else if (session.getWinnerID() != session.getCurrentBotId()) {
                statusCode = MsgState.YOU_LOSE;
            }

            session.removeBot(session.getCurrentBotId());
            if (session.getBotsIDs().size() == 0) {
                sessionsManager.removeSession(session.getSessionId());
            }
        }

        if (errorCode != MsgError.OK) {
            return statusCode != MsgState.OK ? statusCode : errorCode;
        }

        return statusCode;
    }

    private int checkRegisteredBot(int botID,  int prevError) {
        int error = MsgError.OK;

        if (prevError == MsgError.OK) {
            if (!sessionsManager.getStartResources().registeredBots.containsKey(botID)) {
                error = MsgError.INVALID_BOT;
            }
        }

        return error;
    }

    private int checkNotExistBot(Session session, int botID,  int prevError) {
        int error = MsgError.OK;

        if (prevError == MsgError.OK) {
            if (!sessionsManager.getStartResources().registeredBots.containsKey(botID)) {
                error = MsgError.INVALID_BOT;
            } else if (session.getBotsIDs().size() >= CONST.MAX_BOTS_IN_SESSION) {
                error = MsgError.INVALID_BOT;
            }
        }

        return error;
    }


    private int checkExistBot(Session session, int botID,  int prevError) {
        int error = MsgError.OK;

        if (prevError == MsgError.OK) {
            if (!session.getBotsIDs().contains(botID)) {
                error = MsgError.INVALID_BOT;
            }
        }

        return error;
    }

    private int checkSession(Session session, int sessionID,  int prevError) {
        int error = MsgError.OK;

        if (prevError == MsgError.OK) {
            if (session == null){
                error = MsgError.INVALID_SESSION;
            } else if (sessionsManager.getSessions().containsKey(sessionID)) {
                error = MsgError.INVALID_SESSION;
            }
        }

        return error;
    }

    public String jsonPrepareGameInfo(Session session, int errorCode, int stateCode) throws JsonProcessingException {
        int currIdx = 0;
        int enemyIdx = 0;

        if (session.currentBotId == session.getBotsIDs().get(0)) {
            currIdx = 0;
            enemyIdx = 1;
        }
        if (session.currentBotId == session.getBotsIDs().get(1)) {
            currIdx = 1;
            enemyIdx = 0;
        }

        ResponseGameInfo rgs = new ResponseGameInfo();
        Player currPlayer = session.getBots().get( session.getBotsIDs().get(currIdx) );
        Player enemyPlayer = session.getBots().get( session.getBotsIDs().get(enemyIdx) );

        rgs.stateCode = stateCode;
        rgs.errorCode = errorCode;

        rgs.stateMessage = MsgState.getMessage(rgs.stateCode);
        rgs.errorMessage = MsgError.getMessage(rgs.errorCode);

        rgs.coordinates = session.getTileManager().getCoordinates();

        rgs.currPlayer.id = currPlayer.getId();
        rgs.currPlayer.base = currPlayer.getBase();
        rgs.currPlayer.mine = currPlayer.getMine();
        rgs.currPlayer.baseHP = currPlayer.getBaseHP();
        rgs.currPlayer.money = currPlayer.getMoney();
        rgs.currPlayer.entities = currPlayer.getEntities();

        rgs.enemyPlayer.id = enemyPlayer.getId();
        rgs.enemyPlayer.base = enemyPlayer.getBase();
        rgs.enemyPlayer.mine = enemyPlayer.getMine();
        rgs.enemyPlayer.baseHP = enemyPlayer.getBaseHP();
        rgs.enemyPlayer.money = enemyPlayer.getMoney();
        rgs.enemyPlayer.entities = enemyPlayer.getEntities();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(rgs);

        return json;
    }

    private String jsonPrepareGameState(int errorCode, int stateCode) throws JsonProcessingException {
        ResponseGameState rgs = new ResponseGameState();

        rgs.stateCode = stateCode;
        rgs.errorCode = errorCode;

        rgs.stateMessage = MsgState.getMessage(rgs.stateCode);
        rgs.errorMessage = MsgError.getMessage(rgs.errorCode);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(rgs);

        return json;
    }


    private String jsonPrepareGameSessions(int errorCode, int stateCode, List<ManagerSessions.SessionIDs> keys) throws JsonProcessingException {
        ResponseGameSessions rgss = new ResponseGameSessions();

        rgss.stateCode = stateCode;
        rgss.errorCode = errorCode;

        rgss.stateMessage = MsgState.getMessage(rgss.stateCode);
        rgss.errorMessage = MsgError.getMessage(rgss.errorCode);

        rgss.sessions = keys;

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(rgss);

        return json;
    }

    private static void waitBetweenActions(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
}