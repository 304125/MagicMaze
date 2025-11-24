package org.game.utils.output;

import org.game.model.AI.AIPlayerType;
import org.game.model.Game;
import org.game.utils.input.GameParams;

import java.util.ArrayList;
import java.util.List;

public class GameResult {
    private int numberOfPlayers;
    private List<AIPlayerType> aiPlayers;
    private boolean gameWon;

    public GameResult(GameParams gameParams, boolean gameWon){
        this.numberOfPlayers = gameParams.getNumberOfPlayers();
        this.aiPlayers = gameParams.getAiPlayers();
        this.gameWon = gameWon;
    }

    public GameResult(){
        aiPlayers = new ArrayList<>();
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public List<AIPlayerType> getAiPlayers() {
        return aiPlayers;
    }

    public boolean isGameWon() {
        return gameWon;
    }
}
