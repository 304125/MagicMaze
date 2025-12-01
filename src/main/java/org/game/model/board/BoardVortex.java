package org.game.model.board;

import org.game.model.Color;
import org.game.model.Coordinate;

public record BoardVortex(Coordinate coordinate, int cardId, Color color) {
}
