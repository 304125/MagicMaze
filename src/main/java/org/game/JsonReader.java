package org.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonReader {

    public List<Card> loadCardsFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = "tiles.json";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + filePath);
            }

            List<Map<String, Object>> tiles = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // loop through all tiles, save them to a list of cards

            List<Card> cards = new ArrayList<>();
            for (Map<String, Object> tile : tiles) {
                int id = (Integer) tile.get("id");
                List<List<String>> layout = (List<List<String>>) tile.get("layout");
                List<List<String>> color = (List<List<String>>) tile.get("color");
                List<List<Boolean>> walls_up = (List<List<Boolean>>) tile.get("walls_up");
                List<List<Boolean>> walls_down = (List<List<Boolean>>) tile.get("walls_down");
                List<List<Boolean>> walls_left = (List<List<Boolean>>) tile.get("walls_left");
                List<List<Boolean>> walls_right = (List<List<Boolean>>) tile.get("walls_right");
                cards.add(createCardFromJson(id, layout, color, walls_up, walls_down, walls_left, walls_right));
            }

            return cards;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load board from JSON", e);
        }
    }

    private Card createCardFromJson(int id, List<List<String>> layout, List<List<String>> color, List<List<Boolean>> walls_up, List<List<Boolean>> walls_down, List<List<Boolean>> walls_left, List<List<Boolean>> walls_right) {
        Tile[][] tiles = new Tile[layout.size()][layout.get(0).size()];
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
            tiles[i] = row.toArray(new Tile[0]);
        }
        Card card = new Card(id, tiles);
        return card;
    }
}
