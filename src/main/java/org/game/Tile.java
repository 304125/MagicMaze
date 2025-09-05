package org.game;

public class Tile {
    private TileType type;
    private Color color;
    private boolean wallUp;
    private boolean wallDown;
    private boolean wallLeft;
    private boolean wallRight;
    private int cardId;

    public Tile(TileType type, Color color, boolean wallUp, boolean wallDown, boolean wallLeft, boolean wallRight, int cardId) {
        this.type = type;
        this.wallUp = wallUp;
        this.wallDown = wallDown;
        this.wallLeft = wallLeft;
        this.wallRight = wallRight;
        this.color = color;
        this.cardId = cardId;
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
}