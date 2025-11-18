package org.game.utils.input;

import java.util.List;

public class RootParams {
    private String mode;
    private String gameName;
    private List<GameParams> games;

    public List<GameParams> getGames() {
        return games;
    }

    public String getMode() {
        return mode;
    }

    public String getGameName() {
        return gameName;
    }
}
