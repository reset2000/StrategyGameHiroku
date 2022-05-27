package pl.eg.enginegame;

import java.util.HashMap;
import java.util.Map;

public class MsgError {
    public static int OK = 0;
    public static int INVALID_SESSION = 1001;
    public static int INVALID_BOT = 1002;
    public static int INVALID_JOIN = 1003;

    public static int STATUS_WAIT_FOR_OPPONENT = 3002;
    public static int STATUS_WAIT_FOR_YOUR_TURN = 3003;

    public static int CRASH_ERROR_ATTACK = 8001;
    public static int CRASH_ERROR_MOVE = 8002;
    public static int CRASH_ERROR_RECRUIT = 8003;
    public static int CRASH_ERROR_UPDATE_RESOURCES = 8004;
    public static int CRASH_ERROR_CHECK_GAME_END = 8005;

    public static int GAME_IS_OVER = 9000;

    static Map<Integer, String>  errors = new HashMap<Integer, String>();
    static {
        errors.put(OK, "OK");
        errors.put(INVALID_BOT, "Invalid bot ID.");
        errors.put(INVALID_SESSION, "Invalid session ID.");
        errors.put(INVALID_JOIN, "Invalid join to session.");
        errors.put(CRASH_ERROR_ATTACK, "Critical error while executing bot attack action.");
        errors.put(CRASH_ERROR_MOVE, "Critical error while executing bot move action.");
        errors.put(CRASH_ERROR_RECRUIT, "Critical error while executing bot recruit action.");

        errors.put(CRASH_ERROR_UPDATE_RESOURCES, "Critical error while updating bot resources.");
        errors.put(CRASH_ERROR_CHECK_GAME_END, "Critical error while checking for game end conditions.");
    }

    public static String getMessage(int code) {
        if(errors.containsKey(code)) {
            return errors.get(code);
        } else {
            return "Unknown error code";
        }
    }
}
