package org.game.model.board;

import org.game.model.*;
import org.game.model.AI.PathFinder;
import org.game.model.AI.SearchPath;
import org.game.utils.Config;

import java.util.List;

public class Board {
    // store tiles as a 2-d array of size 70x70
    private final Tile[][] tiles;
    private final int numRows;
    private final int numCols;
    private List<Pawn> pawns;
    private final List<BoardEscalator> escalators = new java.util.ArrayList<>();
    private final List<BoardVortex> yellowVortices = new java.util.ArrayList<>();
    private final List<BoardVortex> purpleVortices = new java.util.ArrayList<>();
    private final List<BoardVortex> greenVortices = new java.util.ArrayList<>();
    private final List<BoardVortex> orangeVortices = new java.util.ArrayList<>();
    private final Timer timer;
    private final PawnManager pawnManager;
    private final PathFinder pathFinder;
    private final GeneralGoalManager generalGoalManager;


    public Board(int maxSize) {
        numRows = maxSize;
        numCols = maxSize;
        this.tiles = new Tile[numRows][numCols];
        this.timer = new Timer();
        this.pawnManager = new PawnManager(this);
        this.pathFinder = new PathFinder(tiles);
        this.generalGoalManager = GeneralGoalManager.getInstance();
    }

    public void testPathFinder(){
        // locate one of the pawns
        Coordinate from = new Coordinate(13, 11);
        Coordinate to = new Coordinate(13, 14);
        System.out.println("Finding path from "+ from +" to "+ to);
        SearchPath path = pathFinder.findShortestPath(from, to);
        if (path != null) {
            System.out.println("Path found:");
            for (SearchPath.Node node : path.getNodes()) {
                System.out.println("Step to (" + node.x() + ", " + node.y() + ") using " + node.action());
            }
        } else {
            System.out.println("No path found.");
        }
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public void initializeStartingTile(Card startingCard) {
        Tile[][] startingTiles = startingCard.getTiles();

        for (int i = 0; i < startingTiles.length; i++) {
            for (int j = 0; j < startingTiles[i].length; j++) {
                int x = (int) (i + (double) (numRows / 2));
                int y = (int) (j + (double) (numRows / 2));
                if(Config.PRINT_EVERYTHING){
                    System.out.println("Placing tile at (" + x + ", " + y + ") of type " + startingTiles[i][j].getType());
                }
                tiles[x][y] = startingTiles[i][j];

            }
        }
        // handle tile type specifics AFTER all tiles are placed
        for (int i = 0; i < startingTiles.length; i++) {
            for (int j = 0; j < startingTiles[i].length; j++) {
                int x = (int) (i + (double) (numRows / 2));
                int y = (int) (j + (double) (numRows / 2));
                handleTileTypeSpecifics(startingTiles[i][j], new Coordinate(x, y));
            }
        }
        // printEscalators();
    }

    private void handleTileTypeSpecifics(Tile tile, Coordinate position) {
        if(tile.hasEscalator()){
            updateEscalator(tile, position);
        }
        switch (tile.getType()) {
            case VORTEX -> addVortex(tile.getColor(), tile, position);
            case TIMER -> generalGoalManager.addTimerToAllPawns(position);
            case GOAL_ITEM -> generalGoalManager.getPawnGoalManager(tile.getColor()).setItem(position);
            case GOAL_EXIT -> generalGoalManager.getPawnGoalManager(tile.getColor()).setExit(position);
            case DISCOVERY -> {
                // only add discovery if it is not surrounded from all 4 sides
                if (!(isTileAt(position.move(1, 0)) && isTileAt(position.move(-1, 0)) && isTileAt(position.move(0, 1)) && isTileAt(position.move(0, -1))))
                 {
                    generalGoalManager.getPawnGoalManager(tile.getColor()).addDiscovery(position);
                 }
            }
            default -> {
            }
        }
    }

    private void checkForBlockedDiscoveries(Coordinate leftTopCorner){
        List<Coordinate> possibleEntries = getFourPossibleAdjacentEntryTiles(leftTopCorner);
        for (Coordinate entry : possibleEntries) {
            // check if there is a discovery tile at this position
            Tile tile = getTileAt(new Coordinate(entry.x(), entry.y()));
            if(tile != null && tile.getType() == TileType.DISCOVERY){
                // no need to check if it is now blocked from all 4 sides - if it exists, it is surrounded
                // remove from generalGoalManager
                generalGoalManager.getPawnGoalManager(tile.getColor()).removeDiscovery(entry);
            }
        }
    }

    private void addVortex(Color color, Tile tile, Coordinate position){
        List<BoardVortex> vortexList;
        switch (color) {
            case YELLOW: vortexList = yellowVortices; break;
            case PURPLE: vortexList = purpleVortices; break;
            case GREEN: vortexList = greenVortices; break;
            case ORANGE: vortexList = orangeVortices; break;
            default: return;
        }
        vortexList.add(new BoardVortex(position, tile.getCardId(), tile.getColor()));
    }

    public void initializeStartingPawns(List<Pawn> initialPawns) {
        this.pawns = initialPawns;
        // for each pawn, set their position at occupied in the corresponding tile
        for (Pawn pawn : initialPawns) {
            int x = pawn.getCoordinate().x();
            int y = pawn.getCoordinate().y();
            tiles[x][y].setOccupied(true);
        }
    }

    public boolean addCardToBoard(Card newCard, Coordinate coordinate) {
        // remove the discovery tile from goals

        // calculate the newCard so that the START is on the bottom left corner of the coordinate
        Tile[][] newCardTiles = newCard.getTiles();

        // check the 4 possible positions of Start
        // left
        if(newCardTiles[1][0].getType() == TileType.START){
            newCard = newCard.rotate270();
        }
        //right
        else if(newCardTiles[2][3].getType() == TileType.START){
            newCard = newCard.rotate90();
        }
        //top
        else if(newCardTiles[0][2].getType() == TileType.START){
            newCard = newCard.rotate180();
        }

        Card rotatedCard = newCard; // default no rotation

        // no tile to the right
        if(!isTileAt(coordinate.move(0, 1))) {
            // rotate newTiles 90 degrees clockwise
            rotatedCard = newCard.rotate90();
        }
        // no tile to the left
        else if(!isTileAt(coordinate.move(0, -1))) {
            // rotate newTiles 90 degrees counter-clockwise
            rotatedCard = newCard.rotate270();
        }
        // no tile above
        else if(!isTileAt(coordinate.move(-1, 0))) {
            // do not rotate
        }
        // no tile below
        else if(!isTileAt(coordinate.move(1, 0))) {
            // rotate newTiles 180 degrees
            rotatedCard = newCard.rotate180();
        }
        else{
            System.out.println("Error: Cannot place card at the given coordinate, there are tiles in all 4 directions.");
            return false;
        }

        Coordinate corner = getLeftTopCornerOfNewCard(coordinate);
        Tile[][] rotatedTiles = rotatedCard.getTiles();
        // add the new tiles to the board at the correct position
        for (int i = 0; i < rotatedTiles.length; i++) {
            System.arraycopy(rotatedTiles[i], 0, tiles[corner.x() + i], corner.y(), rotatedTiles[i].length);
        }
        // run after adding all tiles
        for (int i = 0; i < rotatedTiles.length; i++) {
            for (int j = 0; j < rotatedTiles[i].length; j++) {
                Coordinate c = new Coordinate(corner.x() + i, corner.y() + j);
                handleTileTypeSpecifics(rotatedTiles[i][j], c);
            }
        }

        checkForBlockedDiscoveries(corner);

        return true;
    }

    private void updateEscalator(Tile tile, Coordinate position){
        // if escalators does not contain BoardEscalator where hasId equals startingTiles[i][j].getEscalator()
        // if there is no escalator with the same id, add a new one
        if(escalators.stream().noneMatch(e -> e.getId().equals(tile.getEscalator()))){
            escalators.add(new BoardEscalator(position, tile.getEscalator()));
        }
        else{
            // find BoardEscalator with the same id and add the new coordinate to it
            for (BoardEscalator escalator : escalators) {
                if (escalator.getId().equals(tile.getEscalator())) {
                    escalator.setEnd(position);
                    break;
                }
            }
        }
    }

    public boolean isTileAt(Coordinate position) {
        return tiles[position.x()][position.y()] != null;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }

    public Pawn getPawnByColor(Color color) {
        for (Pawn pawn : pawns) {
            if (pawn.getColor() == color) {
                return pawn;
            }
        }
        return null;
    }

    public void printAllPawns(){
        for (Pawn pawn : pawns) {
            System.out.println("Pawn color: " + pawn.getColor() + " at position " + pawn.getCoordinate());
        }
    }

    public Tile getTileAt(Coordinate coordinate) {
        if (tiles[coordinate.x()][coordinate.y()] != null) {
            return tiles[coordinate.x()][coordinate.y()];
        } else {
            return null;
        }
    }

    public Coordinate getLeftTopCornerOfNewCard(Coordinate position) {
        int startX = position.x();
        int startY = position.y();

        // no tile to the right
        if(!isTileAt(position.move(0, 1))){
            return new Coordinate(startX-1, startY+1);
        }
        // no tile to the left
        else if(!isTileAt(position.move(0, -1))){
            return new Coordinate(startX-2, startY-4);
        }
        // no tile above
        else if(!isTileAt(position.move(-1, 0))){
            return new Coordinate(startX-4, startY-1);
        }
        // no tile below
        else if(!isTileAt(position.move(1, 0))){
            return new Coordinate(startX+1, startY-2);
        }
        return null; // should not reach here
    }

    public List<Coordinate> getFourPossibleAdjacentEntryTiles(Coordinate leftTopCorner){
        List<Coordinate> possibleEntries = new java.util.ArrayList<>();
        possibleEntries.add(new Coordinate(leftTopCorner.x()+1, leftTopCorner.y()-1)); // left
        possibleEntries.add(new Coordinate(leftTopCorner.x()+2, leftTopCorner.y()+4)); // right
        possibleEntries.add(new Coordinate(leftTopCorner.x()-1, leftTopCorner.y()+2)); // top
        possibleEntries.add(new Coordinate(leftTopCorner.x()+4, leftTopCorner.y()+1)); // bottom
        return possibleEntries;
    }

    public void printEscalators(){
        for (BoardEscalator escalator : escalators) {
            System.out.println("Escalator ID: " + escalator.getId() + " from " + escalator.getStart().x() + "," + escalator.getStart().y() +
                    " to " + (escalator.getEnd() != null ? escalator.getEnd().x() + "," + escalator.getEnd().y() : "not set"));
        }
    }

    public List<BoardEscalator> getEscalators() {
        return escalators;
    }

    public Timer getTimer() {
        return timer;
    }

    public boolean isPawnAtTimerTile(Pawn pawn){
        Tile tile = tiles[pawn.getCoordinate().x()][pawn.getCoordinate().y()];
        return tile.getType() == TileType.TIMER;
    }

    public Pawn getRandomPawn(){
        java.util.Random rand = new java.util.Random();
        int randomIndex = rand.nextInt(pawns.size());
        return pawns.get(randomIndex);
    }

    public List<BoardVortex> getVortexListByColor(Color color) {
        return switch (color) {
            case YELLOW -> yellowVortices;
            case PURPLE -> purpleVortices;
            case GREEN -> greenVortices;
            case ORANGE -> orangeVortices;
            default -> new java.util.ArrayList<>();
        };
    }

    public Pawn useVortex(Color pawnColor, int vortexNumber){
        return pawnManager.useVortex(pawnColor, vortexNumber);
    }

    public Pawn useEscalator(Color pawnColor){
        return pawnManager.useEscalator(pawnColor);
    }

    public Pawn movePawn(Color pawnColor, Action action) {
        return pawnManager.movePawn(pawnColor, action);
    }

    public void removeTimerFromGoals(Coordinate timer) {
        generalGoalManager.removeTimerFromAllPawns(timer);
    }

    public PawnManager getPawnManager() {
        return pawnManager;
    }
}
