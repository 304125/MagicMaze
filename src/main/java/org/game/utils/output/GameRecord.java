package org.game.utils.output;

import org.game.utils.input.GameParams;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GameRecord {
    private GameParams gameParams;
    private TreeMap<Instant, String> gameMoves;

    public GameRecord(GameParams gameParams){
        this.gameParams = gameParams;
        gameMoves = new java.util.TreeMap<>();
    }

    public GameParams getGameParams() {
        return gameParams;
    }

    public TreeMap<Instant, String> getGameMoves() {
        return gameMoves;
    }

    public Map.Entry<Instant, String> next() {
        return gameMoves.pollFirstEntry();
    }

    public void addMove(Instant timestamp, String move){
        gameMoves.put(timestamp, move);
    }

    public boolean isEmpty() {
        return gameMoves.isEmpty();
    }
}
