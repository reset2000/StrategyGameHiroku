package pl.eg.enginegame.services;

public class Entity {

  static int count = 0;
  int id = 0;
  int owner;

  int classType;
  int x, y;

  int health;
  int actionPoints;
  int attackPower;

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void setX(int x){
    this.x = x;
  }

  public void setY(int y){
    this.y = y;
  }

  public int getId() {
    return id;
  }

  public int getOwner() {
    return owner;
  }

  public int getClassType() {
    return classType;
  }

  public int getHealth() {
    return health;
  }

  public int getActionPoints() {
    return actionPoints;
  }

  public int getAttackPower() {
    return attackPower;
  }

  @Override
  public String toString() {
    if (this.classType == 0){
      return "S" + "(" + this.owner + ", id=" + this.id + ")";
    } else {
      return "None";
    }
  }
}