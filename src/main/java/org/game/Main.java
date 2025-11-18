package org.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.game.model.AI.AIPlayerType;
import org.game.model.Game;
import org.game.ui.BoardUI;
import org.game.utils.ActionDelegator;
import org.game.utils.InputManager;
import org.game.utils.input.RootParams;

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

        for (var gameParams : root.getGames()){
            System.out.println("Starting a new game");
            List<AIPlayerType> aiPlayerTypes = gameParams.getAiPlayers().stream()
                    .map(cfg -> AIPlayerType.valueOf(cfg.getType()))  // convert string → enum
                    .collect(Collectors.toList());
            CountDownLatch latch = new CountDownLatch(1);
            runOnce(gameParams.getNumberOfPlayers(), aiPlayerTypes, latch);
            latch.await();
        }
        System.out.println("All games finished");
    }

    public static void runOnce(int numberOfPlayers, List<AIPlayerType> aiPlayerTypes, CountDownLatch latch) {
        SwingUtilities.invokeLater(() -> {
            boolean printEverything = false; // Set to true to enable detailed logging

            Game game = new Game(numberOfPlayers, aiPlayerTypes);
            BoardUI boardUI = new BoardUI(game.getBoard());
            ActionDelegator actionDelegator = new ActionDelegator(game, boardUI);
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
