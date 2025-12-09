package org.game.model;


import java.util.Objects;

public class Action {
    private final ActionType type;
    private Coordinate vortexCoordinate;

    public Action(ActionType type){
        this.type = type;
    }

    public Action(ActionType type, Coordinate vortexCoordinate){
        this.type = type;
        this.vortexCoordinate = vortexCoordinate;
    }

    public ActionType getType(){
        return this.type;
    }

    public void setVortexCoordinate(Coordinate coordinate){
        this.vortexCoordinate = coordinate;
    }

    public Coordinate getVortexCoordinate(){
        return this.vortexCoordinate;
    }

    public String toString(){
        if(this.type == ActionType.VORTEX && this.vortexCoordinate != null){
            return this.type+" ("+this.vortexCoordinate + ")";
        }
        return this.type.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Action other = (Action) obj;
        if (this.type != other.type) return false;
        if (this.type == ActionType.VORTEX) {
            return this.vortexCoordinate != null && this.vortexCoordinate.equals(other.vortexCoordinate);
        }
        return true;
    }

    @Override
    public int hashCode() {
        if(vortexCoordinate != null){
            return Objects.hash(type, vortexCoordinate);
        }
        return Objects.hash(type);
    }
}
