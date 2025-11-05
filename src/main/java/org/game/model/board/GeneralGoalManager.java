package org.game.model.board;

import org.game.model.Color;
import org.game.model.Coordinate;

import java.util.List;

public class GeneralGoalManager {
    List<PawnGoalManager> pawnGoalManagers;

    public GeneralGoalManager() {
        this.pawnGoalManagers = List.of(
            new PawnGoalManager(Color.ORANGE),
            new PawnGoalManager(Color.GREEN),
            new PawnGoalManager(Color.YELLOW),
            new PawnGoalManager(Color.PURPLE)
        );
    }

    public PawnGoalManager getPawnGoalManager(Color color) {
        for (PawnGoalManager pgm : pawnGoalManagers) {
            if (pgm.pawnColor == color) {
                return pgm;
            }
        }
        return null;
    }

    public void addTimerToAllPawns(Coordinate timer) {
        for (PawnGoalManager pgm : pawnGoalManagers) {
            pgm.addTimer(timer);
        }
    }

    public void removeTimerFromAllPawns(Coordinate timer) {
        for (PawnGoalManager pgm : pawnGoalManagers) {
            pgm.getTimers().remove(timer);
        }
    }
}
