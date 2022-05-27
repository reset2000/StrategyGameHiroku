package pl.eg.enginegame;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import pl.eg.enginegame.jsons.*;

@Service
public class ManagerSessions {
    static int sessionId = 0;
    Map<Integer, Session> sessions = new HashMap<Integer, Session>();;
    StartResources startResources = new StartResources();

    public static class SessionIDs {
        SessionIDs(Integer id, String uuid) {
            this.id = id;
            this.uuid = uuid;
        }
        public int id;
        public String uuid;
    }

    public ManagerSessions() {
        synchronized (this) {
            loadAllJson();
        }
    }

    public StartResources getStartResources() {
        return startResources;
    }

    public Session createNewSession(int botId) {
        int newSessionID = getNewSessionId();

        Session newSession = new Session(newSessionID, botId);
        newSession.setStartResources(startResources);
        sessions.put(newSessionID, newSession);


        return newSession;
    }
    public Map<Integer, Session> getSessions() {
        return sessions;
    }

    public List<SessionIDs> getActiveSessionsIDs() {
        List<SessionIDs> sessionIDs = new ArrayList<>();

        for (Map.Entry<Integer, Session> s: sessions.entrySet() ) {
            Session v = s.getValue();
            if (v.botsIDs.size() == CONST.MAX_BOTS_IN_SESSION && v.winnerID == CONST.GAME_IN_ACTION) {
                sessionIDs.add(new SessionIDs(s.getKey().intValue(), v.getSessionUUID()));
            }
        }

        return sessionIDs;
    }

    public List<SessionIDs> getWaitingSessionsIDs() {
        List<SessionIDs> sessionIDs = new ArrayList<>();

        for (Map.Entry<Integer, Session> s: sessions.entrySet() ) {
            Session v = s.getValue();
            if (v.botsIDs.size() == 1 && v.winnerID == CONST.GAME_IN_ACTION) {
                sessionIDs.add(new SessionIDs(s.getKey().intValue(), v.getSessionUUID()));
            }
        }

        return sessionIDs;
    }

    public Session getSession(int sessionId) {
        return this.sessions.get(sessionId);
    }
    public void removeSession(int sessionId) {
        sessions.remove(sessionId);
    }

    private int getNewSessionId() {
        return ++sessionId;
    }

    void loadAllJson() {
        try {
            //String Json = objectMapper.writeValueAsString(arr);

            ObjectMapper objectMapper = new ObjectMapper();
            RegisteredBots[] registeredRegisteredBots;
            BaseUnits[] baseUnitsJson;

            InputStream isMap = getClass().getResourceAsStream("/" + "map.json");
            startResources.map = objectMapper.readValue(isMap, BaseMap.class);
            for (int row = 0; row < startResources.map.mapMatrix.length; row++) {
                for (int col = 0; col < startResources.map.mapMatrix[0].length; col++) {

                    if (startResources.map.mapMatrix[row][col] == BaseMap.MINE_SIGNATURE) {
                        startResources.mines.add(new Coordinate(row, col));
                    }

                    if (startResources.map.mapMatrix[row][col] == BaseMap.BASE_SIGNATURE) {
                        startResources.bases.add(new Coordinate(row, col));
                    }
                }
            }

            InputStream isResources = getClass().getResourceAsStream("/" + "resources.json");
            startResources.resources = objectMapper.readValue(isResources, BaseResources.class);

            InputStream isUnits = getClass ().getResourceAsStream("/" + "units.json");
            baseUnitsJson = objectMapper.readValue(isUnits, BaseUnits[].class);
            for (BaseUnits u: baseUnitsJson ) { startResources.units.put(u.name, u); }

            InputStream isRegisteredBots = getClass().getResourceAsStream("/" + "bots.json");
            registeredRegisteredBots = objectMapper.readValue(isRegisteredBots, RegisteredBots[].class);
            for (RegisteredBots rb: registeredRegisteredBots) { startResources.registeredBots.put(rb.botID, rb); }



        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
