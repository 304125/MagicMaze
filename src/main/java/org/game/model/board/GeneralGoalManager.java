package org.game.model.board;

import org.game.model.Color;
import org.game.model.Coordinate;

import java.util.List;

// singleton
public class GeneralGoalManager {
    private static GeneralGoalManager instance; // singleton
    List<PawnGoalManager> pawnGoalManagers;

    private GeneralGoalManager() {
        this.pawnGoalManagers = List.of(
            new PawnGoalManager(Color.ORANGE),
            new PawnGoalManager(Color.GREEN),
            new PawnGoalManager(Color.YELLOW),
            new PawnGoalManager(Color.PURPLE)
        );
    }

    public static GeneralGoalManager getInstance() {
        if (instance == null) {
            synchronized (GeneralGoalManager.class) {
                instance = new GeneralGoalManager();
            }
        }
        return instance;
    }

    public void reset() {
        instance = new GeneralGoalManager();
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
            pgm.removeTimer(timer);
        }
    }

    public boolean areAllItemGoalsDiscovered(){
        for (PawnGoalManager pawnGoalManager : pawnGoalManagers){
            if (pawnGoalManager.getItem() == null){
                return false;
            }
        }
        return true;
    }

    public boolean areAllExitGoalsDiscovered(){
        for (PawnGoalManager pawnGoalManager : pawnGoalManagers){
            if (pawnGoalManager.getExit() == null){
                return false;
            }
        }
        return true;
    }
}
