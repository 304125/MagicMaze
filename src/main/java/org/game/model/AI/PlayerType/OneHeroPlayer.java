package org.game.model.AI.PlayerType;

import org.game.model.*;
import org.game.model.AI.*;
import org.game.model.board.Board;
import org.game.model.board.GeneralGoalManager;
import org.game.utils.Config;

import java.util.*;

public class OneHeroPlayer extends AIPlayer {
    private Pawn currentlyPlannedPawn;
    private ActionTree actionTree;
    private final GeneralGoalManager generalGoalManager;
    PathFinder pathFinder;
    Thread actionExecutionThread;
    private boolean isThreadSleeping = false;
    private List<Color> otherPawnMoves = new ArrayList<>();
    private int ticksWaiting = 0;
    private boolean running = false;
    private boolean thinking = false;
    private boolean buildingTree = false;
    private boolean iWasLastToMove = false;
    private long lastDoSomethingPlacedTimestamp = 0;
    private boolean updatingOtherPawnMoves = false;

    public OneHeroPlayer(List<Action> actions, String name, Board board) {
        super(actions, name, board);
        currentlyPlannedPawn = board.getRandomPawn();
        actionTree = new ActionTree();
        if(Config.PRINT_EVERYTHING){
            System.out.println("OneHeroPlayer initialized with memory capacity: " + super.getCurrentMemoryCapacity());
        }
        this.generalGoalManager = GeneralGoalManager.getInstance();
        this.pathFinder = new PathFinder(board.getTiles(), board);
    }

    @Override
    public void startGame(){
        running = true;
        buildActionTree();
        startActionExecution();
    }

    // builds action tree from scratch
    private void buildActionTree() {
        buildingTree = true;
        ticksWaiting = 0;
        if(!isThreadSleeping && running){
            try{
                isThreadSleeping = true;
                Thread.sleep(1000); // wait a bit to process
                isThreadSleeping = false;
            } catch (InterruptedException e) {
                System.out.println("Build action tree sleep interrupted.");
                Thread.currentThread().interrupt();
            }
        }

        while(updatingOtherPawnMoves){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Build action tree wait interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        updatingOtherPawnMoves = true;
        if(otherPawnMoves.contains(currentlyPlannedPawn.getColor())){
            otherPawnMoves.removeIf(color -> color.equals(currentlyPlannedPawn.getColor()));
        }
        updatingOtherPawnMoves = false;

        actionTree = new ActionTree();
        int currentChunkSize = 0;
        List<Coordinate> goalCoordinates = generalGoalManager.getPawnGoalManager(currentlyPlannedPawn.getColor()).getAllGoals();
        if(Config.PRINT_EVERYTHING){

        }
        System.out.println("Building action tree for pawn " + currentlyPlannedPawn.getColor() + " towards goals: " + goalCoordinates);

        // consider all possible goals of that color of hero in ascending order of distance

        // order goalCoordinates by distance to lastMovedPawn
        Map<Coordinate, Integer> distanceMap = new HashMap<>();
        for (Coordinate goal : goalCoordinates) {
            int estimatedDistance = pathFinder.findDistance(currentlyPlannedPawn.getCoordinate(), goal);
            distanceMap.put(goal, estimatedDistance);
        }
        if(Config.PRINT_EVERYTHING){
            System.out.println("Distance map from pawn " + currentlyPlannedPawn.getColor() + " at " + currentlyPlannedPawn.getCoordinate() + ": " + distanceMap);
        }

        // sort goalCoordinates by estimated distance from distanceMap
        goalCoordinates.sort(Comparator.comparingInt(distanceMap::get));

        // starting from the closest goal, find the shortest path
        for (Coordinate goal : goalCoordinates) {
            SearchPath path = pathFinder.findShortestPath(currentlyPlannedPawn.getCoordinate(), goal, currentlyPlannedPawn.getColor());
            currentChunkSize += ChunkGenerator.countChunks(path);
            if(currentChunkSize > super.getCurrentMemoryCapacity()){
                if(Config.PRINT_EVERYTHING){
                    System.out.println("Reached maximum chunk size limit while building action tree. Stopping further path additions.");
                }
            }
            Double priority = calculatePriority(goal);
            actionTree.addRoute(path.getActions(), priority);
            if(Config.PRINT_EVERYTHING){
                System.out.println("Added path to goal " + goal + " with estimated distance " + distanceMap.get(goal) + " to action tree.");
            }
        }

        System.out.println("Finished building action tree for pawn " + currentlyPlannedPawn.getColor() + ".");

        // there is nothing to do for this color now -> re-do with another color
        if(actionTree.isEmpty() || !actionTree.areAnyLeafsPositivePriority()){
            System.out.println("Action tree is empty or has no positive priority leafs for pawn "+currentlyPlannedPawn.getColor()+". Re-planning for another pawn.");
            // re-plan for the last moved pawn not of current color
            if(otherPawnMoves.isEmpty()){
                currentlyPlannedPawn = getActionDelegator().getRandomPawn();
            }
            else{
                Color replanForColor = otherPawnMoves.getLast();
                currentlyPlannedPawn = getActionDelegator().getPawnByColor(replanForColor);
            }
            buildActionTree();
        }
        if(Config.PRINT_EVERYTHING){
        }


        System.out.println("Planned for pawn of color: "+currentlyPlannedPawn.getColor());
        actionTree.printTree(getName(), super.getActions());

        buildingTree = false;
    }

    @Override
    public void onPawnMoved(Pawn movedPawn, Action action) {
        if(canPerformAction(action)){
            iWasLastToMove = true;
        }
        else{
            iWasLastToMove = false;
        }

        if(!isThreadSleeping && running){
            try{
                isThreadSleeping = true;
                Thread.sleep(500); // wait a bit to process
                isThreadSleeping = false;
            } catch (InterruptedException e) {
                System.out.println("On pawn moved sleep interrupted.");
                Thread.currentThread().interrupt();
            }
        }

        if(movedPawn.getColor().equals(currentlyPlannedPawn.getColor())) {
            ticksWaiting = 0;
            boolean moved = actionTree.takeAction(action);
            if(Config.PRINT_EVERYTHING){
                actionTree.printTree(getName(), super.getActions());
            }
            if(actionTree.isEmpty()){
                // re-build tree, all actions used up
                buildActionTree();
            }
            if (!moved) {
                System.out.println("Action "+action+" taken by pawn "+movedPawn.getColor()+" was not in my ("+ super.getName()+") plan. Rebuilding action tree...");
                // re-build tree, the pawn was moved in an unexpected way
                buildActionTree();
            }
        }
        else{
            while(updatingOtherPawnMoves){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("On pawn moved wait interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
            otherPawnMoves.add(movedPawn.getColor());
            // if that pawn has been moved more times than my blindness (and is not the pawn i have tree for), re-build
            if(otherPawnMoves.stream().filter(color -> color.equals(movedPawn.getColor())).count() >= super.getBlindness()){
                currentlyPlannedPawn = movedPawn;
                // re-build tree, another pawn was moved
                buildActionTree();
            }
        }
    }

    @Override
    public void onDiscovered(Pawn pawn){
        if(canPerformAction(Action.DISCOVER)){
            iWasLastToMove = true;
        }
        else{
            iWasLastToMove = false;
        }

        if(pawn.getColor() == currentlyPlannedPawn.getColor()) {
            boolean moved = actionTree.takeAction(Action.DISCOVER);
            if(actionTree.isEmpty()){
                // re-build tree, all actions used up
                buildActionTree();
            }
            if(Config.PRINT_EVERYTHING){
                actionTree.printTree(getName(), super.getActions());
            }

            if (!moved) {
                // re-build tree, the pawn was moved in an unexpected way
                buildActionTree();
            }
        }
        else{
            currentlyPlannedPawn = pawn;
            // re-build tree, another pawn was moved
            buildActionTree();
        }
    }

    @Override
    public void startActionExecution() {
        actionExecutionThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // only wait if someone else was moving
                    // if i was moving last, continue moving (go 3 left in once ex.)
                    if(!iWasLastToMove){
                        if(!isThreadSleeping && running){
                            isThreadSleeping = true;
                            Thread.sleep(1000); // Wait for 1 second
                            isThreadSleeping = false;
                        }
                    }

                    if(buildingTree){
                        continue; // skip this iteration if the tree is being built
                    }
                    if(thinking){
                        continue; // skip this iteration if currently occupied with do something
                    }
                    Action bestAction = actionTree.bestAction();
                    while(bestAction == null){
                        // nothing is worth doing
                        System.out.println("No action is worth taking ("+currentlyPlannedPawn.getColor()+"). Rebuilding action tree...");
                        buildActionTree(); // Rebuild the tree if no action is available
                        if(!isThreadSleeping && running){
                            isThreadSleeping = true;
                            Thread.sleep(3000); // Wait for 3 seconds
                            isThreadSleeping = false;
                        }
                        bestAction = actionTree.bestAction();
                    }
                    if(canPerformAction(bestAction)){
                        if (actionTree.isEmpty()) {
                            System.out.println("No valid action to take. Rebuilding action tree...");
                            buildActionTree(); // Rebuild the tree if no action is available
                            if(!isThreadSleeping && running){
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
                                ticksWaiting = 0;
                                if(getActionDelegator().isPerformable(bestAction, currentlyPlannedPawn.getColor())){
                                    placeDoSomething(bestAction);
                                }
                                else{
                                    // if the action is not performable, someone is blocking the pawn -> vortex
                                    placeDoSomething(Action.VORTEX);
                                }
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    System.out.println("Action execution thread interrupted. Stopping...");
                    Thread.currentThread().interrupt(); // Restore interrupted status
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
        if(Config.PRINT_EVERYTHING){

        }
        System.out.println(getName() + " is attempting to perform action: " + bestAction + " with pawn " + currentlyPlannedPawn.getColor());

        switch (bestAction){
            case MOVE_EAST, MOVE_NORTH, MOVE_SOUTH, MOVE_WEST, ESCALATOR -> {
                moved = getActionDelegator().movePawn(currentlyPlannedPawn.getColor(), bestAction);
            }
            case DISCOVER -> {
                getActionDelegator().discoverRandomCard(currentlyPlannedPawn.getColor());
            }
            case VORTEX -> {
                moved = getActionDelegator().vortexPawn(currentlyPlannedPawn.getColor(), bestAction.getVortexCoordinate());
            }
        }
        if(moved){
            ticksWaiting = 0;
        }

        // if moved = false, I need to add plan for the blocking pawn
        if(!moved){
            if(Config.PRINT_EVERYTHING){
                System.out.println("It has not moved, so something is blocking the action.");
            }
            tryToClearBlockingPawn(bestAction);
        }
    }

    private void tryToClearBlockingPawn(Action bestAction){
        Pawn blockingPawn = getActionDelegator().getBlockingPawn(currentlyPlannedPawn, bestAction, bestAction.getVortexCoordinate());
        if(blockingPawn == null){
            if(Config.PRINT_EVERYTHING){
                System.out.println("Hmmmm something weird happened");
            }
            // the blocking pawn has since moved or it is not blocking what i think it is, just wait it out and try again
            buildActionTree();
            return;
        }
        else{
            if(Config.PRINT_EVERYTHING){
                System.out.println("I "+ super.getName()+" can see that pawn "+blockingPawn.getColor()+" is blocking best action "+bestAction+" for pawn "+currentlyPlannedPawn.getColor());
                System.out.println("Can I vortex? "+canPerformAction(Action.VORTEX));
            }
            // schedule a plan for the blocking pawn to move away
            // just vortex that pawn to the closest vortex
            // own color's vortex is never blocking
            if(canPerformAction(Action.VORTEX)){
                getActionDelegator().vortexToClosest(blockingPawn.getColor());
            }
            else{
                // Place a "do something" token in front of person who can do that action
                if(Config.PRINT_EVERYTHING){
                    System.out.println("My patience: "+super.getPatience()+", ticks waiting: "+ticksWaiting);
                }
                if(ticksWaiting >= super.getPatience()){
                    placeDoSomething(Action.VORTEX);
                    ticksWaiting = 0;
                }
                else{
                    ticksWaiting++;
                }
            }
        }
    }

    @Override
    public void onFirstPhaseCompleted(){
        // force build of action tree for all heroes
        buildActionTree();
    }

    public Double calculatePriority(Coordinate goal){
        Coordinate pawnCoordinate = currentlyPlannedPawn.getCoordinate();
        Tile goalTile = getBoard().getTileAt(goal);
        switch (goalTile.getType()) {
            case DISCOVERY: {
                if(getActionDelegator().areAllGoalsDiscovered()){
                    if(Config.PRINT_EVERYTHING){

                    }
                    System.out.println("All discovery goals have been discovered. No priority for discovery goals.");
                    return (double) 0;
                }
                // higher priority for closer discovery goals, give one penalty for each chunk (5 nodes)
                int distance = pathFinder.findDistance(pawnCoordinate, goal);
                double chunkPenalty = ChunkGenerator.estimateChunks(distance);


                return 5-chunkPenalty;
            }
            case GOAL_ITEM: {
                if(getActionDelegator().isFirstPhase() && !pawnCoordinate.equals(goal)){
                    return (double)1;
                }
                else{
                    // negative priority = will not be ever followed
                    return (double)-1;
                }
            }
            case GOAL_EXIT: {
                if(getActionDelegator().isFirstPhase() && !pawnCoordinate.equals(goal)){
                    return (double)-1;
                }
                else{
                    return (double)1;
                }
            }
            case TIMER: {
                // following the logarithmic function
                int timerPriority = getActionDelegator().getTimerPayoff();
                int distance = pathFinder.findDistance(pawnCoordinate, goal);
                double chunkPenalty = ChunkGenerator.estimateChunks(distance);

                return timerPriority-chunkPenalty;
            }
        };
        return (double)0;
    }

    public void endGame(){
        running = false;
        if(actionExecutionThread != null && actionExecutionThread.isAlive()){
            actionExecutionThread.interrupt();
        }
    }

    @Override
    public void doSomething() {
        System.out.println("Someone is asking me to do something!");
        ticksWaiting = 0;
        if(thinking){
            System.out.println("I am already thinking, no need to ask me again.");
            return;
        }
        thinking = true;
        System.out.println("I am going to do something");



        // look if I can do anything according to my plan
        Action bestAction = actionTree.bestAction();
        // while nothing is worth doing, re-build
        while(bestAction == null){
            buildActionTree(); // Rebuild the tree if no action is available (automatically switches color)
            bestAction = actionTree.bestAction();
        }

        // all colors is ordered in decreasing order of being moved in the past
        List<Color> allColors = new ArrayList<>(List.of(Color.PURPLE, Color.GREEN, Color.ORANGE, Color.YELLOW));
        for(Color color : otherPawnMoves){
            allColors.remove(color);
            allColors.addFirst(color);
        }

        // place the last moved in the beginning
        allColors.remove(currentlyPlannedPawn.getColor());
        allColors.addFirst(currentlyPlannedPawn.getColor());

        int index = 0;

        super.decreaseMemoryCapacity();


        while(index<allColors.size()){
            System.out.println("My best action is "+bestAction+" for pawn "+currentlyPlannedPawn.getColor());
            System.out.println("Is it performable (not occupied)? "+getActionDelegator().isPerformable(bestAction, currentlyPlannedPawn.getColor()));
            if(Config.PRINT_EVERYTHING){

            }
            if(getActionDelegator().isPerformable(bestAction, currentlyPlannedPawn.getColor())){
                if(canPerformAction(bestAction)){
                    attemptBestAction(bestAction);
                    thinking = false;
                    return;
                }
                else{
                    // someone wants me to do something, but I don't know what to do (not in my plan)
                    currentlyPlannedPawn = getActionDelegator().getPawnByColor(allColors.get(index));
                    System.out.println("Now going to consider pawn of color "+currentlyPlannedPawn.getColor());
                    buildActionTree();
                    index++;
                }
            }
            else{
                // my next best thing is not executable -> can I vortex?
                if(super.getActions().contains(Action.VORTEX)){
                    System.out.println("My best action "+bestAction+" is blocked, I will try to vortex the blocking pawn.");
                    tryToClearBlockingPawn(bestAction);
                    thinking = false;
                    return;
                }
                else{
                    currentlyPlannedPawn = getActionDelegator().getPawnByColor(allColors.get(index));
                    buildActionTree();
                    index++;
                }
            }
        }
        System.out.println("I cannot do anything useful right now. I'll just do something random.");
        getActionDelegator().performRandomAvailableActionFromActionSet(super.getActions());
        thinking = false;
    }

    @Override
    public void doSomethingPlaced(Player player){
        lastDoSomethingPlacedTimestamp = System.currentTimeMillis();
        thinking = false;
        ticksWaiting = 0;
//        if(player.getActions().contains(actionTree.bestAction()) || !getActionDelegator().isPerformable(actionTree.bestAction(), currentlyPlannedPawn.getColor())){
//            // someone already notified the player about the same thing i am waiting for - no need to do it twice
//            ticksWaiting = 0;
//        }
    }

    private void placeDoSomething(Action action){
        if(lastDoSomethingPlacedTimestamp + 4000 > System.currentTimeMillis()){
            // avoid spamming do something tokens
            return;
        }
        System.out.println(getName() + " is placing a Do Something token for action: " + action);
        getActionDelegator().placeDoSomething(action);
    }
}
