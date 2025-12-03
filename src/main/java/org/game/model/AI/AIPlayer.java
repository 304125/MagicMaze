package org.game.model.AI;

import org.game.model.Action;
import org.game.model.board.Board;
import org.game.model.Player;
import org.game.utils.ActionDelegator;

import java.util.List;
import java.util.Random;

public abstract class AIPlayer extends Player  implements StateChangeListener, AIPlayerBehavior{
    private final Board board;
    private ActionDelegator actionDelegator;
    private int currentMemoryCapacity;
    private int blindness;
    private int patience;

    public AIPlayer(List<Action> actions, String name, Board board) {
        super(actions, name);
        this.board = board;
        currentMemoryCapacity = ChunkGenerator.generateChunkSize();
        Random random = new Random();
        // 0, 1, 2, 3
        blindness = random.nextInt(2);
        patience = random.nextInt(5,10);
    }

    public Board getBoard() {
        return board;
    }

    public void setActionDelegator(ActionDelegator actionDelegator){
        this.actionDelegator = actionDelegator;
    }

    public ActionDelegator getActionDelegator(){
        return actionDelegator;
    }

    public int getCurrentMemoryCapacity(){
        return currentMemoryCapacity;
    }

    public void decreaseMemoryCapacity(){
        currentMemoryCapacity--;
    }

    public int getBlindness(){
        return blindness;
    }

    public int getPatience(){
        return patience;
    }
}
