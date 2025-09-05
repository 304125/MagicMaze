package org.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BoardSetUp {
    private Board board;
    private final String filePath = "tiles.json";

    public BoardSetUp() {
        board = new Board(loadBoardFromJson(filePath));
    }

    private List<List<Tile>> loadBoardFromJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + filePath);
            }

            List<Map<String, Object>> tiles = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Randomly select one tile
            Random random = new Random();
            Map<String, Object> selectedTile = tiles.get(random.nextInt(tiles.size()));

            // Parse layout and walls
            int id = (Integer) selectedTile.get("id");
            List<List<String>> layout = (List<List<String>>) selectedTile.get("layout");
            List<List<String>> color = (List<List<String>>) selectedTile.get("color");
            List<List<Boolean>> walls_up = (List<List<Boolean>>) selectedTile.get("walls_up");
            List<List<Boolean>> walls_down = (List<List<Boolean>>) selectedTile.get("walls_down");
            List<List<Boolean>> walls_left = (List<List<Boolean>>) selectedTile.get("walls_left");
            List<List<Boolean>> walls_right = (List<List<Boolean>>) selectedTile.get("walls_right");

            return createBoardFromJson(id, layout, color, walls_up, walls_down, walls_left, walls_right);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load board from JSON", e);
        }
    }

    private List<List<Tile>> createBoardFromJson(int id, List<List<String>> layout, List<List<String>> color, List<List<Boolean>> walls_up, List<List<Boolean>> walls_down, List<List<Boolean>> walls_left, List<List<Boolean>> walls_right) {
        List<List<Tile>> board = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            List<Tile> row = new ArrayList<>();
            for (int j = 0; j < layout.get(i).size(); j++) {
                TileType type = TileType.valueOf(layout.get(i).get(j));
                Color tileColor = Color.valueOf(color.get(i).get(j));
                boolean wallUpConfig = walls_up.get(i).get(j);
                boolean wallDownConfig = walls_down.get(i).get(j);
                boolean wallLeftConfig = walls_left.get(i).get(j);
                boolean wallRightConfig = walls_right.get(i).get(j);
                row.add(new Tile(type, tileColor, wallUpConfig, wallDownConfig, wallLeftConfig, wallRightConfig, id));

            }
            board.add(row);
        }
        return board;
    }

    public Board getBoard() {
        return board;
    }

    public static void main(String[] args) {
        BoardSetUp boardSetUp = new BoardSetUp();
        boardSetUp.board.printBoard();
    }
}