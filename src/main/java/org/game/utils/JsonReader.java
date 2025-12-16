package org.game.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.game.model.*;
import org.game.model.AI.PlayerType.AIPlayerType;
import org.game.model.AI.PlayerType.AIPlayer;
import org.game.model.board.Board;
import org.game.utils.output.GameRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonReader {

    public List<Card> loadCardsFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = "input/tiles.json";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + filePath);
            }

            List<Map<String, Object>> tiles = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {}
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
                List<List<String>> escalators = (List<List<String>>) tile.get("escalators");
                cards.add(createCardFromJson(id, layout, color, walls_up, walls_down, walls_left, walls_right, escalators));
            }

            return cards;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load board from JSON", e);
        }
    }

    public GameRecord loadGameFromJson(String folderName, String fileName){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        String filePath = "output/all/" + folderName + "/" + fileName;
        Path path = Path.of(filePath);

        try (InputStream inputStream = Files.newInputStream(path)) {
            // Deserialize JSON into GameRecord object
            return objectMapper.readValue(inputStream, GameRecord.class);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load board from JSON", e);
        }
    }

    private Card createCardFromJson(int id, List<List<String>> layout, List<List<String>> color, List<List<Boolean>> walls_up, List<List<Boolean>> walls_down, List<List<Boolean>> walls_left, List<List<Boolean>> walls_right, List<List<String>> escalators) {
        Tile[][] tiles = new Tile[layout.size()][layout.getFirst().size()];
        for (int i = 0; i < layout.size(); i++) {
            List<Tile> row = new ArrayList<>();
            for (int j = 0; j < layout.get(i).size(); j++) {
                TileType type = TileType.valueOf(layout.get(i).get(j));
                Color tileColor = Color.valueOf(color.get(i).get(j));
                boolean wallUpConfig = walls_up.get(i).get(j);
                boolean wallDownConfig = walls_down.get(i).get(j);
                boolean wallLeftConfig = walls_left.get(i).get(j);
                boolean wallRightConfig = walls_right.get(i).get(j);
                Tile thisTile;
                if(!escalators.get(i).get(j).equals("-")) {
                    // there is an escalator on this tile
                    String escalatorString = String.valueOf(id).concat(String.valueOf(escalators.get(i).get(j)));
                    thisTile = new Tile(type, tileColor, wallUpConfig, wallDownConfig, wallLeftConfig, wallRightConfig, id, escalatorString);
                }
                else{
                    thisTile = new Tile(type, tileColor, wallUpConfig, wallDownConfig, wallLeftConfig, wallRightConfig, id);
                }
                row.add(thisTile);
            }
            tiles[i] = row.toArray(new Tile[0]);
        }

        return new Card(id, tiles);
    }

    public List<Player> loadPlayersFromJson(int numPlayers, List<AIPlayerType> aiPlayerTypes, Board board) {
        int aiPlayersLeft = aiPlayerTypes.size();
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = "input/actions.json";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + filePath);
            }

            List<Map<String, Object>> playersData = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {}
            );

            List<Player> players = new ArrayList<>();
            for (Map<String, Object> playerData : playersData) {
                int numberOfPlayers = (Integer) playerData.get("players");
                List<List<String>> actionCards = (List<List<String>>) playerData.get("cards");
                // shuffle actionCards
                Collections.shuffle(actionCards);

                // only continue if numberOfPlayers == numPlayers
                if (numberOfPlayers == numPlayers) {
                    for (List<String> actionCardList : actionCards) {
                        List<ActionType> actions = new ArrayList<>();
                        for (String actionString : actionCardList) {
                            actions.add(ActionType.valueOf(actionString));
                        }
                        if(aiPlayersLeft > 0){
                            // add the next in the aiPlayerTypes list
                            AIPlayerType playerType =  aiPlayerTypes.get(aiPlayerTypes.size() - aiPlayersLeft);
                            switch (playerType) {
                                case REACTIVE -> players.add(new AIPlayer(actions, "AI player: " +playerType + "(" + aiPlayersLeft + ")", board, AIPlayerType.REACTIVE));
                                case SHORT_FUSE -> players.add(new AIPlayer(actions, "AI player: " +playerType + "(" + aiPlayersLeft + ")", board, AIPlayerType.SHORT_FUSE));
                                case BASIC -> players.add(new AIPlayer(actions, "AI player: " + playerType + "(" + aiPlayersLeft + ")", board, AIPlayerType.BASIC));
                            }
                            aiPlayersLeft--;
                        }
                        else{
                            players.add(new Player(actions, "Human player"));
                        }
                    }
                    break; // exit the loop once we've found the matching number of players
                }
            }

            return players;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load players from JSON", e);
        }
    }
}
