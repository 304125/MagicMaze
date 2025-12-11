package org.game.utils;

import org.game.model.*;
import org.game.model.AI.PayoffCalculator;
import org.game.model.AI.PlayerType.AIPlayer;
import org.game.model.AI.StateChangeListener;
import org.game.model.Action;
import org.game.model.board.Board;
import org.game.model.board.PawnManager;
import org.game.ui.ActionUIUpdater;
import org.game.ui.BoardUI;
import org.game.utils.output.ActionWriter;
import static org.game.model.ActionType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ActionDelegator {
    private final Game game;
    private final Board board;
    private final BoardUI boardUI;
    private final ActionWriter actionWriter;
    private PayoffCalculator payoffCalculator;
    private final ActionUIUpdater actionUIUpdater;
    private final List<StateChangeListener> listeners = new ArrayList<>();

    public ActionDelegator(Game game, BoardUI boardUI, ActionWriter actionWriter) {
        this.game = game;
        board = game.getBoard();
        this.boardUI = boardUI;
        this.actionWriter = actionWriter;
        this.payoffCalculator = new PayoffCalculator(board.getTimer());
        this.actionUIUpdater = new ActionUIUpdater(boardUI);
        for(Player player : game.getPlayers()){
            if(player instanceof AIPlayer aiPlayer){
                addStateChangeListener(aiPlayer);
            }
        }
    }

    private void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    // return false if the action was not performed
    public boolean movePawn(Color pawnColor, Action action) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn;
        if(action.getType() == MOVE_EAST || action.getType() == MOVE_WEST || action.getType() == MOVE_NORTH || action.getType() == MOVE_SOUTH){
            updatedPawn = board.movePawn(pawnColor, action);
            if(updatedPawn.getCoordinate() == previousPawn.getCoordinate()){
                // the pawn has not moved
                return false;
            }
            else{
                // check for goal conditions
                board.checkGoalConditions();
                if(actionWriter != null) actionWriter.recordMove(pawnColor, action);

                if(board.isPawnAtTimerTile(updatedPawn)){
                    onTimerFlipped(board.getTimer().getTimeLeftInTimer());
                    boardUI.changeTimerColorToDark(updatedPawn.getCoordinate());
                }
            }
        }
        else if(action.getType() == ESCALATOR){
            updatedPawn = board.useEscalator(pawnColor);
            if(updatedPawn.getCoordinate().equals(previousPawn.getCoordinate())){
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

        board.updateLastMovedPawn(updatedPawn, action);

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
    public boolean vortexPawn(Color pawnColor, int vortexNumber, int heuristicType) {
        if(!board.isFirstPhase()){
            System.out.println("Cannot use vortex outside of first phase");
            return false;
        }
        Coordinate vortexCoordinate = board.getVortexCoordinateById(vortexNumber, pawnColor);
        Action action = new Action(VORTEX, vortexCoordinate);
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn = board.useVortex(pawnColor, vortexNumber);
        if(updatedPawn.getCoordinate().equals(previousPawn.getCoordinate())){
            System.out.println("No vortex to use for pawn " + pawnColor + " with number " + vortexNumber);
            // attempt to vortex the pawn standing on the destination away
            // only if it is a different color
            if(board.getPawnByColor(pawnColor).getCoordinate().equals(vortexCoordinate)){
                System.out.println("Vortex destination is occupied by the same color pawn. Cannot vortex.");
                return false;
            }
            else{
                Pawn blockingPawn = board.getPawnAt(vortexCoordinate);
                return vortexToClosest(blockingPawn.getColor(), heuristicType);
            }
        }
        if(actionWriter != null) actionWriter.recordVortex(pawnColor, vortexNumber);

        handlePawnsUI(previousPawn, updatedPawn);

        board.updateLastMovedPawn(updatedPawn, action);

        return true;
    }

    public boolean vortexPawn(Color color, Coordinate vortexCoordinate, int heuristicType) {
        int vortexNumber = board.getCardIdOfVortex(vortexCoordinate, color);
        System.out.println("Vortexing pawn " + color + " to vortex number " + vortexNumber + " at coordinate " + vortexCoordinate);
        return vortexPawn(color, vortexNumber, heuristicType);
    }

    private void handlePawnsUI(Pawn previousPawn, Pawn updatedPawn) {
        if(Config.PRINT_EVERYTHING){
            board.printAllPawns();
        }
        boardUI.unhighlightPawn(previousPawn);
        boardUI.highlightPawn(updatedPawn);
    }

    public Pawn getBlockingPawn(Pawn pawn, Action action, Coordinate vortexCoordinate){
        switch (action.getType()){
            case MOVE_EAST :
                return board.getPawnAt(pawn.getCoordinate().move(0, 1));
            case MOVE_WEST:
                return board.getPawnAt(pawn.getCoordinate().move(0, -1));
            case MOVE_NORTH:
                return board.getPawnAt(pawn.getCoordinate().move(-1, 0));
            case MOVE_SOUTH:
                return board.getPawnAt(pawn.getCoordinate().move(1, 0));
            case ESCALATOR:
                return board.getPawnAt(PawnManager.getOtherSideOfEscalator(pawn.getCoordinate()));
            case VORTEX:
                return board.getPawnAt(vortexCoordinate);
        }

        return null;
    }

    public boolean vortexToClosest(Color pawnColor, int heuristicType){
        System.out.println("Vortexing pawn " + pawnColor + " to closest vortex");
        Pawn pawn = board.getPawnByColor(pawnColor);
        Coordinate closestVortex = board.getClosestVortex(pawn.getCoordinate(), pawn.getColor(), heuristicType);

        boolean worked = vortexPawn(pawn.getColor(), closestVortex, heuristicType);

        if(!worked){
            Pawn blockingPawn = board.getPawnAt(closestVortex);
            if(blockingPawn.getColor() != pawnColor){
                // try to vortex the blocking pawn away if it is a different color
                // otherwise if it is the same color, it would loop forever (move itself to where it already is standing)
                return vortexToClosest(blockingPawn.getColor(), heuristicType);
            }
        }
        return true;
    }

    public void placeDoSomething(ActionType action){
        List<ActionType> allActions = game.getAllActionsForPlayer(action);
        actionWriter.recordDoSomething(allActions);
        game.placeDoSomething(action);
        placeDoSomethingUI(allActions);
    }

    public void placeDoSomethingUI(List<ActionType> actions){
        actionUIUpdater.updateUI(actions);
    }

    public boolean performRandomAvailableActionFromActionSet(List<ActionType> actions, int heuristicType){
        System.out.println("Panic!");
        // randomly ordered list of all colors and actions
        List<Pawn> allPawns = board.getPawns();
        List<Color> allColors = new ArrayList<>();
        for(Pawn pawn : allPawns){
            allColors.add(pawn.getColor());
        }
        Collections.shuffle(allColors);
        Collections.shuffle(actions);

        // going from first to last, try to perform action on color until one action is allowed. then stop
        boolean done = false;
        for(ActionType actionType: actions){
            for(Color color: allColors){
                System.out.println("Checking if actionType " + actionType + " is performable by pawn " + color);
                Action action = new Action(actionType);
                if(isPerformable(action, color, heuristicType)){
                    switch (actionType){
                        case DISCOVER: {
                            System.out.println("Trying to discover with pawn " + color);
                            done = discoverRandomCard(color);
                            break;
                        }
                        case MOVE_EAST, MOVE_NORTH, MOVE_WEST, MOVE_SOUTH, ESCALATOR: {
                            System.out.println("Trying to move pawn " + color + " with actionType " + actionType);
                            done = movePawn(color, action);
                            break;
                        }
                        case VORTEX: {
                            if(isFirstPhase()){
                                System.out.println("Trying to vortex pawn " + color);
                                done = vortexToClosest(color, heuristicType);
                            }
                            break;
                        }
                    }
                    if(done){
                        System.out.println("Action " + actionType + " performed by pawn " + color);
                        return true;
                    }
                }
            }
        }
        System.out.println("There is nothing this agent can do");
        return false;
    }

    public boolean isPerformable(Action action, Color pawnColor, int heuristicType){
        if(action == null){
            return false;
        }
        Coordinate pawnCoordinate = board.getPawnByColor(pawnColor).getCoordinate();
        Tile currentTile = board.getTileAt(pawnCoordinate);
        switch (action.getType()){
            case DISCOVER: {
                return (currentTile.getType() == TileType.DISCOVERY && pawnColor == currentTile.getColor());
            }
            case ESCALATOR: {
                boolean isCurrentEscalator = currentTile.hasEscalator();
                if(!isCurrentEscalator){
                    return false;
                }
                Coordinate otherSide = PawnManager.getOtherSideOfEscalator(pawnCoordinate);
                boolean isOtherSideOccupied = board.getTileAt(otherSide).isOccupied();
                return !isOtherSideOccupied;
            }
            case VORTEX: {
                if(action.getVortexCoordinate() == null){
                    // vortex to the closest one (triggered by random action)
                    Coordinate closestVortex = board.getClosestVortex(pawnCoordinate, pawnColor, heuristicType);
                    return !board.getTileAt(closestVortex).isOccupied();
                }
                else{
                    // vortex to a specific coordinate
                    return !board.getTileAt(action.getVortexCoordinate()).isOccupied();
                }
            }
            case MOVE_EAST: {
                if(board.getTileAt(pawnCoordinate.move(0, 1)) == null) return false;
                return !board.getTileAt(pawnCoordinate.move(0, 1)).isOccupied();
            }

            case MOVE_NORTH: {
                if(board.getTileAt(pawnCoordinate.move(-1, 0)) == null) return false;
                return !board.getTileAt(pawnCoordinate.move(-1, 0)).isOccupied();
            }

            case MOVE_SOUTH: {
                if(board.getTileAt(pawnCoordinate.move(1, 0)) == null) return false;
                return !board.getTileAt(pawnCoordinate.move(1, 0)).isOccupied();
            }

            case MOVE_WEST: {
                if(board.getTileAt(pawnCoordinate.move(0, -1)) == null) return false;
                return !board.getTileAt(pawnCoordinate.move(0, -1)).isOccupied();
            }
        }
        return false;
    }

    public Pawn getPawnByColor(Color color){
        return board.getPawnByColor(color);
    }

    public Pawn getRandomPawn(){
        return board.getRandomPawn();
    }

    public boolean isFirstPhase(){
        return board.isFirstPhase();
    }

    public boolean areAllGoalsDiscovered(){
        return board.areAllGoalsDiscovered();
    }

    public int getTimerPayoff(float logBase){
        return payoffCalculator.calculateLogarithmicTimePayoff(logBase);
    }

    public void onTimerFlipped(int timeLeft){
        for (StateChangeListener listener : listeners) {
            listener.onTimerFlipped(timeLeft);
        }
    }
}
