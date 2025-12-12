package org.game.model.AI;

import org.game.model.Action;
import org.game.model.Pawn;

public interface StateChangeListener {
    void onPawnMoved(Pawn movedPawn, Action action);
    void onDiscovered(Pawn pawn);
    void onFirstPhaseCompleted();
    void onTimerFlipped(int timeLeft);
}
