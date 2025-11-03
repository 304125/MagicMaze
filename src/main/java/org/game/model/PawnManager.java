package org.game.model;

import java.util.List;

public class PawnManager {
    private static Board board;

    public PawnManager(Board board) {
        PawnManager.board = board;
    }

    public Pawn useVortex(Color pawnColor, int vortexNumber){
        Pawn pawn = board.getPawnByColor(pawnColor);
        List<BoardVortex> vortexList = board.getVortexListByColor(pawnColor);

        // find the vortex with the given cardId
        for (BoardVortex vortex : vortexList) {
            if (vortex.getCardId() == vortexNumber) {
                Coordinate destination = vortex.getPosition();
                // check if the destination is occupied
                if(board.getTileAt(destination.getX(), destination.getY()).isOccupied()){
                    System.out.println("Error: Vortex destination is occupied");
                    return pawn;
                }
                System.out.println("Pawn " + pawnColor + " used vortex " + vortexNumber + " to (" + destination.getX() + "," + destination.getY() + ")");

                // set previous tile not occupied, move pawn to destination, set new tile to occupied
                board.getTileAt(pawn.getX(), pawn.getY()).setOccupied(false);
                pawn.moveTo(destination.getX(), destination.getY());
                board.getTileAt(pawn.getX(), pawn.getY()).setOccupied(true);
                return pawn;
            }
        }
        return pawn;
    }

    public Pawn useEscalator(Color pawnColor){
        Pawn pawn = board.getPawnByColor(pawnColor);
        Tile currentTile = board.getTileAt(pawn.getX(), pawn.getY());

        Coordinate destination = getOtherSideOfEscalator(new Coordinate(pawn.getX(), pawn.getY()));
        if(destination == null){
            System.out.println("Error: No escalator found at current position");
            return pawn;
        }
        if(board.getTileAt(destination.getX(), destination.getY()).isOccupied()){
            System.out.println("Error: Escalator destination is occupied");
            return pawn;
        }
        else{
            System.out.println("Pawn " + pawnColor + " used an escalator to (" + destination.getX() + "," + destination.getY() + ")");

            // set previous tile not occupied, move pawn to destination, set new tile to occupied
            board.getTileAt(pawn.getX(), pawn.getY()).setOccupied(false);
            pawn.moveTo(destination.getX(), destination.getY());
            board.getTileAt(pawn.getX(), pawn.getY()).setOccupied(true);
        }
        return pawn;
    }

    public static Coordinate getOtherSideOfEscalator(Coordinate currentCoordinate){
        Tile currentTile = board.getTileAt(currentCoordinate.getX(), currentCoordinate.getY());

        // find the currentTile in escalators
        for (BoardEscalator escalator : board.getEscalators()) {
            if (escalator.getId().equals(currentTile.getEscalator())) {
                // the currentTile is either start or end
                Coordinate end = escalator.getEnd();
                Coordinate start = escalator.getStart();

                if(currentCoordinate.equals(start)){
                    // move to end
                    return escalator.getEnd();
                }
                else if(currentCoordinate.equals(end)){
                    // move to start
                    return escalator.getStart();
                }
                else{
                    System.out.println("Error: Coordinate is not an escalator tile");
                    return null;
                }
            }
        }
        // should not get here
        return null;
    }

    public Pawn movePawn(Color pawnColor, Action action) {
        Pawn pawn = board.getPawnByColor(pawnColor);
        Tile currentTile = board.getTileAt(pawn.getX(), pawn.getY());
        boolean moved = false;

        switch (action) {
            case MOVE_NORTH:
                if (!currentTile.hasWallUp() && board.getTileAt(pawn.getX() - 1, pawn.getY()) != null && !board.getTileAt(pawn.getX() - 1, pawn.getY()).isOccupied()) {
                    pawn.moveNorth();
                    System.out.println("Moved "+ pawnColor +" north");
                    moved = true;
                }
                break;
            case MOVE_SOUTH:
                if( !currentTile.hasWallDown() && board.getTileAt(pawn.getX() + 1, pawn.getY()) != null && !board.getTileAt(pawn.getX() + 1, pawn.getY()).isOccupied()) {
                    pawn.moveSouth();
                    System.out.println("Moved "+ pawnColor +" south");
                    moved = true;
                }
                break;
            case MOVE_WEST:
                if (!currentTile.hasWallLeft() && board.getTileAt(pawn.getX(), pawn.getY() - 1) != null && !board.getTileAt(pawn.getX(), pawn.getY() - 1).isOccupied()) {
                    pawn.moveWest();
                    System.out.println("Moved "+ pawnColor +" west");
                    moved = true;
                }
                break;
            case MOVE_EAST:
                if( !currentTile.hasWallRight() && board.getTileAt(pawn.getX(), pawn.getY() + 1) != null && !board.getTileAt(pawn.getX(), pawn.getY() + 1).isOccupied()) {
                    pawn.moveEast();
                    System.out.println("Moved "+ pawnColor +" east");
                    moved = true;
                }
                break;
        }

        //update occupied
        if(moved){
            board.getTileAt(pawn.getX(), pawn.getY()).setOccupied(true);
            // set the previous tile to not occupied
            switch (action) {
                case MOVE_NORTH:
                    board.getTileAt(pawn.getX() + 1, pawn.getY()).setOccupied(false);
                    break;
                case MOVE_SOUTH:
                    board.getTileAt(pawn.getX() - 1, pawn.getY()).setOccupied(false);
                    break;
                case MOVE_WEST:
                    board.getTileAt(pawn.getX(), pawn.getY() + 1).setOccupied(false);
                    break;
                case MOVE_EAST:
                    board.getTileAt(pawn.getX(), pawn.getY() - 1).setOccupied(false);
                    break;
            }
        }

        // check if the pawn has moved onto a Timer tile
        Tile newTile = board.getTileAt(pawn.getX(), pawn.getY());
        if(moved && newTile.getType() == TileType.TIMER && !newTile.isUsed()){
            System.out.println("Pawn " + pawnColor + " landed on a Timer tile!");
            board.getTimer().flipTimer();
            newTile.setUsed(true);
        }

        return pawn;
    }
}
