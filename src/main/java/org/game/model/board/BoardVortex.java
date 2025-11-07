package org.game.model.board;

import org.game.model.Color;
import org.game.model.Coordinate;

public record BoardVortex(Coordinate position, int cardId, Color color) {
}
