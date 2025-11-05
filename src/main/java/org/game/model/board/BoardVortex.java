package org.game.model.board;

import org.game.model.Color;
import org.game.model.Coordinate;

public class BoardVortex {
    private Coordinate position;
    private int cardId;
    private Color color;

    public BoardVortex(Coordinate position, int cardId, Color color) {
        this.position = position;
        this.cardId = cardId;
        this.color = color;
    }

    public Coordinate getPosition() {
        return position;
    }

    public int getCardId() {
        return cardId;
    }

    public Color getColor() {
        return color;
    }
}
