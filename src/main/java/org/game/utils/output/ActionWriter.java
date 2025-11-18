package org.game.utils.output;

import org.game.utils.input.GameParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ActionWriter {
    private final String filePath;
    private GameRecord gameRecord;

    public ActionWriter(String folderName, String fileName, GameParams gameParams){
        filePath = "output/" + folderName + "/" + fileName + ".json";
        gameRecord = new GameRecord(gameParams);
        createOrClearJsonFile();
        writeGameRecordToFile();
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

    private void writeGameRecordToFile(){
        Path path = Path.of(filePath);
        try {
            String jsonContent = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(gameRecord);
            Files.writeString(path, jsonContent, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not write game record to file: " + filePath, e);
        }
    }
}
