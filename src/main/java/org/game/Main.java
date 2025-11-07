package org.game;

import org.game.model.Game;
import org.game.ui.BoardUI;
import org.game.utils.ActionDelegator;
import org.game.utils.InputManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            BoardUI boardUI = new BoardUI(game.getBoard());
            ActionDelegator actionDelegator = new ActionDelegator(game, boardUI);
            new InputManager(actionDelegator);
        });
    }
}
