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
        System.out.println("Discovered tiles before adding card: " + countDiscoveredTiles());
        Card rotatedCard = newCard; // default no rotation

        // no tile to the right
        if(!isTileAt(startX, startY+1)) {
            // rotate newTiles 90 degrees clockwise
            rotatedCard = newCard.rotate90();
        }
        // no tile to the left
        else if(!isTileAt(startX, startY-1)) {
            // rotate newTiles 90 degrees counter-clockwise
            rotatedCard = newCard.rotate270();
        }
        // no tile above
        else if(!isTileAt(startX-1, startY)) {
            // do not rotate
        }
        // no tile below
        else if(!isTileAt(startX+1, startY)) {
            // rotate newTiles 180 degrees
            rotatedCard = newCard.rotate180();
        }

        Coordinate corner = getLeftTopCornerOfNewCard(new Coordinate(startX, startY));
        Tile[][] rotatedTiles = rotatedCard.getTiles();
        // add the new tiles to the board at the correct position
        for (int i = 0; i < rotatedTiles.length; i++) {
            for (int j = 0; j < rotatedTiles[i].length; j++) {
                tiles[corner.getX() + i][corner.getY() + j] = rotatedTiles[i][j];
            }
        }

        System.out.println("Discovered tiles after adding card: " + countDiscoveredTiles());
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

    public Tile getTileAt(int x, int y) {
        if (tiles[x][y] != null) {
            return tiles[x][y];
        } else {
            return null;
        }
    }

    public int countDiscoveredTiles() {
        int count = 0;
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public Coordinate getLeftTopCornerOfNewCard(Coordinate position) {
        int startX = position.getX();
        int startY = position.getY();

        // no tile to the right
        if(!isTileAt(startX, startY+1)){
            return new Coordinate(startX-1, startY+1);
        }
        // no tile to the left
        else if(!isTileAt(startX, startY-1)){
            return new Coordinate(startX-2, startY-4);
        }
        // no tile above
        else if(!isTileAt(startX-1, startY)){
            return new Coordinate(startX-4, startY-1);
        }
        // no tile below
        else if(!isTileAt(startX+1, startY)){
            return new Coordinate(startX+1, startY-2);
        }
        return null; // should not reach here
    }
}
