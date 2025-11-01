package org.game.model;

import org.game.model.AI.SearchPath;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class PathFinder {
    private final Tile[][] grid;

    public PathFinder(Tile[][] grid) {
        this.grid = grid;
    }

    public SearchPath findShortestPath(int startX, int startY, int endX, int endY) {
        int rows = grid.length;
        int cols = grid[0].length;

        // Priority queue for A* search
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<String> closedSet = new HashSet<>();
        SearchPath searchPath = new SearchPath();

        // Start node
        Node startNode = new Node(startX, startY, 0, heuristicManhattan(startX, startY, endX, endY), null, null);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = current.x + "," + current.y;

            // Goal check
            if (current.x == endX && current.y == endY) {
                reconstructPath(searchPath, current);
                return searchPath; // Return the cost of the shortest path
            }

            closedSet.add(currentKey);

            // Explore neighbors
            for (Action action : Action.values()) {
                int newX = current.x + action.dx;
                int newY = current.y + action.dy;

                if (isValid(newX, newY, rows, cols, grid) && !closedSet.contains(newX + "," + newY) && hasNoWall(grid[current.x][current.y], grid[newX][newY], action)) {
                    int newG = current.g + 1; // Cost to move to the neighbor
                    int newH = heuristicManhattan(newX, newY, endX, endY);
                    Node neighbor = new Node(newX, newY, newG, newH, current, action);

                    openSet.add(neighbor);
                }
            }
        }

        return null; // No path is found
    }

    private boolean isValid(int x, int y, int rows, int cols, Tile[][] grid) {
        return x >= 0 && x < rows && y >= 0 && y < cols && grid[x][y] != null;
    }

    private boolean hasNoWall(Tile fromTile, Tile toTile, Action action) {
        switch (action) {
            case MOVE_NORTH:
                return !fromTile.hasWallUp() && !toTile.hasWallDown();
            case MOVE_SOUTH:
                return !fromTile.hasWallDown() && !toTile.hasWallUp();
            case MOVE_WEST:
                return !fromTile.hasWallLeft() && !toTile.hasWallRight();
            case MOVE_EAST:
                return !fromTile.hasWallRight() && !toTile.hasWallLeft();
            default:
                return false;
        }
    }

    public int heuristicManhattan(int aX, int aY, int bX, int bY) {
        return Math.abs(aX - bX) + Math.abs(aY - bY);
    }

    private void reconstructPath(SearchPath searchTree, Node goalNode) {
        Node current = goalNode;
        while (current != null) {
            searchTree.addNode(current.x, current.y, current.action);
            current = current.parent;
        }
    }

    private static class Node {
        int x, y, g, f;
        Node parent;
        Action action;

        Node(int x, int y, int g, int h, Node parent, Action action) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = g + h;
            this.parent = parent;
            this.action = action;
        }
    }

    public enum Action {
        MOVE_NORTH(-1, 0),
        MOVE_SOUTH(1, 0),
        MOVE_WEST(0, -1),
        MOVE_EAST(0, 1);

        final int dx, dy;

        Action(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }
}
