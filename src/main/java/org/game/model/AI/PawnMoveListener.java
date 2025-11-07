package org.game.model.AI;

import org.game.model.Action;
import org.game.model.Pawn;

public interface PawnMoveListener {
    void onPawnMoved(Pawn movedPawn, Action action);
}
