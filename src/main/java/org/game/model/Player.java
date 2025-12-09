package org.game.model;

import java.util.List;

public class Player {
    private final List<ActionType> actions;
    private final String name;

    public Player(List<ActionType> actions, String name) {
        this.actions = actions;
        this.name = name;
    }

    public List<ActionType> getActions() {
        return actions;
    }

    public String getName() {
        return name;
    }

    public boolean canPerformAction(ActionType action) {
        return actions.contains(action);
    }

    public void doSomething(){
        System.out.println("Do something!");
    }

    public void doSomethingPlaced(Player player){
        System.out.println("Do something was placed in front of player "+player.getName());
    }
}
