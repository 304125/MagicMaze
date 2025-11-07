package org.game.model;

/**
 * @param x Row position
 * @param y Column position
 */
public record Coordinate(int x, int y) {

    public Coordinate move(int x, int y) {
        return new Coordinate(this.x + x, this.y + y);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
