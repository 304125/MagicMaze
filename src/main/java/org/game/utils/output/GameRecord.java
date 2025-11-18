package org.game.utils.output;

import org.game.utils.input.GameParams;

import java.util.List;

public class GameRecord {
    private GameParams gameParams;
    private List<String> gameMoves;

    public GameRecord(GameParams gameParams){
        this.gameParams = gameParams;
        gameMoves = new java.util.ArrayList<>();
    }

    public GameParams getGameParams() {
        return gameParams;
    }

    public List<String> getGameMoves() {
        return gameMoves;
    }
}
