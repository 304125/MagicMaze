package org.game.model.AI;

import org.game.model.Action;
import org.game.model.board.Board;
import org.game.model.Player;

import java.util.List;

public abstract class AIPlayer extends Player {
    private Board board;

    public AIPlayer(List<Action> actions, String name, Board board) {
        super(actions, name);
        this.board = board;
        System.out.println("I am AI, beep boop.");
    }

    public Board getBoard() {
        return board;
    }
}
