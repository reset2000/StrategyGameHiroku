package pl.eg.enginegame.jsons;

import pl.eg.enginegame.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StartResources {
    public Map<Integer, RegisteredBots> registeredBots = new HashMap<Integer, RegisteredBots>();;
    public BaseMap map;
    public BaseResources resources;
    public Map<String, BaseUnits> units  = new HashMap<String, BaseUnits>();

    public ArrayList<Coordinate> bases = new ArrayList<Coordinate>();
    public ArrayList<Coordinate> mines = new ArrayList<Coordinate>();

}
