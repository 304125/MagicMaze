package org.game.model.board;

import org.game.model.Color;
import org.game.model.Coordinate;
import org.game.utils.Config;

import java.util.ArrayList;
import java.util.List;

// class representing goals per hero (color)
public class PawnGoalManager {
    Color pawnColor;
    List<Coordinate> timers;
    List<Coordinate> discoveries;
    Coordinate exit;
    Coordinate item;

    public PawnGoalManager(Color pawnColor) {
        this.pawnColor = pawnColor;
        this.timers = new ArrayList<>();
        this.discoveries = new ArrayList<>();
        this.exit = null;
        this.item = null;
    }

    public void addTimer(Coordinate timer) {
        this.timers.add(timer);
        if(Config.PRINT_EVERYTHING){
            System.out.println("Timer for pawn " + pawnColor + " added at (" + timer.x() + "," + timer.y() + ")");
        }
    }

    public void addDiscovery(Coordinate discovery) {
        this.discoveries.add(discovery);
        if(Config.PRINT_EVERYTHING){
            System.out.println("Discovery for pawn " + pawnColor + " added at (" + discovery.x() + "," + discovery.y() + ")");
        }
    }

    public void setExit(Coordinate exit) {
        this.exit = exit;
        if(Config.PRINT_EVERYTHING){
            System.out.println("Exit for pawn " + pawnColor + " set at (" + exit.x() + "," + exit.y() + ")");
        }
    }

    public void setItem(Coordinate item) {
        this.item = item;
        if(Config.PRINT_EVERYTHING){
            System.out.println("Item for pawn " + pawnColor + " set at (" + item.x() + "," + item.y() + ")");
        }
    }

    public List<Coordinate> getTimers() {
        return timers;
    }

    public List<Coordinate> getDiscoveries() {
        return discoveries;
    }

    public Coordinate getExit() {
        return exit;
    }

    public Coordinate getItem() {
        return item;
    }

    public Color getPawnColor() {
        return pawnColor;
    }

    public void removeTimer(Coordinate timer) {
        this.timers.remove(timer);
    }

    public void removeDiscovery(Coordinate discovery) {
        this.discoveries.remove(discovery);
        if(Config.PRINT_EVERYTHING){
            System.out.println("Discovery at (" + discovery.x() + "," + discovery.y() + ") removed for pawn " + pawnColor);
        }
    }

    public List<Coordinate> getAllGoals(){
        List<Coordinate> allGoals = new ArrayList<>();
        allGoals.addAll(timers);
        allGoals.addAll(discoveries);
        if(exit != null){
            allGoals.add(exit);
        }
        if(item != null){
            allGoals.add(item);
        }
        return allGoals;
    }
}
