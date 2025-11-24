package org.game.utils.output;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.game.utils.input.GameParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class RunOverviewWriter {
    private final String filePath;

    public RunOverviewWriter(String gameName){
        filePath = "output/" + gameName + "/" + gameName + "_overview.json";
        createOrClearJsonFile();
    }

    private void createOrClearJsonFile(){
        Path path = Path.of(filePath);
        try {
            // Ensure the parent directory exists
            Files.createDirectories(path.getParent());

            if (Files.exists(path)) {
                // File exists → clear it by overwriting with empty JSON object
                Files.writeString(path, "{}", StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                // File does not exist → create it with empty JSON object
                Files.writeString(path, "{}", StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create or clear the JSON file: " + filePath, e);
        }
    }

    public void writeGameResult(GameParams gameParams, boolean gameWon) {
        Path path = Path.of(filePath);
        ObjectMapper objectMapper = new ObjectMapper();
        GameResult gameResult = new GameResult(gameParams, gameWon);

        try {
            // Read existing content or initialize an empty list
            GameResultList gameResults;
            if (Files.exists(path) && Files.size(path) > 0) {
                String existingContent = Files.readString(path);
                gameResults = objectMapper.readValue(existingContent, new TypeReference<GameResultList>() {});
            } else {
                gameResults = new GameResultList();
            }

            // Add the new GameResult
            gameResults.add(gameResult);

            // Write the updated list back to the file
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(gameResults);
            Files.writeString(path, jsonContent, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Could not write game record to file: " + filePath, e);
        }
    }
}
