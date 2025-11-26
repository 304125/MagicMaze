package org.game.model;

public class Tile {
    private final TileType type;
    private final Color color;
    private final boolean wallUp;
    private final boolean wallDown;
    private final boolean wallLeft;
    private final boolean wallRight;
    private final int cardId;
    private boolean isOccupied = false;
    private String escalator;
    private boolean used = false;

    public Tile(TileType type, Color color, boolean wallUp, boolean wallDown, boolean wallLeft, boolean wallRight, int cardId) {
        this.type = type;
        this.wallUp = wallUp;
        this.wallDown = wallDown;
        this.wallLeft = wallLeft;
        this.wallRight = wallRight;
        this.color = color;
        this.cardId = cardId;
    }

    public Tile(TileType type, Color color, boolean wallUp, boolean wallDown, boolean wallLeft, boolean wallRight, int cardId, String escalator) {
        this.type = type;
        this.wallUp = wallUp;
        this.wallDown = wallDown;
        this.wallLeft = wallLeft;
        this.wallRight = wallRight;
        this.color = color;
        this.cardId = cardId;
        this.escalator = escalator;
    }

    public TileType getType() {
        return type;
    }

    public boolean hasWallUp() {
        return wallUp;
    }

    public boolean hasWallDown() {
        return wallDown;
    }

    public boolean hasWallLeft() {
        return wallLeft;
    }

    public boolean hasWallRight() {
        return wallRight;
    }

    public Color getColor() {
        return color;
    }

    public int getCardId() {
        return cardId;
    }

    public boolean hasEscalator(){
        return escalator != null;
    }

    public String getEscalator(){
        return escalator;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "type=" + type +
                ", wallUp=" + wallUp +
                ", wallDown=" + wallDown +
                ", wallLeft=" + wallLeft +
                ", wallRight=" + wallRight +
                '}';
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}