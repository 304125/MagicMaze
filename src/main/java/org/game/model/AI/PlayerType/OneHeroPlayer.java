package org.game.model.AI.PlayerType;

import org.game.model.*;
import org.game.model.AI.*;
import org.game.model.board.Board;
import org.game.model.board.GeneralGoalManager;
import org.game.utils.Config;

import java.util.*;

public class OneHeroPlayer extends AIPlayer {
    private Pawn lastMovedPawn;
    private ActionTree actionTree;
    private final GeneralGoalManager generalGoalManager;
    PathFinder pathFinder;
    Thread actionExecutionThread;
    private boolean isThreadSleeping = false;
    private List<Color> otherPawnMoves = new ArrayList<>();
    private int ticksWaiting = 0;

    public OneHeroPlayer(List<Action> actions, String name, Board board) {
        super(actions, name, board);
        lastMovedPawn = board.getRandomPawn();
        actionTree = new ActionTree();
        if(Config.PRINT_EVERYTHING){
            System.out.println("OneHeroPlayer initialized with memory capacity: " + super.getCurrentMemoryCapacity());
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
        ticksWaiting = 0;
        if(!isThreadSleeping){
            try{
                isThreadSleeping = true;
                Thread.sleep(1000); // wait a bit to process
                isThreadSleeping = false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        otherPawnMoves.removeIf(color -> color.equals(lastMovedPawn.getColor()));
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
            if(currentChunkSize > super.getCurrentMemoryCapacity()){
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
        // there is nothing to do for this color now -> re-do with another color
        if(actionTree.isEmpty()){
            // re-plan for the last moved pawn not of current color
            if(otherPawnMoves.isEmpty()){
                lastMovedPawn = getActionDelegator().getRandomPawn();
            }
            else{
                Color replanForColor = otherPawnMoves.getLast();
                lastMovedPawn = getActionDelegator().getPawnByColor(replanForColor);
            }
            buildActionTree();
        }
        if(Config.PRINT_EVERYTHING){
            actionTree.printTree(getName());
        }
    }

    @Override
    public void onPawnMoved(Pawn movedPawn, Action action) {
        if(!isThreadSleeping){
            try{
                isThreadSleeping = true;
                Thread.sleep(500); // wait a bit to process
                isThreadSleeping = false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(movedPawn.getColor() == lastMovedPawn.getColor()) {
            ticksWaiting = 0;
            boolean moved = actionTree.takeAction(action);
            if(Config.PRINT_EVERYTHING){
                actionTree.printTree(getName());
            }
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
            otherPawnMoves.add(movedPawn.getColor());
            // if that pawn has been moved more times than my blindness (and is not the pawn i have tree for), re-build
            if(otherPawnMoves.stream().filter(color -> color.equals(movedPawn.getColor())).count() >= super.getBlindness()){
                lastMovedPawn = movedPawn;
                // re-build tree, another pawn was moved
                buildActionTree();
            }
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
            if(Config.PRINT_EVERYTHING){
                actionTree.printTree(getName());
            }

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
                    if(!isThreadSleeping){
                        isThreadSleeping = true;
                        Thread.sleep(1000); // Wait for 1 second
                        isThreadSleeping = false;
                    }

                    Action bestAction = actionTree.bestAction();
                    if(canPerformAction(bestAction)){
                        ticksWaiting = 0;
                        if (actionTree.isEmpty()) {
                            System.out.println("No valid action to take. Rebuilding action tree...");
                            buildActionTree(); // Rebuild the tree if no action is available
                            if(!isThreadSleeping){
                                isThreadSleeping = true;
                                Thread.sleep(3000); // Wait for 3 seconds
                                isThreadSleeping = false;
                            }
                        }
                        else{
                            attemptBestAction(bestAction);
                        }
                    }
                    else{
                        if(actionTree.isEmpty()){
                            buildActionTree();
                        }
                        else{
                            ticksWaiting++;
                            if(ticksWaiting >= super.getPatience()){
                                if(getActionDelegator().isPerformable(bestAction, lastMovedPawn.getColor())){
                                    getActionDelegator().placeDoSomething(bestAction);
                                }
                                else{
                                    // if the action is not performable, someone is blocking the pawn -> vortex
                                    getActionDelegator().placeDoSomething(Action.VORTEX);
                                }
                                ticksWaiting = 0;
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

    private void attemptBestAction(Action bestAction){
        // try to perform the action
        // if moved = false, then the action is blocked (by another pawn)
        boolean moved = true;
        switch (bestAction){
            case MOVE_EAST, MOVE_NORTH, MOVE_SOUTH, MOVE_WEST, ESCALATOR -> {
                if(Config.PRINT_EVERYTHING){
                    System.out.println(getName() + " is performing action: " + bestAction + " with pawn " + lastMovedPawn.getColor());
                }
                moved = getActionDelegator().movePawn(lastMovedPawn.getColor(), bestAction);
            }
            case DISCOVER -> {
                if(Config.PRINT_EVERYTHING){
                    System.out.println(getName() + " is performing action: " + bestAction + " with pawn " + lastMovedPawn.getColor());
                }
                getActionDelegator().discoverRandomCard(lastMovedPawn.getColor());
            }
            case VORTEX -> {
                if(Config.PRINT_EVERYTHING){
                    System.out.println(getName() + " is performing action: " + bestAction + " with pawn " + lastMovedPawn.getColor());
                }
                moved = getActionDelegator().vortexPawn(lastMovedPawn.getColor(), bestAction.getVortexCoordinate());
            }
        }

        // if moved = false, I need to add plan for the blocking pawn
        if(!moved){
            Pawn blockingPawn = getActionDelegator().getBlockingPawn(lastMovedPawn, bestAction, bestAction.getVortexCoordinate());
            if(blockingPawn == null){
                // the blocking pawn has since moved, just wait it out and try again
                return;
            }
            else{
                // schedule a plan for the blocking pawn to move away
                // just vortex that pawn to the closest vortex
                // own color's vortex is never blocking
                if(canPerformAction(Action.VORTEX)){
                    getActionDelegator().vortexToClosest(blockingPawn.getColor());
                }
                else{
                    // Place a "do something" token in front of person who can do that action
                    getActionDelegator().placeDoSomething(Action.VORTEX);
                }
            }
        }
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

    @Override
    public void doSomething() {
        super.decreaseMemoryCapacity();

        // forceful re-build of action tree with diminished memory
        buildActionTree();

        // look if I can do anything according to my plan
        Action bestAction = actionTree.bestAction();
        if(canPerformAction(bestAction)){
            attemptBestAction(bestAction);
        }
        else{
            // someone wants me to do something, but I don't know what to do (not in my plan)
            // look at all pawns and perform a random action from my action set on a random pawn
            getActionDelegator().performRandomAvailableActionFromActionSet(super.getActions());
        }
    }

    @Override
    public void doSomethingPlaced(Player player){
        if(actionTree.isEmpty()){
            buildActionTree();
            return;
        }
        if(player.getActions().contains(actionTree.bestAction()) || !getActionDelegator().isPerformable(actionTree.bestAction(), lastMovedPawn.getColor())){
            // someone already notified the player about the same thing i am waiting for - no need to do it twice
            ticksWaiting = 0;
        }
    }
}
