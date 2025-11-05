package org.game.model;

public class Coordinate {
    private int x; // Row position
    private int y; // Column position

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Coordinate move(int x, int y){
        return new Coordinate(this.x + x, this.y + y);
    }

    public String toString(){
        return "("+x+", "+y+")";
    }

    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return this.x == that.x && this.y == that.y;
    }

    public Coordinate copy(){
        return new Coordinate(this.x, this.y);
    }
}
