package pl.eg.enginegame;

import java.util.HashMap;
import java.util.Map;

public class MsgState {
    public static int OK = 0;
    public static int IS_ERROR = 3001;
    public static int WAIT_FOR_OPPONENT = 3002;
    public static int WAIT_FOR_YOUR_TURN = 3003;

    public static int YOU_WIN = 9001;
    public static int YOU_LOSE = 9002;
    public static int YOU_DRAW = 9003;

    static Map<Integer, String>  states = new HashMap<Integer, String>();
    static {
        states.put(OK, "OK");
        states.put(IS_ERROR, "Error occured.");
        states.put(WAIT_FOR_OPPONENT, "Wait for opponent.");
        states.put(WAIT_FOR_YOUR_TURN, "Wait for your turn.");
        states.put(YOU_WIN, "You win! :-)");
        states.put(YOU_DRAW, "You draw! :-|");
        states.put(YOU_LOSE, "You lose! :-(");
    }

    public static String getMessage(int code) {
        if(states.containsKey(code)) {
            return states.get(code);
        } else {
            return "Unknown state code";
        }
    }
    public static int getStatusCodeByError(int msgError) {
        if(states.containsKey(msgError)) {
            return msgError;
        } else {
            return IS_ERROR;
        }
    }
}
