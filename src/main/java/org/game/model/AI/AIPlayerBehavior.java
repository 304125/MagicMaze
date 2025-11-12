package org.game.model.AI;

import org.game.model.Action;
import org.game.model.Coordinate;

public interface AIPlayerBehavior {
    void startGame();
    void startActionExecution();
    int calculatePriority(Coordinate goal);
}
