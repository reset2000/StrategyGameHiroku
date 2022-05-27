package pl.eg.enginegame.services;

import pl.eg.enginegame.ManagerSessions;
import pl.eg.enginegame.Session;
import pl.eg.enginegame.jsons.BaseMap;
import pl.eg.enginegame.jsons.BaseResources;
import pl.eg.enginegame.jsons.BaseUnits;
import pl.eg.enginegame.jsons.StartResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
  StartResources startResources;

  private static int count = 0;

  int id;

  int[] base;
  int[] mine;

  int money;
  int baseHP;

  Player enemy;

  TileManager tileManager;
  List<Entity> entities = new ArrayList<>();

  // SETTERS AND GETTERS

  public void setBase(int[] base) {
    this.base = base;
  }
  public void setMine(int[] mine) {
    this.mine = mine;
  }

  public int getMoney() {
    return money;
  }

  public int getBaseHP() {
    return baseHP;
  }

  public void setFromJson(int money) {
    this.money = money;
  }

  public void setMoney(int money) {
    this.money = money;
  }

  public void setBaseHP(int baseHP) {
    this.baseHP = baseHP;
  }

  public Entity getEntity(int id){
    return getEntityById(id);
  }

  public void setEnemy(Player player){
    this.enemy = player;
  }

  public int getEntitiesCnt() {
    return this.entities.size();
  }
  public int getEnemyEntitiesCnt() {
    return enemy.entities.size();
  }

  public int buyUnit(String type){
    if (tileManager.coordinates[base[0]][base[1]].entities.isEmpty()){
      if (type.equals("soldier") && this.money >= 250){
        Soldier e = new Soldier(this.base[0], this.base[1], this.id, startResources.units.get(type));
        this.entities.add(e);
        tileManager.addUnit(e);
        this.money -= 250;
        return 0;
      }
      return -2;
    } else {
      return -1;
    }
  }

  private int killUnit(int id){
    Entity e = getEnemyEntityById(id);
    System.out.println("ENEMY IS ABOUT TO GET KILLED");

    System.out.println(this.entities);

    if (e != null) {
      tileManager.removeUnit(e);
      this.entities.remove(e);
    }

    return 0;
  }

  public int moveUnit(int id, int[] destination){

    Entity e = getEntityById(id);
    int distance = tileManager.minDistance(tileManager.terrainArray, e, destination, true);
    if (distance > e.actionPoints || distance < 0){
      return -1;
    }
    e.actionPoints -= distance;
    tileManager.moveUnit(e, destination);
    return distance;
  }

  public int attack(int id, int[] destination){

    int x = destination[0];
    int y = destination[1];

    Entity e = getEntityById(id);

    int distance = tileManager.minDistance(tileManager.terrainArray, e, destination, false);

    if (e.actionPoints < 1){
      return -1;
    }

    if (distance > 1 || distance < 0){
      return -1;
    }

    if (!tileManager.coordinates[x][y].entities.isEmpty()){
      Entity enemy = tileManager.coordinates[x][y].entities.get(0);

      if (enemy.owner == this.id){
        return 1;
      }

      // SUB HP
      enemy.health -= e.attackPower;

      if (enemy.health <= 0){
        System.out.println("ENEMY HAS LESS THAN 0 HP");
        killUnit(enemy.id);
        if (destination[0] != this.enemy.base[0] && destination[1] != this.enemy.base[1] && this.enemy.baseHP > 0){
          moveUnit(id, destination);
        }
        return 0;
      }
      e.actionPoints--;
      return 0;

    } else if (tileManager.coordinates[x][y].getType() == 4){
      this.enemy.baseHP -= e.attackPower;

      if (this.enemy.baseHP <= 0){
        System.out.println("Wygrywa gracz z id=" + this.id);
        tileManager.coordinates[x][y].setType(-1);
      }
    }

    return 1;
  }

  // NEXT ROUND PREPARATION

  private void healUnits(){
    for (Entity e : this.entities){
      if(e.getHealth() < 100){
        e.health += 10;
      }
    }
  }

  private void getMineMoney(){
    int x = mine[0];
    int y = mine[1];
    for(Entity ignored : tileManager.coordinates[x][y].entities){
      this.money += 200;
    }
  }

  private void resetMovePoints(){
    for (Entity e : this.entities){
      e.actionPoints = 5;
    }
  }

  public void prepareToNextRound(){
    getMineMoney();
    resetMovePoints();
    healUnits();
  }

  // HELPER

  Entity getEntityById(int id){
    for (Entity e : this.entities){
      if (e.getId() == id){
        return e;
      }
    }
    return null;
  }

  Entity getEnemyEntityById(int id){
    for (Entity e : this.enemy.entities){
      if (e.getId() == id){
        return e;
      }
    }
    return null;
  }


  public Player(int botId){
      this.id = botId;
  }

  public int getId() {
    return id;
  }

  public int[] getBase() {
    return base;
  }

  public int[] getMine() {
    return mine;
  }

  public Player getEnemy() {
    return enemy;
  }

  public List<Entity> getEntities() {
    return entities;
  }

  public void setStartResource(Session session, ManagerSessions ms) {
    this.startResources = ms.getStartResources();
    this.tileManager = session.getTileManager();
    int myIdx = 0;
    int enemyIdx = 0;

    if (this.id == session.getBotsIDs().get(0)) {
      myIdx = 0;
      enemyIdx = 1;
    }
    if (this.id == session.getBotsIDs().get(1)) {
      myIdx = 1;
      enemyIdx = 0;
    }

//TODO: do skasowania
//
//    this.tileManager.setPlayer1(session.getBots().get(session.getBotsIDs().get(myIdx)));
//    this.tileManager.setPlayer2(session.getBots().get(session.getBotsIDs().get(enemyIdx)));

    this.setEnemy(session.getBots().get(session.getBotsIDs().get(enemyIdx)));
    this.base = new int[]{ this.startResources.bases.get(myIdx).row, this.startResources.bases.get(myIdx).col};
    this.mine = new int[]{ this.startResources.mines.get(myIdx).row, this.startResources.mines.get(myIdx).col};

    this.setMoney( this.startResources.resources.money );
    this.setBaseHP( this.startResources.resources.baseHP );

    //this.buyUnit("soldier");
  }
}