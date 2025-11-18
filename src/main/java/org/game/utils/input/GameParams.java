package org.game.utils.input;

import org.game.model.AI.AIPlayerType;

import java.util.List;

public class GameParams {
    private int numberOfPlayers;
    private List<AIPlayerType> aiPlayers;

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }
    public List<AIPlayerType> getAiPlayers() {
        return aiPlayers;
    }

}