package org.game.model;

import java.util.List;

public class Player {
    private final List<Action> actions;
    private String name;

    public Player(List<Action> actions, String name) {
        this.actions = actions;
        this.name = name;
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getName() {
        return name;
    }
}
