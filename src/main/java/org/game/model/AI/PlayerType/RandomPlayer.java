package org.game.model.AI.PlayerType;

import org.game.model.ActionType;
import org.game.model.Player;
import org.game.utils.ActionDelegator;

import java.util.List;

public class RandomPlayer extends Player {
    private ActionDelegator actionDelegator;
    private final AIPlayerType playerType;
    private boolean running = true;
    Thread actionExecutionThread;

    public RandomPlayer(List<ActionType> actions, String name, AIPlayerType playerType) {
        super(actions, name);
        this.playerType = playerType;
    }

    public void setActionDelegator(ActionDelegator actionDelegator){
        this.actionDelegator = actionDelegator;
    }

    private void makeRandomMove() {
        actionDelegator.performRandomAvailableActionFromActionSet(super.getActions(), playerType.getParameters().heuristicType());
    }

    public void startGame(){
        running = true;
        actionExecutionThread = new Thread(() -> {
            System.out.println("RandomPlayer " + super.getName() + " starting execution thread.");
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Sleep for a random time between 3 and 5 seconds
                    long sleepTimeMillis = 3000 + (long)(Math.random() * 2000);
                    Thread.sleep(sleepTimeMillis);
                } catch (InterruptedException e) {
                    System.out.println("RandomPlayer " + super.getName() + " interrupted, stopping execution thread.");
                }
                makeRandomMove();
            }
        });
        actionExecutionThread.setDaemon(true); // Optional: Set as daemon thread
        actionExecutionThread.start();
    }

    public void endGame(){
        running = false;
        if(actionExecutionThread != null && actionExecutionThread.isAlive()){
            actionExecutionThread.interrupt();
        }
    }
}
