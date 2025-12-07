package org.game.ui;

import org.game.model.Action;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class ActionUIUpdater {
    private final BoardUI boardUI;

    public ActionUIUpdater(BoardUI boardUI) {
        this.boardUI = boardUI;
    }

    public void updateUI(List<Action> actions) {

        JPanel actionIndicatorPanel = boardUI.getTilePanelAt(0, 2); // Tile to turn red
        JPanel actionIconPanel1 = boardUI.getTilePanelAt(0, 3); // Tile to display action text
        JPanel actionIconPanel2 = boardUI.getTilePanelAt(0, 4);
        JPanel actionIconPanel3 = boardUI.getTilePanelAt(0, 5);
        JPanel actionIconPanel4 = boardUI.getTilePanelAt(0, 6);

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

        // for each actionIconPanel that is not null, update it with the corresponding action

        actionIconPanel1.removeAll();
        actionIconPanel2.removeAll();
        actionIconPanel3.removeAll();
        actionIconPanel4.removeAll();

        // there is always at least one action
        if(actions.isEmpty()){
            return;
        }

        populateActionPanel(actionIconPanel1, actions.get(0).toString());
        if(actions.size() >= 2){
            populateActionPanel(actionIconPanel2, actions.get(1).toString());
            if(actions.size() >= 3){
                populateActionPanel(actionIconPanel3, actions.get(2).toString());
                if(actions.size() == 4){
                    populateActionPanel(actionIconPanel4, actions.get(3).toString());
                }
            }
        }
    }

    private void populateActionPanel(JPanel panel, String action){
        // Update the action text panel
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
        panel.setLayout(new BorderLayout());
        panel.add(imagePanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }
}