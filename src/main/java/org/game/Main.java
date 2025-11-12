package org.game;

import org.game.model.AI.AIPlayerType;
import org.game.model.Game;
import org.game.ui.BoardUI;
import org.game.utils.ActionDelegator;
import org.game.utils.InputManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            boolean printEverything = false; // Set to true to enable detailed logging
            int numberOfPlayers = 4;
            List<AIPlayerType> aiPlayers = new ArrayList<>();
            aiPlayers.add(AIPlayerType.ONE_HERO);
            aiPlayers.add(AIPlayerType.ONE_HERO);
            aiPlayers.add(AIPlayerType.ONE_HERO);
            aiPlayers.add(AIPlayerType.ONE_HERO);


            Game game = new Game(numberOfPlayers, aiPlayers);
            BoardUI boardUI = new BoardUI(game.getBoard());
            ActionDelegator actionDelegator = new ActionDelegator(game, boardUI);
            game.giveActionDelegatorToAIPlayers(actionDelegator);
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
