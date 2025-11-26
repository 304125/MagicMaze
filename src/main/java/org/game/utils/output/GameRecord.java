package org.game.utils.output;

import org.game.model.Color;
import org.game.model.Coordinate;
import org.game.utils.input.GameParams;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GameRecord {
    private GameParams gameParams;
    private final TreeMap<Instant, String> gameMoves;
    private Map<Color, Coordinate> initialPawnPositions;

    public GameRecord(GameParams gameParams){
        this.gameParams = gameParams;
        gameMoves = new java.util.TreeMap<>();
    }

    public GameRecord(){
        gameMoves = new java.util.TreeMap<>();
    }

    public GameParams getGameParams() {
        return gameParams;
    }

    public TreeMap<Instant, String> getGameMoves() {
        return gameMoves;
    }

    public Map<Color, Coordinate> getInitialPawnPositions() {
        return initialPawnPositions;
    }

    public Map.Entry<Instant, String> next() {
        return gameMoves.pollFirstEntry();
    }

    public void addMove(Instant timestamp, String move){
        gameMoves.put(timestamp, move);
    }

    public void setInitialPawnPositions(List<Color> pawnColors, List<Coordinate> coordinates){
        initialPawnPositions = new TreeMap<>();
        for(int i = 0; i < pawnColors.size(); i++){
            initialPawnPositions.put(pawnColors.get(i), coordinates.get(i));
        }
    }
}
