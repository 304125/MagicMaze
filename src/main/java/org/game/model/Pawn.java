package org.game.model;

public class Pawn {
    private int x; // Row position
    private int y; // Column position
    private final Color color;

    public Pawn(int startX, int startY, Color color) {
        this.x = startX;
        this.y = startY;
        this.color = color;
    }

    public Pawn(Pawn pawn){
        this.x = pawn.x;
        this.y = pawn.y;
        this.color = pawn.color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void moveNorth() {
        x--;
    }
    public void moveSouth() {
        x++;
    }
    public void moveEast() {
        y++;
    }
    public void moveWest() {
        y--;
    }

    public Color getColor() {
        return color;
    }
}