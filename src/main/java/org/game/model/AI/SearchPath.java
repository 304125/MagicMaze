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
            if (node.action() instanceof Action) {
                actions.add((Action) node.action());
            }
        }
        return actions;
    }
}
