package org.game;

import java.util.List;

public class Board {
    // store tiles as a 2-d array of size 70x70
    private Tile[][] tiles;
    private final int numRows;
    private final int numCols;
    private List<Pawn> pawns;

    public Board(int maxSize) {
        numRows = maxSize;
        numCols = maxSize;
        this.tiles = new Tile[numRows][numCols];
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public void initializeStartingTile(Card startingCard) {
        Tile[][] startingTiles = startingCard.getTiles();

        // place the tile startingTiles at 0,0 to 34,37 in tiles
        for (int i = 0; i < startingTiles.length; i++) {
            for (int j = 0; j < startingTiles[i].length; j++) {
                tiles[(int) (i+ (double) (numRows / 2))][(int) (j+ (double) (numRows / 2))] = startingTiles[i][j];
            }
        }
    }

    public void initializeStartingPawns(List<Pawn> initialPawns) {
        this.pawns = initialPawns;
        // for each pawn, set their position at occupied in the corresponding tile
        for (Pawn pawn : initialPawns) {
            int x = pawn.getX();
            int y = pawn.getY();
            tiles[x][y].setOccupied(true);
        }
    }

    public void addCardToBoard(Card newCard, int startX, int startY) {
        Tile[][] newTiles = newCard.getTiles();

        // no tile to the right
        if(!isTileAt(startX+1, startY)) {
            // rotate newTiles 90 degrees clockwise
            Tile[][] rotatedTiles = new Tile[newTiles[0].length][newTiles.length];
            for (int i = 0; i < newTiles.length; i++) {
                for (int j = 0; j < newTiles[i].length; j++) {
                    rotatedTiles[j][newTiles.length - 1 - i] = newTiles[i][j];
                }
            }

            // add the rotated tile to the board starting from startX+1, startY+1
            for (int i = 0; i < rotatedTiles.length; i++) {
                for (int j = 0; j < rotatedTiles[i].length; j++) {
                    tiles[startX + 1 + i][startY + 1 + j] = rotatedTiles[i][j];
                }
            }
        }
        // no tile to the left
        else if(!isTileAt(startX-1, startY)) {
            // rotate newTiles 90 degrees counter-clockwise
            Tile[][] rotatedTiles = new Tile[newTiles[0].length][newTiles.length];
            for (int i = 0; i < newTiles.length; i++) {
                for (int j = 0; j < newTiles[i].length; j++) {
                    rotatedTiles[newTiles[0].length - 1 - j][i] = newTiles[i][j];
                }
            }
            // add the rotated tile to the board starting from startX-4, startY+2
            for (int i = 0; i < rotatedTiles.length; i++) {
                for (int j = 0; j < rotatedTiles[i].length; j++) {
                    tiles[startX - 4 + i][startY + 2 + j] = rotatedTiles[i][j];
                }
            }
        }
        // no tile above
        else if(!isTileAt(startX, startY-1)) {
            // do not rotate
            // add the tile to the board starting from startX-2, startY+4
            for (int i = 0; i < newTiles.length; i++) {
                for (int j = 0; j < newTiles[i].length; j++) {
                    tiles[startX - 2 + i][startY + 4 + j] = newTiles[i][j];
                }
            }
        }
        // no tile below
        else if(!isTileAt(startX, startY+1)) {
            // rotate newTiles 180 degrees
            Tile[][] rotatedTiles = new Tile[newTiles.length][newTiles[0].length];
            for (int i = 0; i < newTiles.length; i++) {
                for (int j = 0; j < newTiles[i].length; j++) {
                    rotatedTiles[newTiles.length - 1 - i][newTiles[0].length - 1 - j] = newTiles[i][j];
                }
            }
            // add the rotated tile to the board starting from startX-2, startY-1
            for (int i = 0; i < rotatedTiles.length; i++) {
                for (int j = 0; j < rotatedTiles[i].length; j++) {
                    tiles[startX - 2 + i][startY - 1 + j] = rotatedTiles[i][j];
                }
            }
        }
    }

    public void printBoard() {
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                System.out.print(tile.getType() + " ");
            }
            System.out.println();
        }
    }

    public boolean isTileAt(int x, int y) {
        if (tiles[x][y] != null) {
            return true;
        } else {
            return false;
        }
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }

    public Pawn movePawn(Color pawnColor, String direction) {
        Pawn pawn = null;
        for (Pawn p : pawns) {
            if (p.getColor() == pawnColor) {
                pawn = p;
                break;
            }
        }
        Tile currentTile = tiles[pawn.getX()][pawn.getY()];
        boolean moved = false;

        switch (direction.toLowerCase()) {
            case "n": // Move north
                if (!currentTile.hasWallUp() && tiles[pawn.getX() - 1][pawn.getY()] != null && !tiles[pawn.getX() - 1][pawn.getY()].isOccupied()) {
                    pawn.moveNorth();
                    System.out.println("Moved "+ pawnColor +" north");
                    moved = true;
                }
                break;
            case "s": // Move south
                if( !currentTile.hasWallDown() && tiles[pawn.getX() + 1][pawn.getY()] != null && !tiles[pawn.getX() + 1][pawn.getY()].isOccupied()) {
                    pawn.moveSouth();
                    System.out.println("Moved "+ pawnColor +" south");
                    moved = true;
                }
                break;
            case "w": // Move west
                if (!currentTile.hasWallLeft() && tiles[pawn.getX()][pawn.getY() - 1] != null && !tiles[pawn.getX()][pawn.getY() - 1].isOccupied()) {
                    pawn.moveWest();
                    System.out.println("Moved "+ pawnColor +" west");
                    moved = true;
                }
                break;
            case "e": // Move east
                if( !currentTile.hasWallRight() && tiles[pawn.getX()][pawn.getY() + 1] != null && !tiles[pawn.getX()][pawn.getY() + 1].isOccupied()) {
                    pawn.moveEast();
                    System.out.println("Moved "+ pawnColor +" east");
                    moved = true;
                }
                break;
        }

        //update occupied
        if(moved){
            tiles[pawn.getX()][pawn.getY()].setOccupied(true);
            // set the previous tile to not occupied
            switch (direction.toLowerCase()) {
                case "n":
                    tiles[pawn.getX() + 1][pawn.getY()].setOccupied(false);
                    break;
                case "s":
                    tiles[pawn.getX() - 1][pawn.getY()].setOccupied(false);
                    break;
                case "w":
                    tiles[pawn.getX()][pawn.getY() + 1].setOccupied(false);
                    break;
                case "e":
                    tiles[pawn.getX()][pawn.getY() - 1].setOccupied(false);
                    break;
            }
        }

        return pawn;
    }

    public Pawn getPawnByColor(Color color) {
        for (Pawn pawn : pawns) {
            if (pawn.getColor() == color) {
                return pawn;
            }
        }
        return null;
    }

    public void printAllPawns(){
        for (Pawn pawn : pawns) {
            System.out.println("Pawn color: " + pawn.getColor() + " at position (" + pawn.getX() + ", " + pawn.getY() + ")");
        }
    }
}
