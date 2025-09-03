package org.game;

import java.util.List;

public class Pawn {
    private int x; // Row position
    private int y; // Column position

    public Pawn(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(String direction, List<List<Tile>> board) {
        Tile currentTile = board.get(x).get(y);

        switch (direction.toLowerCase()) {
            case "n": // Move north
                if (!currentTile.hasWallUp() && x > 0) {
                    x--;
                }
                break;
            case "s": // Move south
                if (!currentTile.hasWallDown() && x < board.size() - 1) {
                    x++;
                }
                break;
            case "w": // Move west
                if (!currentTile.hasWallLeft() && y > 0) {
                    y--;
                }
                break;
            case "e": // Move east
                if (!currentTile.hasWallRight() && y < board.get(0).size() - 1) {
                    y++;
                }
                break;
        }
    }
}