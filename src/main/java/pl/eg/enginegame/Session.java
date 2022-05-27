package pl.eg.enginegame;


import org.springframework.beans.factory.annotation.Value;
import pl.eg.enginegame.jsons.StartResources;
import pl.eg.enginegame.services.Player;
import pl.eg.enginegame.services.TileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session {
    String sessionUUID;
    int sessionId;
    int currentBotId = 0;
    int winnerID = CONST.GAME_IN_ACTION;
    int turnCouner = 0;

    Map<Integer, Player> bots = new HashMap<Integer, Player>();
    ArrayList<Integer> botsIDs = new ArrayList<>();
    StartResources startResources;

    public TileManager tileManager;

    public Session(int sessionId, int botId) {
        this.sessionId = sessionId;
        this.sessionUUID = String.valueOf( UUID.randomUUID());
        this.currentBotId = botId;

        addBot(botId);
    }

    public int getWinnerID() {
        return winnerID;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setWinnerID(int winnerID) {
        this.winnerID = winnerID;
    }

    public void setStartResources(StartResources sr) {
        this.startResources = sr;
        this.tileManager = new TileManager(sr);
    }
    public StartResources getStartResources() {
        return this.startResources;
    }

    public Map<Integer, Player> getBots() {
        return bots;
    }
    public ArrayList<Integer> getBotsIDs() { return botsIDs; }

    public TileManager getTileManager() {
        return tileManager;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void addBot(int botId) {
        if(!bots.containsKey(botId) && bots.size() < CONST.MAX_BOTS_IN_SESSION) {
            botsIDs.add(botId);
            bots.put(botId, new Player(botId));
        }
    }

    public void removeBot(int botId) {
        if(!bots.containsKey(botId) && bots.size() < CONST.MAX_BOTS_IN_SESSION) {
            bots.remove(botId);
        }
    }

    public void switchCurrentBot() {
        for (int botId : botsIDs) {
            if (botId != currentBotId)  {
                currentBotId = botId;
                return;
            }
        }
    }

    public int getCurrentBotId() {
        return currentBotId;
    }

    private class TemplateJSON {
        public int sessionId;
        public int currentBot = 0;
        public ArrayList<Integer> bots;
        public String map;
        public String resource;
    }

    public void incrementTurnCouner() {
        this.turnCouner++;
    }

    public int getTurnCouner() {
        return turnCouner;
    }

}
