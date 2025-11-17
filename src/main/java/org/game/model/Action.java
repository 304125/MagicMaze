package org.game.model;

public enum Action {
    MOVE_NORTH,
    MOVE_SOUTH,
    MOVE_EAST,
    MOVE_WEST,
    DISCOVER,
    VORTEX,
    ESCALATOR;

    private Coordinate vortexCoordinate;

    Action(){}

    public void setVortexCoordinate(Coordinate coordinate){
        this.vortexCoordinate = coordinate;
    }

    public Coordinate getVortexCoordinate(){
        return this.vortexCoordinate;
    }
}
