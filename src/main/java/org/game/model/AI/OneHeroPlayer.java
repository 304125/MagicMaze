package org.game.model.AI;

import org.game.model.Action;
import org.game.model.Coordinate;
import org.game.model.board.Board;
import org.game.model.Pawn;
import org.game.model.board.GeneralGoalManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneHeroPlayer extends AIPlayer implements PawnMoveListener{
    private Pawn lastMovedPawn;
    private ActionTree actionTree;
    private int maximumChunkSize;
    private final GeneralGoalManager generalGoalManager;
    PathFinder pathFinder;

    public OneHeroPlayer(List<Action> actions, String name, Board board, GeneralGoalManager generalGoalManager) {
        super(actions, name, board);
        lastMovedPawn = board.getRandomPawn();
        actionTree = new ActionTree();
        maximumChunkSize = ChunkGenerator.generateChunkSize();
        System.out.println("OneHeroPlayer initialized with maximum chunk size: " + maximumChunkSize);
        this.generalGoalManager = generalGoalManager;
        this.pathFinder = new PathFinder(board.getTiles());
        buildActionTree();

        // fist fix it and then use it
        // startActionExecution();
    }

    // builds action tree from scratch
    private void buildActionTree() {
        actionTree = new ActionTree();
        int currentChunkSize = 0;
        List<Coordinate> goalCoordinates = generalGoalManager.getPawnGoalManager(lastMovedPawn.getColor()).getAllGoals();

        // consider all possible goals of that color of hero in ascending order of distance

        // order goalCoordinates by distance to lastMovedPawn
        Map<Coordinate, Integer> distanceMap = new HashMap<>();
        for (Coordinate goal : goalCoordinates) {
            int estimatedDistance = pathFinder.findDistance(lastMovedPawn.getCoordinate(), goal);
            distanceMap.put(goal, estimatedDistance);
        }

        // sort goalCoordinates by estimated distance from distanceMap
        goalCoordinates.sort((c1, c2) -> distanceMap.get(c1) - distanceMap.get(c2));

        // starting from the closest goal, find the shortest path
        for (Coordinate goal : goalCoordinates) {
            SearchPath path = pathFinder.findShortestPath(lastMovedPawn.getCoordinate(), goal);
            currentChunkSize += ChunkGenerator.countChunks(path);
            if(currentChunkSize > maximumChunkSize){
                System.out.println("Reached maximum chunk size limit while building action tree. Stopping further path additions.");
                break;
            }
            actionTree.addRoute(path.getActions());
            System.out.println("Added path to goal " + goal + " with estimated distance " + distanceMap.get(goal) + " to action tree.");
        }
    }

    @Override
    public void onPawnMoved(Pawn movedPawn, Action action) {
        if(movedPawn.getColor() == lastMovedPawn.getColor()) {
            boolean moved = actionTree.takeAction(action);
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

    public void startActionExecution() {
        Thread actionExecutionThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    boolean actionTaken = actionTree.takeBestAction();
                    if (!actionTaken) {
                        System.out.println("No valid action to take. Rebuilding action tree...");
                        buildActionTree(); // Rebuild the tree if no action is available
                    }
                    Thread.sleep(1000); // Wait for 1 second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    System.out.println("Action execution thread interrupted. Stopping...");
                }
            }
        });

        actionExecutionThread.setDaemon(true); // Optional: Set as daemon thread
        actionExecutionThread.start();
    }
}
