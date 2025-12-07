package org.game.ui;

import org.game.model.Action;

import javax.swing.*;
import java.awt.*;

public class ActionUIUpdater {
    private final BoardUI boardUI;

    public ActionUIUpdater(BoardUI boardUI) {
        this.boardUI = boardUI;
    }

    public void updateUI(String action) {
        // Get the tile panels for the first row
        JPanel actionIndicatorPanel = boardUI.getTilePanelAt(0, 2); // Tile to turn red
        JPanel actionTextPanel = boardUI.getTilePanelAt(0, 3); // Tile to display action text

        // Turn the action indicator panel red
        actionIndicatorPanel.setBackground(Color.RED);
        actionIndicatorPanel.revalidate();
        actionIndicatorPanel.repaint();

        // Use a timer to reset the color after 0.45 seconds
        Timer resetTimer = new Timer(300, e -> {
            actionIndicatorPanel.setBackground(Color.LIGHT_GRAY);
            actionIndicatorPanel.revalidate();
            actionIndicatorPanel.repaint();
        });
        resetTimer.setRepeats(false);
        resetTimer.start(); // Start the timer

        // Update the action text panel
        actionTextPanel.removeAll();
        JLabel actionLabel = new JLabel(action.toString());
        actionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        actionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        actionTextPanel.setLayout(new BorderLayout());
        actionTextPanel.add(actionLabel, BorderLayout.CENTER);
        actionTextPanel.revalidate();
        actionTextPanel.repaint();
    }
}