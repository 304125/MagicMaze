package org.game.model.AI;

import org.game.model.Action;
import org.game.model.Board;
import org.game.model.Pawn;

import java.util.List;

public class OneHeroPlayer extends AIPlayer{
    private Pawn lastMovedPawn;

    public OneHeroPlayer(List<Action> actions, Board board) {
        super(actions, board);
        lastMovedPawn = board.getRandomPawn();
    }

    public Pawn getLastMovedPawn() {
        return lastMovedPawn;
    }
}
