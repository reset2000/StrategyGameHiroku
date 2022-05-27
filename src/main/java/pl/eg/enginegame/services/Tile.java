package pl.eg.enginegame.services;

import java.util.ArrayList;
import java.util.List;

public class Tile {
  int x, y;
  private int type = 0;
  List<Entity> entities = new ArrayList<>();

  public Tile(int xPosition, int yPosition) {
    x = xPosition;
    y = yPosition;
  }

  public void setType(int type) {

    this.type = type;
  }

  public int getType() {

    return type;
  }

  public void addEntity(Entity entity){

    entities.add(entity);
  }

  public void removeEntity(Entity entity){

    entities.remove(entity);
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public List<Entity> getEntities() {
    return entities;
  }

  @Override
  public String toString(){
    return "{ " + this.type + ", " + this.entities + ", " + this.x + "/" + this.y + "}";
  }


}