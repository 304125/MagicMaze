package org.game;

import java.util.List;

public class Board {
    private List<List<Tile>> tiles;

    public Board(List<List<Tile>> tiles) {
        this.tiles = tiles;
    }

    public List<List<Tile>> getTiles() {
        return tiles;
    }

    public void printBoard() {
        for (List<Tile> row : tiles) {
            for (Tile tile : row) {
                System.out.print(tile.getType() + " ");
            }
            System.out.println();
        }
    }

}
