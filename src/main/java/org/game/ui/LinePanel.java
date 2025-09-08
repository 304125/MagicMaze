package org.game.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LinePanel extends JPanel {
    private final List<Line> lines = new ArrayList<>();

    public void addLine(int x1, int y1, int x2, int y2) {
        lines.add(new Line(x1, y1, x2, y2));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // set color to hex code
        g2d.setColor(new Color(69, 64, 73));


        g2d.setStroke(new BasicStroke(5)); // Set line thickness

        for (Line line : lines) {
            g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
    }

    private static class Line {
        int x1, y1, x2, y2;

        Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
}