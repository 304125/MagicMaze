package org.game.model.AI.PlayerType;

public enum AIPlayerType {
    REACTIVE(new PlayerTypeParameters(6, (float)0.7, 3, 20, 1, (float)3.5, false, false, false, false, false, false)),
    SHORT_FUSE(new PlayerTypeParameters(9, 2, 1, 4, 1, (float)2.5, false, true, true, true, true, false)),
    BASIC(new PlayerTypeParameters(7, 1, 0, 10, 0, 3, false, true, false, false, true, true)),
    RANDOM(new PlayerTypeParameters(0, 1, 0, 1, 0, 3, false, false, false, false, true, false));

    private final PlayerTypeParameters parameters;

    AIPlayerType(PlayerTypeParameters parameters) {
        this.parameters = parameters;
    }

    public PlayerTypeParameters getParameters() {
        return parameters;
    }

    public String toString() {
        return this.name();
    }
}
