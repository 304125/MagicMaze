package org.game;

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
}
