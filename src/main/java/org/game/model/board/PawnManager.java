package org.game.model.board;

import org.game.model.*;
import org.game.model.AI.StateChangeListener;
import org.game.utils.Config;

import java.util.ArrayList;
import java.util.List;

public class PawnManager {
    private static Board board;
    private final List<StateChangeListener> listeners = new ArrayList<>();

    public PawnManager(Board board) {
        PawnManager.board = board;
    }

    public void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    public Pawn useVortex(Color pawnColor, int vortexNumber){
        Pawn pawn = board.getPawnByColor(pawnColor);
        List<BoardVortex> vortexList = board.getVortexListByColor(pawnColor);

        // find the vortex with the given cardId
        for (BoardVortex vortex : vortexList) {
            if (vortex.cardId() == vortexNumber) {
                Coordinate destination = vortex.position();
                // check if the destination is occupied
                if(board.getTileAt(new Coordinate(destination.x(), destination.y())).isOccupied()){
                    System.out.println("Error: Vortex destination is occupied");
                    return pawn;
                }
                if(Config.PRINT_EVERYTHING){
                    System.out.println("Pawn " + pawnColor + " used vortex " + vortexNumber + " to (" + destination.x() + "," + destination.y() + ")");
                }

                // set previous tile not occupied, move pawn to destination, set new tile to occupied
                board.getTileAt(pawn.getCoordinate()).setOccupied(false);
                pawn.moveTo(new Coordinate(destination.x(), destination.y()));
                board.getTileAt(pawn.getCoordinate()).setOccupied(true);

                updateLastMovedPawn(pawn, Action.VORTEX);

                return pawn;
            }
        }
        return pawn;
    }

    public Pawn useEscalator(Color pawnColor){
        Pawn pawn = board.getPawnByColor(pawnColor);
        Tile currentTile = board.getTileAt(pawn.getCoordinate());

        Coordinate destination = getOtherSideOfEscalator(pawn.getCoordinate());
        if(destination == null){
            System.out.println("Error: No escalator found at current position");
            return pawn;
        }
        if(board.getTileAt(new Coordinate(destination.x(), destination.y())).isOccupied()){
            System.out.println("Error: Escalator destination is occupied");
            return pawn;
        }
        else{
            if(Config.PRINT_EVERYTHING) {
                System.out.println("Pawn " + pawnColor + " used an escalator to (" + destination.x() + "," + destination.y() + ")");
            }

            // set previous tile not occupied, move pawn to destination, set new tile to occupied
            board.getTileAt(pawn.getCoordinate()).setOccupied(false);
            pawn.moveTo(new Coordinate(destination.x(), destination.y()));
            board.getTileAt(pawn.getCoordinate()).setOccupied(true);

            updateLastMovedPawn(pawn, Action.ESCALATOR);
        }
        return pawn;
    }

    public static Coordinate getOtherSideOfEscalator(Coordinate currentCoordinate){
        Tile currentTile = board.getTileAt(new Coordinate(currentCoordinate.x(), currentCoordinate.y()));

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
        Tile currentTile = board.getTileAt(pawn.getCoordinate());
        boolean moved = false;

        switch (action) {
            case MOVE_NORTH:
                if (!currentTile.hasWallUp() && board.getTileAt(pawn.getCoordinate().move(-1, 0)) != null && !board.getTileAt(pawn.getCoordinate().move(-1, 0)).isOccupied()) {
                    pawn.moveNorth();
                    if(Config.PRINT_EVERYTHING){
                        System.out.println("Moved "+ pawnColor +" north");
                    }

                    moved = true;
                }
                break;
            case MOVE_SOUTH:
                if( !currentTile.hasWallDown() && board.getTileAt(pawn.getCoordinate().move(1, 0)) != null && !board.getTileAt(pawn.getCoordinate().move(1, 0)).isOccupied()) {
                    pawn.moveSouth();
                    if(Config.PRINT_EVERYTHING){
                        System.out.println("Moved "+ pawnColor +" south");
                    }
                    moved = true;
                }
                break;
            case MOVE_WEST:
                if (!currentTile.hasWallLeft() && board.getTileAt(pawn.getCoordinate().move(0, -1)) != null && !board.getTileAt(pawn.getCoordinate().move(0, -1)).isOccupied()) {
                    pawn.moveWest();
                    if(Config.PRINT_EVERYTHING){
                        System.out.println("Moved "+ pawnColor +" west");
                    }
                    moved = true;
                }
                break;
            case MOVE_EAST:
                if( !currentTile.hasWallRight() && board.getTileAt(pawn.getCoordinate().move(0, 1)) != null && !board.getTileAt(pawn.getCoordinate().move(0, 1)).isOccupied()) {
                    pawn.moveEast();
                    if(Config.PRINT_EVERYTHING){
                        System.out.println("Moved "+ pawnColor +" east");
                    }
                    moved = true;
                }
                break;
        }

        //update occupied
        if(moved){
            board.getTileAt(pawn.getCoordinate()).setOccupied(true);

            updateLastMovedPawn(pawn, action);

            // set the previous tile to not occupied
            switch (action) {
                case MOVE_NORTH:
                    board.getTileAt(pawn.getCoordinate().move(1, 0)).setOccupied(false);
                    break;
                case MOVE_SOUTH:
                    board.getTileAt(pawn.getCoordinate().move(-1, 0)).setOccupied(false);
                    break;
                case MOVE_WEST:
                    board.getTileAt(pawn.getCoordinate().move(0, 1)).setOccupied(false);
                    break;
                case MOVE_EAST:
                    board.getTileAt(pawn.getCoordinate().move(0, -1)).setOccupied(false);
                    break;
            }
        }

        // check if the pawn has moved onto a Timer tile
        Tile newTile = board.getTileAt(pawn.getCoordinate());
        if(moved && newTile.getType() == TileType.TIMER && !newTile.isUsed()){
            if(Config.PRINT_EVERYTHING){
                System.out.println("Pawn " + pawnColor + " landed on a Timer tile!");
            }
            board.getTimer().flipTimer();
            newTile.setUsed(true);
            // remove timer from board's active timers
            board.removeTimerFromGoals(pawn.getCoordinate());
        }

        return pawn;
    }

    private void updateLastMovedPawn(Pawn pawn, Action action){
        // Notify all listeners about the pawn move
        for (StateChangeListener listener : listeners) {
            listener.onPawnMoved(pawn, action);
        }
    }

    public void firstPhaseCompleted(){
        for (StateChangeListener listener : listeners) {
            listener.onFirstPhaseCompleted();
        }
    }
}
