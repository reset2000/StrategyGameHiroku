package pl.eg.enginegame.services;

import pl.eg.enginegame.jsons.BaseUnits;

public class Soldier extends Entity {

  Soldier(int xPosition, int yPosition, int player, BaseUnits bu) {
    this.id = count++;
    this.x = xPosition;
    this.y = yPosition;
    this.owner = player;

    this.classType = bu.classType;
    this.health = bu.health;
    this.actionPoints = bu.actionPoints;;
    this.attackPower = bu.attackPower;
  }

}