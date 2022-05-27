package pl.eg.enginegame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.eg.enginegame.services.FirebaseService;
import pl.eg.enginegame.services.Player;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ManagerGame {
    private static final Logger LOGGER= LoggerFactory.getLogger(EngineGameApp.class);
    @Autowired
    private static FirebaseService fbs;

    public static int moveUnit(Session session, TurnAction action) throws ExecutionException, InterruptedException {
        int code = MsgError.OK;

        try {
            Player p = session.getBots().get(session.getCurrentBotId());
            p.moveUnit(action.getOperation().getUnitID(), new int[]{action.getOperation().getToX(), action.getOperation().getToY()});
        } catch (Exception ex) {
            fbs.addLog("\n\t:+> ERROR CAUGHT; {}", ex.getMessage());
            return MsgError.CRASH_ERROR_MOVE;
        }

        return code;
    }

    public static int attackUnit(Session session, TurnAction action) throws ExecutionException, InterruptedException {
        int code = MsgError.OK;

        try {
            Player p = session.getBots().get(session.getCurrentBotId());
            p.attack(action.getOperation().getUnitID(), new int[]{action.getOperation().getToX(), action.getOperation().getToY()});
        } catch (Exception ex) {
            fbs.addLog("\n\t:+> ERROR CAUGHT; {}", ex.getMessage());
            return MsgError.CRASH_ERROR_ATTACK;
        }

        return code;
    }

    public static int recruitUnit(Session session, TurnAction action) throws ExecutionException, InterruptedException {
        int code = MsgError.OK;

        try {
            Player p = session.getBots().get(session.getCurrentBotId());
            p.buyUnit(action.getOperation().getUnitName());
        } catch (Exception ex) {
            fbs.addLog("\n\t:+> ERROR CAUGHT; {}", ex.getMessage());
            return MsgError.CRASH_ERROR_RECRUIT;
        }

        return code;
    }

    public static int updateResources(Session session, int error) throws ExecutionException, InterruptedException {
        int code = MsgError.OK;

        if (error != MsgError.OK) {
            return error;
        }

        try {
            Player p = session.getBots().get(session.getCurrentBotId());
            p.prepareToNextRound();
        } catch (Exception ex) {
            fbs.addLog("\n\t:+> ERROR CAUGHT; {}", ex.getMessage());
            return MsgError.CRASH_ERROR_UPDATE_RESOURCES;
        }

        return code;
    }

    public static int checkGameEnd(Session session, int error) throws ExecutionException, InterruptedException {
        int code = MsgError.OK;

        if (error != MsgError.OK) {
            return error;
        }

        try {
            Player p1 = session.getBots().get(session.getBotsIDs().get(0));
            Player p2 = session.getBots().get(session.getBotsIDs().get(1));

            if (p1.getBaseHP() <= 0) {
                session.setWinnerID(p2.getId());
            } else if (p2.getBaseHP() <= 0) {
                session.setWinnerID(p1.getId());
            }

            if (session.getWinnerID() == 0) {
                if (session.getTurnCouner() > CONST.MAX_TURNS) {
                    System.out.println("TURN " + session.getTurnCouner());
                    if (p1.getEntitiesCnt() > p2.getEntitiesCnt()) {
                        session.setWinnerID(p1.getId());
                    } else if (p1.getEntitiesCnt() < p2.getEntitiesCnt()) {
                        session.setWinnerID(p2.getId());
                    } else {
                        session.setWinnerID(p1.getId());
                    }
                }
            }
        } catch (Exception ex) {
            fbs.addLog("\n\t:+> ERROR CAUGHT; {}", ex.getMessage());
            return MsgError.CRASH_ERROR_CHECK_GAME_END;
        }

        if (session.getWinnerID() != CONST.GAME_IN_ACTION) {
            code = MsgError.GAME_IS_OVER;
        }

        return code;
    }
}
