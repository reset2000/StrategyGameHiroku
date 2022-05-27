package pl.eg.enginegame;

public interface CONST {
    public static final int MAX_TURNS = 200;

    public static final int ACTION_MOVE = 1;
    public static final int ACTION_ATTACK = 2;
    public static final int ACTION_RECRUIT_UNIT = 3;
    public static final int ACTION_PASS = 0;

    public static final int ERR_WRONG_ACTION = 1001;
    public static final int ERR_WRONG_RECRUIT = 1002;
    public static final int ERR_OUT_OF_BOUNDS = 1003;

    public static final int GAME_IN_ACTION = -1;

    public static final int MAX_BOTS_IN_SESSION = 2;
    public static final String MAP_FILE_ON_SERVER = "map.json";
    public static final String RESOURCES_FILE_ON_SERVER = "resources.base";
}
