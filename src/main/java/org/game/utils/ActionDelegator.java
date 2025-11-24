package org.game.utils;

import org.game.model.*;
import org.game.model.Action;
import org.game.model.board.Board;
import org.game.ui.BoardUI;
import org.game.utils.output.ActionWriter;

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

    public void movePawn(Color pawnColor, Action action) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn;
        if(action == Action.MOVE_EAST || action == Action.MOVE_WEST || action == Action.MOVE_NORTH || action == Action.MOVE_SOUTH){
            updatedPawn = board.movePawn(pawnColor, action);
            if(actionWriter != null) actionWriter.recordMove(pawnColor, action);

            if(board.isPawnAtTimerTile(updatedPawn)){
                boardUI.changeTimerColorToDark(updatedPawn.getCoordinate());
            }
        }
        else if(action == Action.ESCALATOR){
            updatedPawn = board.useEscalator(pawnColor);
            if(updatedPawn.equals(previousPawn)){
                System.out.println("No escalator to use for pawn " + pawnColor);
            }
            else{
                if(actionWriter != null) actionWriter.recordMove(pawnColor, action);
            }
        }
        else{
            System.out.println("Unknown action");
            return;
        }
        handlePawnsUI(previousPawn, updatedPawn);

        // check for goal conditions
        board.checkGoalConditions();
    }

    public void discoverRandomCard(Color pawnColor) {
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
        }
    }

    public void discoverGivenCard(Color pawnColor, int cardId) {
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
        }
    }
    public void vortexPawn(Color pawnColor, int vortexNumber) {
        if(!board.isFirstPhase()){
            System.out.println("Cannot use vortex outside of first phase");
            return;
        }
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn = board.useVortex(pawnColor, vortexNumber);
        if(updatedPawn.equals(previousPawn)){
            System.out.println("No vortex to use for pawn " + pawnColor + " with number " + vortexNumber);
            return;
        }
        if(actionWriter != null) actionWriter.recordVortex(pawnColor, vortexNumber);

        handlePawnsUI(previousPawn, updatedPawn);
    }

    public void vortexPawn(Color color, Coordinate vortexCoordinate){
        int vortexNumber = board.getCardIdOfVortex(vortexCoordinate, color);
        vortexPawn(color, vortexNumber);
    }

    private void handlePawnsUI(Pawn previousPawn, Pawn updatedPawn) {
        if(Config.PRINT_EVERYTHING){
            board.printAllPawns();
        }
        boardUI.unhighlightPawn(previousPawn);
        boardUI.highlightPawn(updatedPawn);
    }
}
