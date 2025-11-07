package org.game.model.AI;

import org.game.model.Coordinate;
import org.game.model.board.PawnManager;
import org.game.model.Tile;

import java.util.*;

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
        Node startNode = new Node(coordinateStart, 0, findDistance(coordinateStart, coordinateEnd), null, null);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = current.getX() + "," + current.getY();

            // Goal check
            if (current.getX() == coordinateEnd.x() && current.getY() == coordinateEnd.y()) {
                reconstructPath(searchPath, current);
                return searchPath; // Return the the shortest path from start to goal
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
                        !closedSet.contains(coordinateOtherSideOfEscalator.x() + "," + coordinateOtherSideOfEscalator.y())) {
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
                        !closedSet.contains(coordinateNew.x() + "," + coordinateNew.y()) &&
                        hasNoWall(grid[current.getX()][current.getY()], grid[coordinateNew.x()][coordinateNew.y()], action)) {
                    int newG = current.g + 1; // Cost to move to the neighbor
                    int newH = findDistance(coordinateNew, coordinateEnd);
                    Node neighbor = new Node(coordinateNew, newG, newH, current, action);

                    openSet.add(neighbor);
                }
            }
        }

        return null; // No path is found
    }

    private boolean isValid(Coordinate coordinate, int rows, int cols, Tile[][] grid) {
        return coordinate.x() >= 0 && coordinate.x() < rows && coordinate.y() >= 0 && coordinate.y() < cols && grid[coordinate.x()][coordinate.y()] != null;
    }

    private boolean hasNoWall(Tile fromTile, Tile toTile, Action action) {
        return switch (action) {
            case MOVE_NORTH -> !fromTile.hasWallUp() && !toTile.hasWallDown();
            case MOVE_SOUTH -> !fromTile.hasWallDown() && !toTile.hasWallUp();
            case MOVE_WEST -> !fromTile.hasWallLeft() && !toTile.hasWallRight();
            case MOVE_EAST -> !fromTile.hasWallRight() && !toTile.hasWallLeft();
            default -> false;
        };
    }

    public int findDistance(Coordinate coordinateA, Coordinate coordinateB) {
        return heuristicManhattan(coordinateA, coordinateB);
    }

    private int heuristicManhattan(Coordinate coordinateA, Coordinate coordinateB) {
        return Math.abs(coordinateA.x() - coordinateB.x()) + Math.abs(coordinateA.y() - coordinateB.y());
    }

    private void reconstructPath(SearchPath searchTree, Node goalNode) {
        Node current = goalNode;
        while (current != null) {
            searchTree.addNode(current.getX(), current.getY(), current.action);
            current = current.parent;
        }
        // Reverse the path to get from start to goal
        reversePath(searchTree);
    }

    private void reversePath(SearchPath searchTree) {
        List<SearchPath.Node> nodes = searchTree.getNodes();
        Collections.reverse(nodes);
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
            return coordinate.x();
        }
        public int getY() {
            return coordinate.y();
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
