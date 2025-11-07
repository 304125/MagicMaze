package org.game.model;

public class Pawn {
    private Coordinate coordinate;
    private final Color color;

    public Pawn(Coordinate coordinate, Color color) {
        this.coordinate = coordinate;
        this.color = color;
    }

    public Pawn(Pawn pawn){
        this.coordinate = pawn.getCoordinate();
        this.color = pawn.color;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void moveNorth() {
        coordinate = coordinate.move(-1, 0);
    }
    public void moveSouth() {
        coordinate = coordinate.move(1, 0);
    }
    public void moveEast() {
        coordinate = coordinate.move(0, 1);
    }
    public void moveWest() {
        coordinate = coordinate.move(0, -1);
    }

    public void moveTo(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Color getColor() {
        return color;
    }
}