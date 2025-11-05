package org.game.model.AI;

import org.game.model.Coordinate;
import org.game.model.board.PawnManager;
import org.game.model.Tile;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class PathFinder {
    private final Tile[][] grid;

    public PathFinder(Tile[][] grid) {
        this.grid = grid;
    }

    public SearchPath findShortestPath(Coordinate coordinateStart, Coordinate coordinateEnd) {
        int rows = grid.length;
        int cols = grid[0].length;

        // Priority queue for A* search
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<String> closedSet = new HashSet<>();
        SearchPath searchPath = new SearchPath();

        // Start node
        Node startNode = new Node(coordinateStart, 0, heuristicManhattan(coordinateStart, coordinateEnd), null, null);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = current.getX() + "," + current.getY();

            // Goal check
            if (current.getX() == coordinateEnd.getX() && current.getY() == coordinateEnd.getY()) {
                reconstructPath(searchPath, current);
                return searchPath; // Return the cost of the shortest path
            }

            closedSet.add(currentKey);

            // Check for escalators
            Tile currentTile = grid[current.getX()][current.getY()];
            if (currentTile.hasEscalator()) {
                Coordinate coordinateOtherSideOfEscalator = PawnManager.getOtherSideOfEscalator(new Coordinate(current.getX(), current.getY()));
                if(coordinateOtherSideOfEscalator == null) {
                    continue; // No valid escalator destination
                }

                if (isValid(coordinateOtherSideOfEscalator, rows, cols, grid) &&
                        !closedSet.contains(coordinateOtherSideOfEscalator.getX() + "," + coordinateOtherSideOfEscalator.getY())) {
                    int newG = current.g + 1; // Cost to move to the escalator destination
                    int newH = heuristicManhattan(coordinateOtherSideOfEscalator, coordinateEnd);
                    Node escalatorNode = new Node(coordinateOtherSideOfEscalator, newG, newH, current, Action.ESCALATOR);
                    openSet.add(escalatorNode);
                }
            }

            // Explore neighbors
            for (Action action : Action.values()) {
                Coordinate coordinateNew = new Coordinate(current.getX() + action.dx, current.getY() + action.dy);

                if (isValid(coordinateNew, rows, cols, grid) &&
                        !closedSet.contains(coordinateNew.getX() + "," + coordinateNew.getY()) &&
                        hasNoWall(grid[current.getX()][current.getY()], grid[coordinateNew.getX()][coordinateNew.getY()], action)) {
                    int newG = current.g + 1; // Cost to move to the neighbor
                    int newH = heuristicManhattan(coordinateNew, coordinateEnd);
                    Node neighbor = new Node(coordinateNew, newG, newH, current, action);

                    openSet.add(neighbor);
                }
            }
        }

        return null; // No path is found
    }

    private boolean isValid(Coordinate coordinate, int rows, int cols, Tile[][] grid) {
        return coordinate.getX() >= 0 && coordinate.getX() < rows && coordinate.getY() >= 0 && coordinate.getY() < cols && grid[coordinate.getX()][coordinate.getY()] != null;
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

    public int heuristicManhattan(Coordinate coordinateA, Coordinate coordinateB) {
        return Math.abs(coordinateA.getX() - coordinateB.getX()) + Math.abs(coordinateA.getY() - coordinateB.getY());
    }

    private void reconstructPath(SearchPath searchTree, Node goalNode) {
        Node current = goalNode;
        while (current != null) {
            searchTree.addNode(current.getX(), current.getY(), current.action);
            current = current.parent;
        }
    }

    private static class Node {
        Coordinate coordinate;
        int g, f;
        Node parent;
        Action action;

        Node(Coordinate coordinate, int g, int h, Node parent, Action action) {
            this.coordinate = coordinate;
            this.g = g;
            this.f = g + h;
            this.parent = parent;
            this.action = action;
        }

        public int getX() {
            return coordinate.getX();
        }
        public int getY() {
            return coordinate.getY();
        }
    }

    public enum Action {
        MOVE_NORTH(-1, 0),
        MOVE_SOUTH(1, 0),
        MOVE_WEST(0, -1),
        MOVE_EAST(0, 1),
        ESCALATOR(1000,1000);


        final int dx, dy;

        Action(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }
}
