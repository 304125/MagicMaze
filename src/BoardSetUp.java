import java.util.ArrayList;
import java.util.List;

public class BoardSetUp {
    private List<List<Tile>> board;
    private boolean[][] visibility; // Tracks visible tiles

    public BoardSetUp() {
        board = new ArrayList<>();
        initializeDummyBoard();
    }

    private void initializeDummyBoard() {
        // Example 5x5 board setup
        TileType[][] layout = {
                {TileType.START, TileType.PATH, TileType.OBSTACLE, TileType.PATH, TileType.GOAL},
                {TileType.PATH, TileType.PATH, TileType.OBSTACLE, TileType.PATH, TileType.PATH},
                {TileType.PATH, TileType.OBSTACLE, TileType.PATH, TileType.PATH, TileType.PATH},
                {TileType.PATH, TileType.PATH, TileType.PATH, TileType.OBSTACLE, TileType.PATH},
                {TileType.PATH, TileType.PATH, TileType.PATH, TileType.PATH, TileType.PATH}
        };

        for (int i = 0; i < layout.length; i++) {
            List<Tile> row = new ArrayList<>();
            for (int j = 0; j < layout[i].length; j++) {
                // Randomly decide walls for each tile
                boolean wallUp = i == 0 || (Math.random() < 0.3); // Top row or 30% chance
                boolean wallDown = i == layout.length - 1 || (Math.random() < 0.3); // Bottom row or 30% chance
                boolean wallLeft = j == 0 || (Math.random() < 0.3); // Leftmost column or 30% chance
                boolean wallRight = j == layout[i].length - 1 || (Math.random() < 0.3); // Rightmost column or 30% chance

                // Ensure consistency between adjacent tiles
                if (i > 0) {
                    Tile aboveTile = board.get(i - 1).get(j);
                    wallUp = aboveTile.hasWallDown();
                }
                if (j > 0) {
                    Tile leftTile = row.get(j - 1);
                    wallLeft = leftTile.hasWallRight();
                }

                row.add(new Tile(layout[i][j], wallUp, wallDown, wallLeft, wallRight));
            }
            board.add(row);
        }
    }

    public List<List<Tile>> getBoard() {
        return board;
    }

    public void printBoard() {
        for (List<Tile> row : board) {
            for (Tile tile : row) {
                System.out.print(tile.getType() + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        BoardSetUp boardSetUp = new BoardSetUp();
        boardSetUp.printBoard();
    }
}