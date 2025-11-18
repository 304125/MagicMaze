package org.game.utils.input;

import org.game.model.AI.AIPlayerType;

import java.util.List;

public class GameParams {
    private int numberOfPlayers;
    private List<AIPlayerConfig> aiPlayers;

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }
    public List<AIPlayerConfig> getAiPlayers() {
        return aiPlayers;
    }

}