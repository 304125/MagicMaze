package org.game.model.AI.PlayerType;

import org.game.model.*;
import org.game.model.AI.*;
import org.game.model.board.Board;
import org.game.model.board.GeneralGoalManager;
import org.game.utils.ActionDelegator;
import org.game.utils.Config;

import java.util.*;

import static org.game.model.ActionType.*;

public class AIPlayer extends Player  implements StateChangeListener, AIPlayerBehavior {
    private final AIPlayerType playerType;
    private Pawn currentlyPlannedPawn;
    private ActionTree actionTree;
    private final GeneralGoalManager generalGoalManager;
    private final PathFinder pathFinder;
    private Thread actionExecutionThread;
    private boolean isThreadSleeping = false;
    private final List<Color> otherPawnMoves = new ArrayList<>();
    private int ticksWaiting = 0;
    private boolean running = false;
    private boolean thinking = false;
    private boolean buildingTree = false;
    private boolean iWasLastToMove = false;
    private boolean updatingOtherPawnMoves = false;
    private int currentMemoryCapacity;
    private final Board board;
    private ActionDelegator actionDelegator;

    public AIPlayer(List<ActionType> actions, String name, Board board, AIPlayerType playerType) {
        super(actions, name);
        this.board = board;
        currentlyPlannedPawn = board.getRandomPawn();
        actionTree = new ActionTree();
        this.generalGoalManager = GeneralGoalManager.getInstance();
        this.pathFinder = new PathFinder(board.getTiles(), board);
        this.playerType = playerType;
        currentMemoryCapacity = playerType.getParameters().startingCapacity();
    }

    @Override
    public void startGame(){
        running = true;
        buildActionTree();
        startActionExecution();
    }

    public void setActionDelegator(ActionDelegator actionDelegator){
        this.actionDelegator = actionDelegator;
    }

    public ActionDelegator getActionDelegator(){
        return actionDelegator;
    }

    // builds action tree from scratch
    private void buildActionTree() {
        if(!running){
            return;
        }
        buildingTree = true;
        ticksWaiting = 0;
        if(!isThreadSleeping){
            try{
                isThreadSleeping = true;
                int sleepTime = (int) (1000/playerType.getParameters().processingRatio());
                Thread.sleep(sleepTime); // wait a bit to process
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
            System.out.println("Building action tree for pawn " + currentlyPlannedPawn.getColor() + " towards goals: " + goalCoordinates);
        }

        // consider all possible goals of that color of hero in ascending order of distance

        // order goalCoordinates by distance to lastMovedPawn
        Map<Coordinate, Integer> distanceMap = new HashMap<>();
        for (Coordinate goal : goalCoordinates) {
            int estimatedDistance = pathFinder.findDistance(currentlyPlannedPawn.getCoordinate(), goal, playerType.getParameters().heuristicType());
            distanceMap.put(goal, estimatedDistance);
        }
        if(Config.PRINT_EVERYTHING){
            System.out.println("Distance map from pawn " + currentlyPlannedPawn.getColor() + " at " + currentlyPlannedPawn.getCoordinate() + ": " + distanceMap);
        }

        // sort goalCoordinates by estimated distance from distanceMap
        goalCoordinates.sort(Comparator.comparingInt(distanceMap::get));

        // starting from the closest goal, find the shortest path
        for (Coordinate goal : goalCoordinates) {
            SearchPath path = pathFinder.findShortestPath(currentlyPlannedPawn.getCoordinate(), goal, currentlyPlannedPawn.getColor(), playerType.getParameters().heuristicType());
            currentChunkSize += ChunkGenerator.countChunks(path);
            if(currentChunkSize > currentMemoryCapacity){
                if(Config.PRINT_EVERYTHING){
                    System.out.println("Reached maximum chunk size limit while building action tree. Stopping further path additions.");
                }
            }
            float priority = calculatePriority(goal);
            actionTree.addRoute(path.getActions(), priority);
            if(Config.PRINT_EVERYTHING){
                System.out.println("Added path to goal " + goal + " with estimated distance " + distanceMap.get(goal) + " to action tree.");
            }
        }

        if(Config.PRINT_EVERYTHING) {
            System.out.println("Finished building action tree for pawn " + currentlyPlannedPawn.getColor() + ".");
        }

        // there is nothing to do for this color now -> re-do with another color
        if(actionTree.isEmpty() || !actionTree.areAnyLeafsPositivePriority()){
            if(Config.PRINT_EVERYTHING) {
                System.out.println("Action tree is empty or has no positive priority leafs for pawn " + currentlyPlannedPawn.getColor() + ". Re-planning for another pawn.");
            }
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

        if(Config.PRINT_EVERYTHING) {
            System.out.println("Planned for pawn of color: " + currentlyPlannedPawn.getColor());
            actionTree.printTree(getName(), super.getActions());
        }

        buildingTree = false;
    }

    @Override
    public void onPawnMoved(Pawn movedPawn, Action action) {
        if(playerType.getParameters().stressedByPlacingDoSomething()){
            // re-set capacity when someone else moved
            currentMemoryCapacity = playerType.getParameters().startingCapacity();
        }
        ticksWaiting = 0;
        iWasLastToMove = canPerformAction(action.getType());

        if(!isThreadSleeping && running){
            try{
                isThreadSleeping = true;
                int sleepTime = (int) (500/playerType.getParameters().processingRatio());
                Thread.sleep(sleepTime); // wait a bit to process
                isThreadSleeping = false;
            } catch (InterruptedException e) {
                System.out.println("On pawn moved sleep interrupted.");
                actionExecutionThread.interrupt();
            }
        }

        if(movedPawn.getColor().equals(currentlyPlannedPawn.getColor())) {
            boolean moved = actionTree.takeAction(action);
            if(Config.PRINT_EVERYTHING){
                actionTree.printTree(getName(), super.getActions());
            }
            if(actionTree.isEmpty()){
                // re-build tree, all actions used up
                buildActionTree();
            }
            if (!moved) {
                if(Config.PRINT_EVERYTHING) {
                    System.out.println("Action " + action + " taken by pawn " + movedPawn.getColor() + " was not in my (" + super.getName() + ") plan. Rebuilding action tree...");
                }
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
            // if that pawn has been moved more times than my blindness (and is not the pawn I have tree for), re-build
            if(otherPawnMoves.stream().filter(color -> color.equals(movedPawn.getColor())).count() >= playerType.getParameters().blindness()){
                currentlyPlannedPawn = movedPawn;
                // re-build tree, another pawn was moved
                buildActionTree();
            }
        }
    }

    @Override
    public void onDiscovered(Pawn pawn){
        // sleep for 7 seconds to process
        if(!isThreadSleeping && running){
            try{
                isThreadSleeping = true;
                int sleepTime = (int) (7000/playerType.getParameters().processingRatio());
                Thread.sleep(sleepTime); // wait a bit to process
                isThreadSleeping = false;
            } catch (InterruptedException e) {
                System.out.println("On discovered sleep interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        iWasLastToMove = canPerformAction(DISCOVER);

        if(pawn.getColor() == currentlyPlannedPawn.getColor()) {
            boolean moved = actionTree.takeAction(new Action(DISCOVER));
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
                    // if I was moving last, continue moving (go 3 left in once ex.)
                    if(!isThreadSleeping && running){
                        isThreadSleeping = true;
                        int sleepTime = (int) (700/playerType.getParameters().processingRatio());
                        Thread.sleep(sleepTime); // Wait for 1 second
                        isThreadSleeping = false;
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
                        if(Config.PRINT_EVERYTHING) {
                            System.out.println("No action is worth taking (" + currentlyPlannedPawn.getColor() + "). Rebuilding action tree...");
                        }
                        buildActionTree(); // Rebuild the tree if no action is available
                        if(!isThreadSleeping && running){
                            isThreadSleeping = true;
                            int sleepTime = (int) (3000/playerType.getParameters().processingRatio());
                            Thread.sleep(sleepTime); // Wait for 3 seconds
                            isThreadSleeping = false;
                        }
                        bestAction = actionTree.bestAction();
                    }
                    if(canPerformAction(bestAction.getType())){
                        if (actionTree.isEmpty()) {
                            if(Config.PRINT_EVERYTHING) {
                                System.out.println("No valid action to take. Rebuilding action tree...");
                            }
                            buildActionTree(); // Rebuild the tree if no action is available
                            if(!isThreadSleeping && running){
                                isThreadSleeping = true;
                                int sleepTime = (int) (3000/playerType.getParameters().processingRatio());
                                Thread.sleep(sleepTime); // Wait for 3 seconds
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
                            if(ticksWaiting >= playerType.getParameters().patience()){
                                ticksWaiting = 0;
                                if(getActionDelegator().isPerformable(bestAction, currentlyPlannedPawn.getColor(), playerType.getParameters().heuristicType())){
                                    placeDoSomething(bestAction.getType());
                                }
                                else{
                                    // only place vortex if first phase
                                    if(getActionDelegator().isFirstPhase()){
                                        // if the action is not performable, someone is blocking the pawn -> vortex
                                        placeDoSomething(VORTEX);
                                    }

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
            System.out.println(getName() + " is attempting to perform action: " + bestAction + " with pawn " + currentlyPlannedPawn.getColor());
        }

        switch (bestAction.getType()){
            case MOVE_EAST, MOVE_NORTH, MOVE_SOUTH, MOVE_WEST, ESCALATOR -> moved = getActionDelegator().movePawn(currentlyPlannedPawn.getColor(), bestAction);
            case DISCOVER -> getActionDelegator().discoverRandomCard(currentlyPlannedPawn.getColor());
            case VORTEX -> moved = getActionDelegator().vortexPawn(currentlyPlannedPawn.getColor(), bestAction.getVortexCoordinate(), playerType.getParameters().heuristicType());
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
        Pawn initialPawn = currentlyPlannedPawn;
        Pawn blockingPawn = getActionDelegator().getBlockingPawn(currentlyPlannedPawn, bestAction, bestAction.getVortexCoordinate());
        if(blockingPawn == null){
            if(Config.PRINT_EVERYTHING){
                System.out.println("Hmmmm something weird happened");
            }
            // the blocking pawn has since moved or the tree was outdated (best action was not valid)
            buildActionTree();
            ticksWaiting++;
        }
        else{
            if(Config.PRINT_EVERYTHING){
                System.out.println("I "+ super.getName()+" can see that pawn "+blockingPawn.getColor()+" is blocking best action "+bestAction+" for pawn "+currentlyPlannedPawn.getColor());
            }
            if(getActionDelegator().isFirstPhase()){
                // schedule a plan for the blocking pawn to move away
                // just vortex that pawn to the closest vortex
                // own color's vortex is never blocking
                if(canPerformAction(VORTEX)){
                    // plan for the blocking pawn to be vortexed to the closest goal that makes sense
                    currentlyPlannedPawn = blockingPawn;
                    buildActionTree();
                    Action blockingBestAction = actionTree.bestAction();
                    if(blockingBestAction != null && blockingBestAction.getType() == VORTEX){
                        // can vortex the blocking pawn away
                        if(Config.PRINT_EVERYTHING) {
                            System.out.println(getName() + " is vortexing blocking pawn " + blockingPawn.getColor() + " away to clear the path for pawn " + initialPawn.getColor());
                        }
                        getActionDelegator().vortexPawn(blockingPawn.getColor(), blockingBestAction.getVortexCoordinate(), playerType.getParameters().heuristicType());
                    }
                    else{
                        // if the best action does not result in vortex, just vortex to closest (to put it away)
                        getActionDelegator().vortexToClosest(blockingPawn.getColor(), playerType.getParameters().heuristicType());
                    }
                    // after vortex, come back to initial pawn and continue
                    currentlyPlannedPawn = initialPawn;
                    attemptBestAction(bestAction);
                    return;
                }
            }
            // either not first phase or cannot vortex -> try to perform any action for the blocking pawn to move it away
            List<ActionType> applicableActions = getActionDelegator().getApplicableActionsForPawn(blockingPawn.getColor());
            // try to perform any of the actions in the set
            boolean canDoAnything = false;
            for(ActionType actionICanPerform : super.getActions()){
                if(applicableActions.contains(actionICanPerform)){
                    Action action = new Action(actionICanPerform);
                    boolean moved = getActionDelegator().movePawn(blockingPawn.getColor(), action);
                    if(moved){
                        // come back to the initial pawn
                        currentlyPlannedPawn = initialPawn;
                        attemptBestAction(bestAction);
                    }
                    canDoAnything = true;
                    break;
                }
            }
            if(!canDoAnything){
                // put do something on that action
                if(ticksWaiting >= playerType.getParameters().patience()){
                    placeDoSomething(applicableActions.getFirst());
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

    public float calculatePriority(Coordinate goal){
        Coordinate pawnCoordinate = currentlyPlannedPawn.getCoordinate();
        Tile goalTile = board.getTileAt(goal);
        switch (goalTile.getType()) {
            case DISCOVERY: {
                if(playerType.getParameters().discoverUntilGoals()){
                    if(getActionDelegator().areAllGoalsDiscovered()){
                        if(Config.PRINT_EVERYTHING){
                            System.out.println("All discovery goals have been discovered. No priority for discovery goals.");
                        }
                        return 0;
                    }
                }
                else{
                    // discover until all tiles are discovered
                    if(getActionDelegator().areAllTilesDiscovered()){
                        if(Config.PRINT_EVERYTHING){
                            System.out.println("All tiles have been discovered. No priority for discovery goals.");
                        }
                        return 0;
                    }
                }
                // higher priority for closer discovery goals, give one penalty for each chunk (5 nodes)
                int distance = pathFinder.findDistance(pawnCoordinate, goal, playerType.getParameters().heuristicType());
                float chunkPenalty = ChunkGenerator.estimateChunks(distance);


                return 5-chunkPenalty;
            }
            case GOAL_ITEM: {
                if(getActionDelegator().isFirstPhase() && !pawnCoordinate.equals(goal)){
                    return 1;
                }
                else{
                    // negative priority = will not be ever followed
                    return -1;
                }
            }
            case GOAL_EXIT: {
                if(getActionDelegator().isFirstPhase() || pawnCoordinate.equals(goal)){
                    return -1;
                }
                else{
                    return 1;
                }
            }
            case TIMER: {
                // following the logarithmic function
                int timerPriority = getActionDelegator().getTimerPayoff(playerType.getParameters().timerLogBase());
                int distance = pathFinder.findDistance(pawnCoordinate, goal, playerType.getParameters().heuristicType());
                float chunkPenalty = ChunkGenerator.estimateChunks(distance);

                // if I am looking at the timer option, I should get stressed if it affects me
                if(playerType.getParameters().stressByLowTime()){
                    // stress by reducing memory capacity
                    currentMemoryCapacity = currentMemoryCapacity - timerPriority;
                }

                return timerPriority-chunkPenalty;
            }
        }
        return 0;
    }

    public void endGame(){
        running = false;
        if(actionExecutionThread != null && actionExecutionThread.isAlive()){
            actionExecutionThread.interrupt();
        }
    }

    @Override
    public void doSomething() {
        if(Config.PRINT_EVERYTHING) {
            System.out.println("Someone is asking me to do something!");
        }
        ticksWaiting = 0;
        if(thinking){
            if(Config.PRINT_EVERYTHING) {
                System.out.println("I am already thinking, no need to ask me again.");
            }
            return;
        }
        thinking = true;

        if(playerType.getParameters().stressedByDoSomething()){
            currentMemoryCapacity--;
        }

        // look if I can do anything according to my plan
        buildActionTree();
        Action bestAction = actionTree.bestAction();
        if(Config.PRINT_EVERYTHING) {
            System.out.println("My best action according to the first plan is " + bestAction + " for pawn " + currentlyPlannedPawn.getColor());
        }
        // while nothing is worth doing, re-build
        while(bestAction == null){
            if(Config.PRINT_EVERYTHING) {
                System.out.println("No action is worth taking (" + currentlyPlannedPawn.getColor() + "). Rebuilding action tree...");
            }
            buildActionTree(); // Rebuild the tree if no action is available (automatically switches color)
            bestAction = actionTree.bestAction();
        }

        // all colors are ordered in decreasing order of being moved in the past
        List<Color> allColors = new ArrayList<>(List.of(Color.PURPLE, Color.GREEN, Color.ORANGE, Color.YELLOW));
        for(Color color : otherPawnMoves){
            allColors.remove(color);
            allColors.addFirst(color);
        }

        // place the last moved in the beginning
        allColors.remove(currentlyPlannedPawn.getColor());
        allColors.addFirst(currentlyPlannedPawn.getColor());

        int index = 0;

        while(index <= allColors.size()){
            if(Config.PRINT_EVERYTHING) {
                System.out.println("My best action is " + bestAction + " for pawn " + currentlyPlannedPawn.getColor());
                System.out.println("Is it performable (not occupied)? " + getActionDelegator().isPerformable(bestAction, currentlyPlannedPawn.getColor(), playerType.getParameters().heuristicType()));
            }
            if(getActionDelegator().isPerformable(bestAction, currentlyPlannedPawn.getColor(), playerType.getParameters().heuristicType())){
                if(canPerformAction(bestAction.getType())){
                    attemptBestAction(bestAction);
                    thinking = false;
                    if(playerType.getParameters().stressedByDoSomething()){
                        currentMemoryCapacity++;
                    }
                    return;
                }
                else{
                    // someone wants me to do something, but I don't know what to do (not in my plan)
                    if(index == allColors.size()){
                        // found nothing useful
                        if(playerType.getParameters().stressedByDoSomething()){
                            currentMemoryCapacity++;
                        }
                        break;
                    }
                    currentlyPlannedPawn = getActionDelegator().getPawnByColor(allColors.get(index));
                    if(Config.PRINT_EVERYTHING) {
                        System.out.println("Now going to consider pawn of color " + currentlyPlannedPawn.getColor());
                    }
                    buildActionTree();
                    bestAction = actionTree.bestAction();
                    index++;
                }
            }
            else{
                // my next best thing is not executable -> can I vortex?
                if(super.getActions().contains(VORTEX) && getActionDelegator().isFirstPhase()){
                    if(Config.PRINT_EVERYTHING) {
                        System.out.println("My best action " + bestAction + " is blocked, I will try to vortex the blocking pawn.");
                    }
                    tryToClearBlockingPawn(bestAction);
                    thinking = false;
                    if(playerType.getParameters().stressedByDoSomething()){
                        currentMemoryCapacity++;
                    }
                    return;
                }
                else{
                    if(index == allColors.size()){
                        // found nothing useful
                        if(playerType.getParameters().stressedByDoSomething()){
                            currentMemoryCapacity++;
                        }
                        break;
                    }
                    currentlyPlannedPawn = getActionDelegator().getPawnByColor(allColors.get(index));
                    buildActionTree();
                    bestAction = actionTree.bestAction();
                    index++;
                }
            }
        }

        boolean canDo = false;
        if(playerType.getParameters().stubborn()){
            if(Config.PRINT_EVERYTHING) {
                System.out.println("I am stubborn and prefer my own strategy.");
            }
        }
        else{
            if(Config.PRINT_EVERYTHING) {
                System.out.println("I cannot do anything useful right now. I'll just do something random.");
            }
            canDo = getActionDelegator().performRandomAvailableActionFromActionSet(super.getActions(), playerType.getParameters().heuristicType());
        }

        if(!canDo){
            if(Config.PRINT_EVERYTHING) {
                System.out.println("I will instead tell someone else to do something.");
            }
            while(index <= allColors.size()){
                currentlyPlannedPawn = getActionDelegator().getPawnByColor(allColors.get(index-1));
                buildActionTree();
                bestAction = actionTree.bestAction();
                if(bestAction != null){
                    placeDoSomething(bestAction.getType());
                    break;
                }
                index++;
            }
        }
        thinking = false;
    }

    @Override
    public void doSomethingPlaced(Player player){
        thinking = false;
        ticksWaiting = 0;
    }

    private void placeDoSomething(ActionType action){
        ticksWaiting = 0;
        if(!playerType.getParameters().alwaysPlaceDoSomething()){
            // flip a coin whether to place or not
            Random rand = new Random();
            boolean place = rand.nextBoolean();
            if(!place){
                if(Config.PRINT_EVERYTHING) {
                    System.out.println(getName() + " decided not to place a Do Something token.");
                }
                return;
            }
        }
        if(playerType.getParameters().stressedByPlacingDoSomething()){
            currentMemoryCapacity--;
        }
        if(Config.PRINT_EVERYTHING) {
            System.out.println(getName() + " is placing a Do Something token for action: " + action);
        }
        getActionDelegator().placeDoSomething(action);
    }

    public void onTimerFlipped(int timeLeft){
        // re-set capacity (nobody should be stressed after timer flip)
        currentMemoryCapacity = playerType.getParameters().startingCapacity();
        // delay for 3 seconds
        try{
            if(!isThreadSleeping && running){
                isThreadSleeping = true;
                Thread.sleep(3000);
                isThreadSleeping = false;
            }
        } catch (InterruptedException e) {
            System.out.println("On timer flipped sleep interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}
