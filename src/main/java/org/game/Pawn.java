package org.game;

import java.util.List;

public class Pawn {
    private int x; // Row position
    private int y; // Column position
    private Color color;

    public Pawn(int startX, int startY, Color color) {
        this.x = startX;
        this.y = startY;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(String direction, Board board) {
        Tile currentTile = board.getTiles().get(x).get(y);

        switch (direction.toLowerCase()) {
            case "n": // Move north
                if (!currentTile.hasWallUp() && x > 0) {
                    x--;
                }
                break;
            case "s": // Move south
                if (!currentTile.hasWallDown() && x < board.getTiles().size() - 1) {
                    x++;
                }
                break;
            case "w": // Move west
                if (!currentTile.hasWallLeft() && y > 0) {
                    y--;
                }
                break;
            case "e": // Move east
                if (!currentTile.hasWallRight() && y < board.getTiles().get(0).size() - 1) {
                    y++;
                }
                break;
        }
        System.out.println("Pawn moved to (" + x + ", " + y + ")");
    }

    public Color getColor() {
        return color;
    }
}