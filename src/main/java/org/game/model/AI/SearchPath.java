package org.game.model.AI;

import org.game.model.Action;

import java.util.ArrayList;
import java.util.List;

public class SearchPath {
    private final List<Node> nodes = new ArrayList<>();

    public void addNode(int x, int y, Object action) {
        nodes.add(new Node(x, y, action));
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int length() {
        return nodes.size();
    }

    public record Node(int x, int y, Object action) {
    }

    public List<Action> getActions() {
        List<Action> actions = new ArrayList<>();
        for (Node node : nodes) {
            Object actionObj = node.action();
            // if not null, cast to Action
            if(actionObj != null){
                PathFinder.Action action = (PathFinder.Action) actionObj;
                // PathFinder.Action and model.Action are different!
                switch (action) {
                    case MOVE_NORTH -> {
                        actions.add(Action.MOVE_NORTH);
                    }
                    case MOVE_SOUTH -> {
                        actions.add(Action.MOVE_SOUTH);
                    }
                    case MOVE_WEST -> {
                        actions.add(Action.MOVE_WEST);
                    }
                    case MOVE_EAST -> {
                        actions.add(Action.MOVE_EAST);
                    }
                    case ESCALATOR -> {
                        actions.add(Action.ESCALATOR);
                    }
                }
            }
        }
        return actions;
    }
}
