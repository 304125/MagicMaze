package org.game.model.AI;

import org.game.model.Coordinate;

public interface AIPlayerBehavior {
    void startGame();
    void endGame();
    void startActionExecution();
    float calculatePriority(Coordinate goal);
}
