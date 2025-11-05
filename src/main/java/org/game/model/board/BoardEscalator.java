package org.game.model.board;

import org.game.model.Coordinate;

public class BoardEscalator {
    Coordinate start;
    String id;
    Coordinate end;

    public BoardEscalator(Coordinate start, String id) {
        this.start = start;
        this.id = id;
    }

    public void setEnd(Coordinate end) {
        this.end = end;
    }

    public Coordinate getStart() {
        return start;
    }

    public String getId() {
        return id;
    }

    public Coordinate getEnd() {
        return end;
    }
}
