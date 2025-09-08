package org.game.model;

public class Card {
    private int id;
    private Tile[][] tiles;

    public Card(int id, Tile[][] tiles) {
        this.id = id;
        this.tiles = tiles;
    }

    public int getId() {
        return id;
    }

    public Tile[][] getTiles() {
        return tiles;
    }


    public Card rotate90(){
        // rotate 90 degrees clockwise
        int n = tiles.length;
        Tile[][] rotated = new Tile[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rotated[j][tiles.length - 1 - i] = new Tile(
                        tiles[i][j].getType(),
                        tiles[i][j].getColor(),
                        tiles[i][j].hasWallLeft(), // new up is old left
                        tiles[i][j].hasWallRight(), // new down is old right
                        tiles[i][j].hasWallDown(), // new left is old down
                        tiles[i][j].hasWallUp(), // new right is old up
                        tiles[i][j].getCardId(),
                        tiles[i][j].getEscalator()
                );

            }
        }
        this.tiles = rotated;
        return this;
    }


    public Card rotate180() {
        // rotate 180 degrees
        this.rotate90().rotate90();
        return this;
    }

    public Card rotate270() {
        // rotate 270 degrees clockwise (or 90 degrees counter-clockwise)
        this.rotate90().rotate90().rotate90();
        return this;
    }
}
