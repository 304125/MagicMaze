package org.game.utils.output;

import org.game.model.Action;
import org.game.model.Color;
import org.game.model.Pawn;
import org.game.utils.input.GameParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

public class ActionWriter {
    private final String filePath;
    private final GameRecord gameRecord;

    public ActionWriter(String folderName, String fileName, GameParams gameParams, List<Pawn> allPawns){
        filePath = "output/" + folderName + "/" + fileName + ".json";
        gameRecord = new GameRecord(gameParams);
        initializePawnPositions(allPawns);
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

    private void initializePawnPositions(List<Pawn> allPawns){
        List<Color> pawnColors = allPawns.stream().map(Pawn::getColor).toList();
        List<org.game.model.Coordinate> coordinates = allPawns.stream().map(Pawn::getCoordinate).toList();
        gameRecord.setInitialPawnPositions(pawnColors, coordinates);
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

    public void recordMove(Color pawnColor, Action action){
        String color = getColor(pawnColor);
        String actionString = switch (action) {
            case MOVE_NORTH -> "n";
            case MOVE_SOUTH -> "s";
            case MOVE_WEST -> "w";
            case MOVE_EAST -> "e";
            case ESCALATOR -> "x";
            default -> "";
        };
        String record = color + actionString;
        gameRecord.addMove(Instant.now(), record);
    }

    public void close(){
        writeGameRecordToFile();
    }

    public void recordVortex(Color pawnColor, int vortexNumber){
        String color = getColor(pawnColor);
        String record = color + "v" + vortexNumber;
        gameRecord.addMove(Instant.now(), record);
        writeGameRecordToFile();
    }

    public void recordDiscover(Color pawnColor, int cardId){
        String color = getColor(pawnColor);
        String record = color + "d" + cardId;
        gameRecord.addMove(Instant.now(), record);
        writeGameRecordToFile();
    }

    public void recordDoSomething(List<Action> actions){
        String actionString = actions.toString();
        gameRecord.addDoSomething(Instant.now(), actionString);
        writeGameRecordToFile();
    }

    private String getColor(Color color){
        return switch (color) {
            case YELLOW -> "y";
            case ORANGE -> "o";
            case PURPLE -> "p";
            case GREEN -> "g";
            default -> "";
        };
    }
}
