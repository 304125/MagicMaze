package org.game.model;

public class Coordinate {
    private int x; // Row position
    private int y; // Column position

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
