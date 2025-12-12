package org.game.model.AI;

import org.game.model.Action;
import org.game.model.ActionType;
import org.game.model.Coordinate;
import org.game.utils.Config;

import java.util.*;

import static org.game.model.ActionType.*;

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
        Double priority = null; // only for leaves
    }

    /** Add a route of actions top-to-bottom */
    public void addRoute(List<Action> actions, double priority) {
        Node current = root;
        for (Action action : actions) {
            ActionEdge edge = current.edges.get(action);
            ActionEdge newEdge = null;
            // if the current node doesn't have this action edge, create it
            if (edge == null) {
                Node child = new Node();
                edge = new ActionEdge(action, child);
                if(action.getType() == VORTEX){
                    if(Config.PRINT_EVERYTHING) {
                        System.out.println("Adding VORTEX action to tree at coordinate: " + action.getVortexCoordinate());
                    }
                }
                current.edges.put(action, edge);
            }
            else if(edge.action.getType().equals(VORTEX) && action.getType().equals(VORTEX)){
                // both are Vortex, check if they go to the same vortex
                Coordinate vortex1 = edge.action.getVortexCoordinate();
                Coordinate vortex2 = action.getVortexCoordinate();
                if(!vortex1.equals(vortex2)){
                    // if both are vortexes but not the same coordinate, create a new edge
                    Node child = new Node();
                    newEdge = new ActionEdge(action, child);
                    current.edges.put(action, newEdge);
                }
            }
            // if the edge exists, move down to the child node
            if(newEdge == null){
                current = edge.childNode;
            }
            else{
                current = newEdge.childNode;
            }
        }
        // assign priority to the leaf node
        current.priority = priority;
    }

    /** Take an action: move root down if action exists */
    public boolean takeAction(Action action) {
        ActionEdge edge = root.edges.get(action);
        if (edge == null) return false; // invalid action
        root = edge.childNode; // move root
        return true;
    }

    /** Find the child of root whose subtree has the highest-priority leaf */
    public Action bestAction() {
        Action bestAction = null;
        Double bestPriority = -1.0;

        for (Map.Entry<Action, ActionEdge> entry : root.edges.entrySet()) {
            Double subtreeMax = maxPriority(entry.getValue().childNode);
            if (subtreeMax > bestPriority) {
                bestPriority = subtreeMax;
                bestAction = entry.getKey();
            }
        }

        if(bestPriority > 0){
            if(Config.PRINT_EVERYTHING) {
                System.out.println("Max priority: " + bestPriority + " by action: " + bestAction);
            }
            return bestAction;
        }

        return null;
    }

    /** Recursively find maximum priority (the higher, the better) in subtree */
    private Double maxPriority(Node node) {
        double max = (node.priority != null) ? node.priority : -Double.MAX_VALUE;
        for (ActionEdge edge : node.edges.values()) {
            max = Math.max(max, maxPriority(edge.childNode));
        }
        return max;
    }

    /** Print the tree */
    public void printTree(String playerName, List<ActionType> actions) {
        System.out.println("Action Tree for " + playerName + "("+actions+") :");
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

    public boolean areAnyLeafsPositivePriority(){
        return maxPriority(root)>0;
    }
}
