package org.game.model;

import java.util.List;

public class Player {
    private final List<Action> actions;
    private String name;

    public Player(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> getActions() {
        return actions;
    }
}
