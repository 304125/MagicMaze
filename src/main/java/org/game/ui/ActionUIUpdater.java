package org.game.ui;

import org.game.model.Action;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
        // display image from resources/images corresponding to the action
        String path = "images/"+action.toLowerCase()+".png";
        BufferedImage image;
        try{
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImageIcon imageIcon = new ImageIcon(image);
        ImagePanel imagePanel = new ImagePanel(imageIcon.getImage(), 0.9, Color.LIGHT_GRAY);
        actionTextPanel.setLayout(new BorderLayout());
        actionTextPanel.add(imagePanel, BorderLayout.CENTER);
        actionTextPanel.revalidate();
        actionTextPanel.repaint();
    }
}