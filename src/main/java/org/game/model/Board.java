package org.game.model;

import java.util.List;

public class Board {
    // store tiles as a 2-d array of size 70x70
    private final Tile[][] tiles;
    private final int numRows;
    private final int numCols;
    private List<Pawn> pawns;
    private List<BoardEscalator> escalators = new java.util.ArrayList<>();


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
                int x = (int) (i+ (double) (numRows / 2));
                int y = (int) (j+ (double) (numRows / 2));
                tiles[x][y] = startingTiles[i][j];
                if(startingTiles[i][j].hasEscalator()){
                    updateEscalator(startingTiles[i][j], new Coordinate(x, y));
                }
            }
        }
        printEscalators();
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

    public void addCardToBoard(Card newCard, Coordinate coordinate) {
        System.out.println("Discovered tiles before adding card: " + countDiscoveredTiles());
        Card rotatedCard = newCard; // default no rotation

        // no tile to the right
        if(!isTileAt(coordinate.move(0, 1))) {
            // rotate newTiles 90 degrees clockwise
            rotatedCard = newCard.rotate90();
        }
        // no tile to the left
        else if(!isTileAt(coordinate.move(0, -1))) {
            // rotate newTiles 90 degrees counter-clockwise
            rotatedCard = newCard.rotate270();
        }
        // no tile above
        else if(!isTileAt(coordinate.move(-1, 0))) {
            // do not rotate
        }
        // no tile below
        else if(!isTileAt(coordinate.move(1, 0))) {
            // rotate newTiles 180 degrees
            rotatedCard = newCard.rotate180();
        }

        Coordinate corner = getLeftTopCornerOfNewCard(coordinate);
        Tile[][] rotatedTiles = rotatedCard.getTiles();
        // add the new tiles to the board at the correct position
        for (int i = 0; i < rotatedTiles.length; i++) {
            for (int j = 0; j < rotatedTiles[i].length; j++) {
                tiles[corner.getX() + i][corner.getY() + j] = rotatedTiles[i][j];
                if(rotatedTiles[i][j].hasEscalator()){
                    updateEscalator(rotatedTiles[i][j], new Coordinate(i, j));
                }
            }
        }


        System.out.println("Discovered tiles after adding card: " + countDiscoveredTiles());
    }

    private void updateEscalator(Tile tile, Coordinate position){
        // if escalators does not contain BoardEscalator where hasId equals startingTiles[i][j].getEscalator()
        // if there is no escalator with the same id, add a new one
        if(escalators.stream().noneMatch(e -> e.getId().equals(tile.getEscalator()))){
            escalators.add(new BoardEscalator(position, tile.getEscalator()));
        }
        else{
            // find BoardEscalator with the same id and add the new coordinate to it
            for (BoardEscalator escalator : escalators) {
                if (escalator.getId().equals(tile.getEscalator())) {
                    escalator.setEnd(position);
                    break;
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

    public boolean isTileAt(Coordinate position) {
        if (tiles[position.getX()][position.getY()] != null) {
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

    public Pawn movePawn(Color pawnColor, Action action) {
        Pawn pawn = null;
        for (Pawn p : pawns) {
            if (p.getColor() == pawnColor) {
                pawn = p;
                break;
            }
        }
        Tile currentTile = tiles[pawn.getX()][pawn.getY()];
        boolean moved = false;

        switch (action) {
            case MOVE_NORTH:
                if (!currentTile.hasWallUp() && tiles[pawn.getX() - 1][pawn.getY()] != null && !tiles[pawn.getX() - 1][pawn.getY()].isOccupied()) {
                    pawn.moveNorth();
                    System.out.println("Moved "+ pawnColor +" north");
                    moved = true;
                }
                break;
            case MOVE_SOUTH:
                if( !currentTile.hasWallDown() && tiles[pawn.getX() + 1][pawn.getY()] != null && !tiles[pawn.getX() + 1][pawn.getY()].isOccupied()) {
                    pawn.moveSouth();
                    System.out.println("Moved "+ pawnColor +" south");
                    moved = true;
                }
                break;
            case MOVE_WEST:
                if (!currentTile.hasWallLeft() && tiles[pawn.getX()][pawn.getY() - 1] != null && !tiles[pawn.getX()][pawn.getY() - 1].isOccupied()) {
                    pawn.moveWest();
                    System.out.println("Moved "+ pawnColor +" west");
                    moved = true;
                }
                break;
            case MOVE_EAST:
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
            switch (action) {
                case MOVE_NORTH:
                    tiles[pawn.getX() + 1][pawn.getY()].setOccupied(false);
                    break;
                case MOVE_SOUTH:
                    tiles[pawn.getX() - 1][pawn.getY()].setOccupied(false);
                    break;
                case MOVE_WEST:
                    tiles[pawn.getX()][pawn.getY() + 1].setOccupied(false);
                    break;
                case MOVE_EAST:
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
        if(!isTileAt(position.move(0, 1))){
            return new Coordinate(startX-1, startY+1);
        }
        // no tile to the left
        else if(!isTileAt(position.move(0, -1))){
            return new Coordinate(startX-2, startY-4);
        }
        // no tile above
        else if(!isTileAt(position.move(-1, 0))){
            return new Coordinate(startX-4, startY-1);
        }
        // no tile below
        else if(!isTileAt(position.move(1, 0))){
            return new Coordinate(startX+1, startY-2);
        }
        return null; // should not reach here
    }

    public void printEscalators(){
        for (BoardEscalator escalator : escalators) {
            System.out.println("Escalator ID: " + escalator.getId() + " from " + escalator.getStart().getX() + "," + escalator.getStart().getY() +
                    " to " + (escalator.getEnd() != null ? escalator.getEnd().getX() + "," + escalator.getEnd().getY() : "not set"));
        }
    }

    public List<BoardEscalator> getEscalators() {
        return escalators;
    }

    public Pawn useEscalator(Color pawnColor){
        Pawn pawn = null;
        for (Pawn p : pawns) {
            if (p.getColor() == pawnColor) {
                pawn = p;
                break;
            }
        }
        Tile currentTile = tiles[pawn.getX()][pawn.getY()];

        // find the currentTile in escalators
        for (BoardEscalator escalator : escalators) {
            if (escalator.getId().equals(currentTile.getEscalator())) {
                // the currentTile is either start or end
                Coordinate end = escalator.getEnd();
                Coordinate start = escalator.getStart();

                Coordinate current = new Coordinate(pawn.getX(), pawn.getY());
                Coordinate destination;
                if(current.equals(start)){
                    // move to end
                    destination = escalator.getEnd();

                }
                else if(current.equals(end)){
                    // move to start
                    destination = escalator.getStart();

                }
                else{
                    System.out.println("Error: Pawn is not on the escalator tile");
                    return null;
                }

                System.out.println("Pawn " + pawnColor + " used escalator " + escalator.getId() + " to (" + destination.getX() + "," + destination.getY() + ")");

                // set previous tile not occupied, move pawn to destination, set new tile to occupied
                tiles[pawn.getX()][pawn.getY()].setOccupied(false);
                pawn.moveTo(destination.getX(), destination.getY());
                tiles[pawn.getX()][pawn.getY()].setOccupied(true);

            }
        }
        return pawn;
    }
}
