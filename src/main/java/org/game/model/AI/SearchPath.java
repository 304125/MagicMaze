package org.game.model.AI;

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

    public static class Node {
        private final int x, y;
        private final Object action;

        public Node(int x, int y, Object action) {
            this.x = x;
            this.y = y;
            this.action = action;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Object getAction() {
            return action;
        }
    }
}
