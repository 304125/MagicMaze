package org.game.model.AI;

import org.game.model.Action;
import org.game.model.Coordinate;
import org.game.model.Tile;
import org.game.model.board.Board;
import org.game.model.Pawn;
import org.game.model.board.GeneralGoalManager;
import org.game.utils.ActionDelegator;
import org.game.utils.Config;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneHeroPlayer extends AIPlayer{
    private Pawn lastMovedPawn;
    private ActionTree actionTree;
    private final int maximumChunkSize;
    private final GeneralGoalManager generalGoalManager;
    PathFinder pathFinder;
    Thread actionExecutionThread;

    public OneHeroPlayer(List<Action> actions, String name, Board board) {
        super(actions, name, board);
        lastMovedPawn = board.getRandomPawn();
        actionTree = new ActionTree();
        maximumChunkSize = ChunkGenerator.generateChunkSize();
        if(Config.PRINT_EVERYTHING){
            System.out.println("OneHeroPlayer initialized with maximum chunk size: " + maximumChunkSize);
        }
        this.generalGoalManager = GeneralGoalManager.getInstance();
        this.pathFinder = new PathFinder(board.getTiles(), board);
    }

    @Override
    public void startGame(){
        buildActionTree();
        startActionExecution();
    }

    // builds action tree from scratch
    private void buildActionTree() {
        actionTree = new ActionTree();
        int currentChunkSize = 0;
        List<Coordinate> goalCoordinates = generalGoalManager.getPawnGoalManager(lastMovedPawn.getColor()).getAllGoals();
        if(Config.PRINT_EVERYTHING){
            System.out.println("Building action tree for pawn " + lastMovedPawn.getColor() + " towards goals: " + goalCoordinates);
        }

        // consider all possible goals of that color of hero in ascending order of distance

        // order goalCoordinates by distance to lastMovedPawn
        Map<Coordinate, Integer> distanceMap = new HashMap<>();
        for (Coordinate goal : goalCoordinates) {
            int estimatedDistance = pathFinder.findDistance(lastMovedPawn.getCoordinate(), goal);
            distanceMap.put(goal, estimatedDistance);
        }
        if(Config.PRINT_EVERYTHING){
            System.out.println("Distance map from pawn " + lastMovedPawn.getColor() + " at " + lastMovedPawn.getCoordinate() + ": " + distanceMap);
        }

        // sort goalCoordinates by estimated distance from distanceMap
        goalCoordinates.sort(Comparator.comparingInt(distanceMap::get));

        // starting from the closest goal, find the shortest path
        for (Coordinate goal : goalCoordinates) {
            SearchPath path = pathFinder.findShortestPath(lastMovedPawn.getCoordinate(), goal, lastMovedPawn.getColor());
            currentChunkSize += ChunkGenerator.countChunks(path);
            if(currentChunkSize > maximumChunkSize){
                if(Config.PRINT_EVERYTHING){
                    System.out.println("Reached maximum chunk size limit while building action tree. Stopping further path additions.");
                }
            }
            int priority = calculatePriority(goal);
            actionTree.addRoute(path.getActions(), priority);
            if(Config.PRINT_EVERYTHING){
                System.out.println("Added path to goal " + goal + " with estimated distance " + distanceMap.get(goal) + " to action tree.");
            }
        }
//        if(Config.PRINT_EVERYTHING){
//        }
        actionTree.printTree(getName());
    }

    @Override
    public void onPawnMoved(Pawn movedPawn, Action action) {
        if(movedPawn.getColor() == lastMovedPawn.getColor()) {
            boolean moved = actionTree.takeAction(action);
            actionTree.printTree(getName());
            if(actionTree.isEmpty()){
                // re-build tree, all actions used up
                buildActionTree();
            }
            if (!moved) {
                // re-build tree, the pawn was moved in an unexpected way
                buildActionTree();
            }
        }
        else{
            lastMovedPawn = movedPawn;
            // re-build tree, another pawn was moved
            buildActionTree();
        }
    }

    @Override
    public void onDiscovered(Pawn pawn){
        if(pawn.getColor() == lastMovedPawn.getColor()) {
            boolean moved = actionTree.takeAction(Action.DISCOVER);
            if(actionTree.isEmpty()){
                // re-build tree, all actions used up
                buildActionTree();
            }

            actionTree.printTree(getName());
            if (!moved) {
                // re-build tree, the pawn was moved in an unexpected way
                buildActionTree();
            }
        }
        else{
            lastMovedPawn = pawn;
            // re-build tree, another pawn was moved
            buildActionTree();
        }
    }

    @Override
    public void startActionExecution() {
        actionExecutionThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000); // Wait for 1 second
                    Action bestAction = actionTree.bestAction();
                    if(canPerformAction(bestAction)){
                        //boolean actionTaken = actionTree.takeAction(bestAction);
                        if (actionTree.isEmpty()) {
                            System.out.println("No valid action to take. Rebuilding action tree...");
                            buildActionTree(); // Rebuild the tree if no action is available
                            Thread.sleep(3000); // Wait for 3 seconds
                        }
                        else{
                            // perform the action
                            switch (bestAction){
                                case MOVE_EAST, MOVE_NORTH, MOVE_SOUTH, MOVE_WEST, ESCALATOR -> {
                                    if(Config.PRINT_EVERYTHING){

                                    }
                                    System.out.println(getName() + " is performing action: " + bestAction + " with pawn " + lastMovedPawn.getColor());
                                    getActionDelegator().movePawn(lastMovedPawn.getColor(), bestAction);
                                }
                                case DISCOVER -> {
                                    if(Config.PRINT_EVERYTHING){

                                    }
                                    System.out.println(getName() + " is performing action: " + bestAction + " with pawn " + lastMovedPawn.getColor());
                                    getActionDelegator().discoverRandomCard(lastMovedPawn.getColor());
                                }
                                case VORTEX -> {
                                    if(Config.PRINT_EVERYTHING){

                                    }
                                    System.out.println(getName() + " is performing action: " + bestAction + " with pawn " + lastMovedPawn.getColor());
                                    getActionDelegator().vortexPawn(lastMovedPawn.getColor(), bestAction.getVortexCoordinate());
                                }
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    System.out.println("Action execution thread interrupted. Stopping...");
                }
            }
        });

        actionExecutionThread.setDaemon(true); // Optional: Set as daemon thread
        actionExecutionThread.start();
    }

    @Override
    public void onFirstPhaseCompleted(){
        // force build of action tree for all heroes
        buildActionTree();
    }

    public int calculatePriority(Coordinate goal){
        Tile goalTile = getBoard().getTileAt(goal);
        return switch (goalTile.getType()) {
            case DISCOVERY -> 2;
            case TIMER -> 1;
            default -> 0;
        };
    }

    public void endGame(){
        actionExecutionThread.interrupt();
    }
}
