package org.game.utils.output;

import java.util.ArrayList;
import java.util.List;

public class GameResultList {
    private List<GameResult> gameResults;

    public GameResultList(){
        this.gameResults = new ArrayList<>();
    }

    public List<GameResult> getGameResults() {
        return gameResults;
    }

    public void add(GameResult gameResult){
        this.gameResults.add(gameResult);
    }
}
