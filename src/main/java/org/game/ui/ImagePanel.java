package org.game.ui;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private final Image image;
    private final double scale;

    public ImagePanel(Image image, double scale, java.awt.Color background) {
        this.image = image;
        this.scale = scale;
        setBackground(background);
        setOpaque(true); // we want background color visible
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // this paints the background color

        if (image != null) {
            int drawWidth = (int) (getWidth() * scale);
            int drawHeight = (int) (getHeight() * scale);

            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;

            g.drawImage(image, x, y, drawWidth, drawHeight, this);
        }
    }
}

