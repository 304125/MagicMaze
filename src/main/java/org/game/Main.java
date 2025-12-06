package org.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.game.model.*;
import org.game.ui.BoardUI;
import org.game.utils.ActionDelegator;
import org.game.utils.KeyboardInputManager;
import org.game.utils.ReplayManager;
import org.game.utils.input.GameParams;
import org.game.utils.input.RootParams;
import org.game.utils.output.ActionWriter;
import org.game.utils.output.RunOverviewWriter;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("input/params.json");

        if (inputStream == null){
            throw new RuntimeException("Could not find params.json");
        }

        RootParams root = mapper.readValue(inputStream, RootParams.class);

        if(root.getMode().equals("new")){
            RunOverviewWriter runOverviewWriter = new RunOverviewWriter(root.getGameName());
            int gameNumber = 1;
            for (GameParams gameParams : root.getGames()){
                System.out.println("Starting a new game");
                CountDownLatch latch = new CountDownLatch(1);
                String gameName = root.getGameName() + "_" + gameNumber;
                runOnce(gameParams, latch, gameName, root.getGameName(), runOverviewWriter);
                latch.await();
                gameNumber++;
            }
            System.out.println("All games finished");
        }
        else if(root.getMode().equals("replay")){
            String folderName = root.getGameName();
            if(root.getReplayRun() == 0){
                System.out.println("Replaying all the runs, starting from 1");
                int gameNumber = 1;
                // loop through all files in the folder
                List<String> fileNames = new ArrayList<>();
                while(true){
                    String fileName = folderName + "_" + gameNumber + ".json";
                    String filePath = "output/" + folderName + "/" + fileName;
                    Path path = Path.of(filePath);
                    try{
                        Files.newInputStream(path);
                        fileNames.add(fileName);
                        gameNumber++;
                    }
                    catch (NoSuchFileException e){
                        break;
                    }
                }
                for(String fileName : fileNames){
                    System.out.println("Replaying run from file: " + fileName);
                    CountDownLatch latch = new CountDownLatch(1);
                    replayOnce(folderName, fileName, latch);
                    latch.await();
                }
            }
            else{
                System.out.println("Replaying run number: " + root.getReplayRun());
                String fileName = root.getGameName() + "_" + root.getReplayRun() + ".json";
                CountDownLatch latch = new CountDownLatch(1);
                replayOnce(folderName, fileName, latch);
                latch.await();
            }
        }
        else{
            throw new RuntimeException("Unknown mode: " + root.getMode());
        }
    }

    private static void runOnce(GameParams gameParams, CountDownLatch latch, String gameName, String folderName, RunOverviewWriter runOverviewWriter) {

        try {
            Game game = new Game(gameParams.getNumberOfPlayers(), gameParams.getAiPlayers());
            // all setup must finish before starting the game
            CountDownLatch setupLatch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {

                List<Pawn> allPawns = game.getBoard().getPawns();
                BoardUI boardUI = new BoardUI(game.getBoard());
                ActionWriter actionWriter = new ActionWriter(folderName, gameName, gameParams, allPawns);
                ActionDelegator actionDelegator = new ActionDelegator(game, boardUI, actionWriter);
                game.giveActionDelegatorToAIPlayers(actionDelegator);

                game.setTimerFinishCallback(() -> {
                    actionWriter.close();
                    game.endGame();
                    System.out.println("Game has ended, you have lost.");
                    runOverviewWriter.writeGameResult(gameParams, false);
                    SwingUtilities.invokeLater(boardUI::dispose); // Close the window
                    latch.countDown();
                });

                game.setGameWonCallback(() -> {
                    actionWriter.close();
                    game.endGame();
                    System.out.println("Congratulations! You have won the game.");
                    runOverviewWriter.writeGameResult(gameParams, true);
                    SwingUtilities.invokeLater(boardUI::dispose); // Close the window
                    latch.countDown();
                });

                new KeyboardInputManager(actionDelegator);
                setupLatch.countDown(); // Signal that setup is complete
            });

            setupLatch.await();
            game.startGame();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted during setup", e);
        }
    }

    private static void replayOnce(String folderName, String fileName, CountDownLatch latch){
        SwingUtilities.invokeLater(() -> {
            ReplayManager replayManager = new ReplayManager(folderName, fileName);
            Map<Color, Coordinate> initialPawnPositions = replayManager.getInitialPawnPositions();
            Game game = new Game(initialPawnPositions);
            BoardUI boardUI = new BoardUI(game.getBoard());
            ActionDelegator actionDelegator = new ActionDelegator(game, boardUI, null);
            replayManager.setActionDelegator(actionDelegator);
            replayManager.startReplay();

            replayManager.setOnGameFinishedCallback(() -> {

                SwingUtilities.invokeLater(boardUI::dispose); // Close the window
                latch.countDown();
            });

            game.setGameWonCallback(() -> {
                System.out.println("Replay has ended.");
                System.out.println("Congratulations! You have won the game.");
                SwingUtilities.invokeLater(boardUI::dispose); // Close the window
                latch.countDown();
            });

            // sleep for 1 second to let the UI load
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
