package org.game.model.AI;

import org.game.model.Action;
import org.game.model.board.Board;
import org.game.model.Player;

import java.util.List;

public abstract class AIPlayer extends Player  implements PawnMoveListener{
    private final Board board;

    public AIPlayer(List<Action> actions, String name, Board board) {
        super(actions, name);
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }
}
