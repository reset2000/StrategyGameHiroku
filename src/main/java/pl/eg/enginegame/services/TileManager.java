package pl.eg.enginegame.services;

import pl.eg.enginegame.jsons.BaseMap;
import pl.eg.enginegame.jsons.StartResources;

import java.util.*;

public class TileManager {
    public int[][] terrainArray;
    public Tile[][] coordinates;

    //TODO: ewentualnie do skasowania
    //
    // public Player player1, player2;

    public TileManager(StartResources startResources) {
        this.terrainArray = startResources.map.mapMatrix.clone();
        int row = terrainArray.length;
        int col = terrainArray[0].length;

        this.coordinates = new Tile[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                coordinates[i][j] = new Tile(i, j);
                coordinates[i][j].setType(terrainArray[i][j]);
            }
        }
    }

    public void addUnit(Entity e) {
        int x = e.getX();
        int y = e.getY();
        coordinates[x][y].addEntity(e);
    }

//TODO:ewentualnie do skasowania
//
//    public void setPlayers(Player player1, Player player2) {
//        this.player1 = player1;
//        this.player2 = player2;
//    }

//TODO:ewentualnie do skasowania
//
//    public void removeUnit(int id) {
//        Entity e = null;
//
//        if (player1.getEntity(id) != null) {
//            e = player1.getEntity(id);
//        } else if (player2.getEntity(id) != null) {
//            e = player2.getEntity(id);
//        } else {
//            return;
//        }
//
//        int x = e.getX();
//        int y = e.getY();
//
//        coordinates[x][y].removeEntity(e);
//    }

    public void removeUnit(Entity e) {

        int x = e.getX();
        int y = e.getY();
        System.out.println("ENEMY IS ABOUT TO GET REMOVED");
        coordinates[x][y].removeEntity(e);
    }

    public void moveUnit(Entity e, int[] destination) {

        int x = destination[0];
        int y = destination[1];

        coordinates[e.x][e.y].removeEntity(e);
        coordinates[x][y].addEntity(e);

        e.setX(x);
        e.setY(y);
    }

    int minDistance(int[][] g, Entity e, int[] destination, boolean move) {
        int x = destination[0];
        int y = destination[1];

        boolean isMine = this.coordinates[x][y].getType() == 3;

        int[][] grid = clone(g);
        QItem source = new QItem(e.getX(), e.getY(), 0);

        if (x >= 0 && x < grid.length && y >= 0 && y < grid[0].length) {
            grid[destination[0]][destination[1]] = 10;
        }

        Queue<QItem> queue = new LinkedList<>();

        queue.add(new QItem(source.row, source.col, 0));

        boolean[][] visited = new boolean[grid.length][grid[0].length];
        visited[source.row][source.col] = true;

        while (!queue.isEmpty()) {
            QItem p = queue.remove();

            // Destination found;
            if (grid[p.row][p.col] == 10) {

                return p.dist;
            }

            // up
            if (isValid(p.row - 1, p.col, grid, visited, move, isMine)) {
                queue.add(new QItem(p.row - 1, p.col, p.dist + 1));
                visited[p.row - 1][p.col] = true;
            }

            // down
            if (isValid(p.row + 1, p.col, grid, visited, move, isMine)) {
                queue.add(new QItem(p.row + 1, p.col, p.dist + 1));
                visited[p.row + 1][p.col] = true;
            }

            // left
            if (isValid(p.row, p.col - 1, grid, visited, move, isMine)) {
                queue.add(new QItem(p.row, p.col - 1, p.dist + 1));
                visited[p.row][p.col - 1] = true;
            }

            // right
            if (isValid(p.row, p.col + 1, grid, visited, move, isMine)) {
                queue.add(new QItem(p.row, p.col + 1, p.dist + 1));
                visited[p.row][p.col + 1] = true;
            }
        }

        return -1;
    }

    private boolean isValid(int x, int y, int[][] grid, boolean[][] visited, boolean move, boolean isMine) {

        if (move && isMine) {

            return x >= 0 &&
                    y >= 0 &&
                    x < grid.length &&
                    y < grid[0].length &&
                    grid[x][y] != 2 &&
                    !visited[x][y];
        } else if (move) {

            return x >= 0 &&
                    y >= 0 &&
                    x < grid.length &&
                    y < grid[0].length &&
                    grid[x][y] != 2 &&
                    !visited[x][y] &&
                    this.coordinates[x][y].entities.isEmpty();
        } else {

            return x >= 0 &&
                    y >= 0 &&
                    x < grid.length &&
                    y < grid[0].length &&
                    grid[x][y] != 2 &&
                    !visited[x][y];
        }
    }

    // Print all information about tiles

    public void print() {
        for (Tile[] tile : coordinates) {
            System.out.println();
            for (Tile tile1 : tile) {
                System.out.print(tile1);
                System.out.print("\t");
            }
        }
        System.out.println();
    }

    // Print information about terrain

    public void printMap() {
        for (Tile[] tile : coordinates) {
            System.out.println();
            for (Tile tile1 : tile) {
                switch (tile1.getType()) {
                    case 0:
                        System.out.print('P');
                        break;
                    case 1:
                        System.out.print('L');
                        break;
                    case 2:
                        System.out.print('R');
                        break;
                    case 3:
                        System.out.print('K');
                        break;
                    case 4:
                        System.out.print('B');
                        break;
                }
                System.out.print("\t");
            }
        }
        System.out.println();
    }

    public int[][] getTerrainArray() {
        return terrainArray;
    }

    public Tile[][] getCoordinates() {
        return coordinates;
    }

    int[][] clone(int[][] grid) {
        int row = grid.length;
        int col = grid[0].length;
        int[][] newGrid = new int[row][col];
        for (int i = 0; i < row; i++) {
            System.arraycopy(grid[i], 0, newGrid[i], 0, col);
        }
        return newGrid;
    }

}