package org.game.model.AI;

import org.game.model.Action;
import org.game.model.board.Board;
import org.game.model.Player;

import java.util.List;

public abstract class AIPlayer extends Player {
    private Board board;

    public AIPlayer(List<Action> actions, Board board) {
        super(actions);
        this.board = board;
        System.out.println("I am AI, beep boop.");
    }


}
