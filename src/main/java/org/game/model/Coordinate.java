package org.game.model;

/**
 * @param x Row coordinate
 * @param y Column coordinate
 */
public record Coordinate(int x, int y) {

    public Coordinate move(int x, int y) {
        return new Coordinate(this.x + x, this.y + y);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
