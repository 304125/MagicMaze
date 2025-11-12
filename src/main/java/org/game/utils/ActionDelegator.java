package org.game.utils;

import org.game.model.*;
import org.game.model.Action;
import org.game.model.board.Board;
import org.game.ui.BoardUI;

public class ActionDelegator {
    private final Game game;
    private final Board board;
    private final BoardUI boardUI;

    public ActionDelegator(Game game, BoardUI boardUI) {
        this.game = game;
        board = game.getBoard();
        this.boardUI = boardUI;
    }

    public void movePawn(Color pawnColor, Action action) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn;
        if(action == Action.MOVE_EAST || action == Action.MOVE_WEST || action == Action.MOVE_NORTH || action == Action.MOVE_SOUTH){
            updatedPawn = board.movePawn(pawnColor, action);
            if(board.isPawnAtTimerTile(updatedPawn)){
                boardUI.changeTimerColorToDark(updatedPawn.getCoordinate());
            }
        }
        else if(action == Action.ESCALATOR){
            updatedPawn = board.useEscalator(pawnColor);
            if(updatedPawn.equals(previousPawn)){
                System.out.println("No escalator to use for pawn " + pawnColor);
            }
        }
        else{
            System.out.println("Unknown action");
            return;
        }

        handlePawns(previousPawn, updatedPawn);
    }

    public void discover(Color pawnColor) {
        Pawn pawn = new Pawn(board.getPawnByColor(pawnColor));
        // check if the pawn is standing on discovery tile
        Tile currentTile = board.getTileAt(pawn.getCoordinate());
        if(currentTile.getType() == TileType.DISCOVERY && pawnColor == currentTile.getColor()){
            Coordinate corner = board.getLeftTopCornerOfNewCard(pawn.getCoordinate());
            boolean discovered = game.discoverCard(pawn);
            // re-render the board
            if(discovered){
                boardUI.renderDiscoveredTiles(corner);
            }
        }
    }

    public void vortexPawn(Color pawnColor, int vortexNumber) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn = board.useVortex(pawnColor, vortexNumber);
        if(updatedPawn.equals(previousPawn)){
            System.out.println("No vortex to use for pawn " + pawnColor + " with number " + vortexNumber);
            return;
        }

        handlePawns(previousPawn, updatedPawn);
    }

    private void handlePawns(Pawn previousPawn, Pawn updatedPawn) {
        if(Config.PRINT_EVERYTHING){
            board.printAllPawns();
        }
        boardUI.unhighlightPawn(previousPawn);
        boardUI.highlightPawn(updatedPawn);
    }
}
