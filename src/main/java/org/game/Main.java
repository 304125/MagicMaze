package org.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.game.model.AI.AIPlayerType;
import org.game.model.Game;
import org.game.ui.BoardUI;
import org.game.utils.ActionDelegator;
import org.game.utils.InputManager;
import org.game.utils.input.GameParams;
import org.game.utils.input.RootParams;
import org.game.utils.output.ActionWriter;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("input/params.json");

        if (inputStream == null){
            throw new RuntimeException("Could not find params.json");
        }

        RootParams root = mapper.readValue(inputStream, RootParams.class);

        if(root.getMode().equals("new")){
            int gameNumber = 1;
            for (GameParams gameParams : root.getGames()){
                System.out.println("Starting a new game");
                CountDownLatch latch = new CountDownLatch(1);
                String gameName = root.getGameName() + "_" + gameNumber;
                runOnce(gameParams, latch, gameName, root.getGameName());
                latch.await();
                gameNumber++;
            }
            System.out.println("All games finished");
        }
    }

    public static void runOnce(GameParams gameParams, CountDownLatch latch, String gameName, String folderName) throws IOException {
        SwingUtilities.invokeLater(() -> {
            boolean printEverything = false; // Set to true to enable detailed logging

            Game game = new Game(gameParams.getNumberOfPlayers(), gameParams.getAiPlayers());
            BoardUI boardUI = new BoardUI(game.getBoard());
            ActionWriter actionWriter = new ActionWriter(folderName, gameName, gameParams);
            ActionDelegator actionDelegator = new ActionDelegator(game, boardUI, actionWriter);
            game.giveActionDelegatorToAIPlayers(actionDelegator);

            game.setTimerFinishCallback(() -> {
                game.endGame();
                System.out.println("Game has ended.");
                SwingUtilities.invokeLater(boardUI::dispose); // Close the window
                latch.countDown();
            });

            // sleep for 1 second to let the UI load
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            game.startGame();


            new InputManager(actionDelegator);
        });
    }
}
