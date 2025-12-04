package org.game.model.AI;

import org.game.model.Coordinate;

public interface AIPlayerBehavior {
    void startGame();
    void endGame();
    void startActionExecution();
    Double calculatePriority(Coordinate goal);
}
