package org.game.utils;

import org.game.model.*;
import org.game.model.Action;
import org.game.model.board.Board;
import org.game.model.board.PawnManager;
import org.game.ui.BoardUI;
import org.game.utils.output.ActionWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActionDelegator {
    private final Game game;
    private final Board board;
    private final BoardUI boardUI;
    private final ActionWriter actionWriter;

    public ActionDelegator(Game game, BoardUI boardUI, ActionWriter actionWriter) {
        this.game = game;
        board = game.getBoard();
        this.boardUI = boardUI;
        this.actionWriter = actionWriter;
    }

    // return false if the action was not performed
    public boolean movePawn(Color pawnColor, Action action) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn;
        if(action == Action.MOVE_EAST || action == Action.MOVE_WEST || action == Action.MOVE_NORTH || action == Action.MOVE_SOUTH){
            updatedPawn = board.movePawn(pawnColor, action);
            if(updatedPawn.getCoordinate() == previousPawn.getCoordinate()){
                // the pawn has not moved
                return false;
            }
            else{
                if(actionWriter != null) actionWriter.recordMove(pawnColor, action);

                if(board.isPawnAtTimerTile(updatedPawn)){
                    boardUI.changeTimerColorToDark(updatedPawn.getCoordinate());
                }
            }
        }
        else if(action == Action.ESCALATOR){
            updatedPawn = board.useEscalator(pawnColor);
            if(updatedPawn.equals(previousPawn)){
                System.out.println("No escalator to use for pawn " + pawnColor);
                return false;
            }
            else{
                if(actionWriter != null) actionWriter.recordMove(pawnColor, action);
            }
        }
        else{
            System.out.println("Unknown action");
            return false;
        }
        handlePawnsUI(previousPawn, updatedPawn);

        // check for goal conditions
        board.checkGoalConditions();
        return true;
    }

    public boolean discoverRandomCard(Color pawnColor) {
        Pawn pawn = new Pawn(board.getPawnByColor(pawnColor));
        // check if the pawn is standing on discovery tile
        Tile currentTile = board.getTileAt(pawn.getCoordinate());
        if(currentTile.getType() == TileType.DISCOVERY && pawnColor == currentTile.getColor()){
            Coordinate corner = board.getLeftTopCornerOfNewCard(pawn.getCoordinate());
            int discoveredCardId = game.discoverRandomCard(pawn);
            // re-render the board
            if(discoveredCardId != 0){
                boardUI.renderDiscoveredTiles(corner);
                if(actionWriter != null) actionWriter.recordDiscover(pawnColor, discoveredCardId);
            }
            return true;
        }
        else{
            return false;
        }
    }

    public boolean discoverGivenCard(Color pawnColor, int cardId) {
        Pawn pawn = new Pawn(board.getPawnByColor(pawnColor));
        // check if the pawn is standing on discovery tile
        Tile currentTile = board.getTileAt(pawn.getCoordinate());
        if(currentTile.getType() == TileType.DISCOVERY && pawnColor == currentTile.getColor()){
            Coordinate corner = board.getLeftTopCornerOfNewCard(pawn.getCoordinate());
            int discoveredCardId = game.discoverGivenCard(pawn, cardId);
            // re-render the board
            if(discoveredCardId != 0){
                boardUI.renderDiscoveredTiles(corner);
                if(actionWriter != null) actionWriter.recordDiscover(pawnColor, discoveredCardId);
            }
            return true;
        }
        else{
            return false;
        }
    }
    public boolean vortexPawn(Color pawnColor, int vortexNumber) {
        if(!board.isFirstPhase()){
            System.out.println("Cannot use vortex outside of first phase");
            return false;
        }
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn = board.useVortex(pawnColor, vortexNumber);
        if(updatedPawn.equals(previousPawn)){
            System.out.println("No vortex to use for pawn " + pawnColor + " with number " + vortexNumber);
            return false;
        }
        if(actionWriter != null) actionWriter.recordVortex(pawnColor, vortexNumber);

        handlePawnsUI(previousPawn, updatedPawn);
        return true;
    }

    public boolean vortexPawn(Color color, Coordinate vortexCoordinate){
        int vortexNumber = board.getCardIdOfVortex(vortexCoordinate, color);
        return vortexPawn(color, vortexNumber);
    }

    private void handlePawnsUI(Pawn previousPawn, Pawn updatedPawn) {
        if(Config.PRINT_EVERYTHING){
            board.printAllPawns();
        }
        boardUI.unhighlightPawn(previousPawn);
        boardUI.highlightPawn(updatedPawn);
    }

    public Pawn getBlockingPawn(Pawn pawn, Action action, Coordinate vortexCoordinate){
        switch (action){
            case Action.MOVE_EAST :
                return board.getPawnAt(pawn.getCoordinate().move(0, 1));
            case Action.MOVE_WEST:
                return board.getPawnAt(pawn.getCoordinate().move(0, -1));
            case Action.MOVE_NORTH:
                return board.getPawnAt(pawn.getCoordinate().move(-1, 0));
            case Action.MOVE_SOUTH:
                return board.getPawnAt(pawn.getCoordinate().move(1, 0));
            case Action.ESCALATOR:
                return board.getPawnAt(PawnManager.getOtherSideOfEscalator(pawn.getCoordinate()));
            case Action.VORTEX:
                return board.getPawnAt(vortexCoordinate);
        }

        return null;
    }

    public boolean vortexToClosest(Color pawnColor){
        Pawn pawn = board.getPawnByColor(pawnColor);
        Coordinate closestVortex = board.getClosestVortex(pawn.getCoordinate(), pawn.getColor());
        return vortexPawn(pawn.getColor(), closestVortex);
    }

    public void placeDoSomething(Action action){
        game.placeDoSomething(action);
    }

    public void performRandomAvailableActionFromActionSet(List<Action> actions){
        // randomly ordered list of all colors and actions
        List<Color> allColors = new ArrayList<>(Arrays.asList(Color.GREEN, Color.ORANGE, Color.PURPLE, Color.YELLOW));
        Collections.shuffle(allColors);
        Collections.shuffle(actions);

        // going from first to last, try to perform action on color until one action is allowed. then stop
        boolean done = false;
        for(Action action: actions){
            for(Color color: allColors){
                switch (action){
                    case Action.DISCOVER: {
                        done = discoverRandomCard(color);
                    }
                    case Action.MOVE_EAST, Action.MOVE_NORTH, Action.MOVE_WEST, Action.MOVE_SOUTH, Action.ESCALATOR: {
                        done = movePawn(color, action);
                    }
                    case Action.VORTEX: {
                        done = vortexToClosest(color);
                    }
                }
                if(done){
                    return;
                }
            }
        }
        System.out.println("There is nothing this agent can do");
    }
}
