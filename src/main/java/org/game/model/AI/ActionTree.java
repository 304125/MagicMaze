package org.game.model.AI;

import org.game.model.Action;

import java.util.*;

public class ActionTree {
    private Node root;

    public ActionTree() {
        this.root = new Node();
    }

    /** Represents an edge between nodes */
    private static class ActionEdge {
        Action action;
        Node childNode;

        ActionEdge(Action action, Node childNode) {
            this.action = action;
            this.childNode = childNode;
        }
    }

    /** Represents a node in the tree */
    private static class Node {
        Map<Action, ActionEdge> edges = new HashMap<>();
        Integer priority = null; // only for leaves
    }

    /** Add a route of actions top-to-bottom */
    public void addRoute(List<Action> actions, int priority) {
        Node current = root;
        for (Action action : actions) {
            ActionEdge edge = current.edges.get(action);
            // if the current node doesn't have this action edge, create it
            if (edge == null) {
                Node child = new Node();
                edge = new ActionEdge(action, child);
                current.edges.put(action, edge);
            }
            // if the edge exists, move down to the child node
            current = edge.childNode;
        }
        // assign priority to the leaf node
        current.priority = priority;
    }

    /** Find a node by a sequence of actions from root */
    private Node findNode(List<Action> actions) {
        Node current = root;
        for (Action action : actions) {
            ActionEdge edge = current.edges.get(action);
            if (edge == null) return null;
            current = edge.childNode;
        }
        return current;
    }

    /** Take an action: move root down if action exists */
    public boolean takeAction(Action action) {
        ActionEdge edge = root.edges.get(action);
        if (edge == null) return false; // invalid action
        root = edge.childNode; // move root
        return true;
    }

    public boolean takeBestAction(){
        Action bestAction = bestAction();
        if(bestAction == null){
            return false;
        }
        return takeAction(bestAction);
    }

    /** Find the child of root whose subtree has the highest-priority leaf */
    public Action bestAction() {
        Action bestAction = null;
        int bestPriority = Integer.MIN_VALUE;

        for (Map.Entry<Action, ActionEdge> entry : root.edges.entrySet()) {
            int subtreeMax = maxPriority(entry.getValue().childNode);
            if (subtreeMax > bestPriority) {
                bestPriority = subtreeMax;
                bestAction = entry.getKey();
            }
        }

        return bestAction;
    }

    /** Recursively find maximum priority (the higher, the better) in subtree */
    private int maxPriority(Node node) {
        int max = (node.priority != null) ? node.priority : Integer.MIN_VALUE;
        for (ActionEdge edge : node.edges.values()) {
            max = Math.max(max, maxPriority(edge.childNode));
        }
        return max;
    }

    /** Print the tree */
    public void printTree(String playerName) {
        System.out.println("Action Tree for " + playerName + ":");
        printTree(root, "", "");
    }

    private void printTree(Node node, String prefix, String actionLabel) {
        String label = actionLabel.isEmpty() ? "ROOT" : actionLabel;
        String priorityStr = (node.priority != null) ? " (P=" + node.priority + ")" : "";
        System.out.println(prefix + label + priorityStr);

        for (ActionEdge edge : node.edges.values()) {
            printTree(edge.childNode, prefix + "  ", edge.action.toString());
        }
    }

    public int getTotalNumberOfEdges(){
        return countEdges(root);
    }

    // recurrently count edges
    private int countEdges(Node node){
        int count = node.edges.size();
        for(ActionEdge edge : node.edges.values()){
            count += countEdges(edge.childNode);
        }
        return count;
    }

    public boolean isEmpty(){
        return root.edges.isEmpty();
    }
}
