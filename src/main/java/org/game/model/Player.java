package org.game.model;

import java.util.List;

public class Player {
    List<Action> actions;
    String name;

    public Player(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> getActions() {
        return actions;
    }
}
