package org.game.model.board;

import org.game.model.Color;
import org.game.model.Coordinate;

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
        System.out.println("Timer for pawn " + pawnColor + " added at (" + timer.getX() + "," + timer.getY() + ")");
    }

    public void addDiscovery(Coordinate discovery) {
        this.discoveries.add(discovery);
        System.out.println("Discovery for pawn " + pawnColor + " added at (" + discovery.getX() + "," + discovery.getY() + ")");
    }

    public void setExit(Coordinate exit) {
        this.exit = exit;
        System.out.println("Exit for pawn " + pawnColor + " set at (" + exit.getX() + "," + exit.getY() + ")");
    }

    public void setItem(Coordinate item) {
        this.item = item;
        System.out.println("Item for pawn " + pawnColor + " set at (" + item.getX() + "," + item.getY() + ")");
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
        System.out.println("Discovery at (" + discovery.getX() + "," + discovery.getY() + ") removed for pawn " + pawnColor);
    }
}
