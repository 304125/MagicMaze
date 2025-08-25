public class Tile {
    private TileType type;
    private boolean wallUp;
    private boolean wallDown;
    private boolean wallLeft;
    private boolean wallRight;

    public Tile(TileType type, boolean wallUp, boolean wallDown, boolean wallLeft, boolean wallRight) {
        this.type = type;
        this.wallUp = wallUp;
        this.wallDown = wallDown;
        this.wallLeft = wallLeft;
        this.wallRight = wallRight;
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