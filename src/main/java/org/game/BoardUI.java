package org.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class BoardUI extends JFrame {
    private static final int TILE_SIZE = 50;
    private JPanel[][] tilePanels;
    private Pawn pawn;
    private List<List<Tile>> board;

    public BoardUI(BoardSetUp boardSetUp) {
        setTitle("Magic Maze Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.board = boardSetUp.getBoard(); // Initialize the board
        int rows = board.size();
        int cols = board.get(0).size();
        tilePanels = new JPanel[rows][cols];

        setLayout(new GridLayout(rows, cols));

        // Initialize the board and find the START position
        int startX = 0, startY = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile tile = board.get(i).get(j);
                JPanel tilePanel = new JPanel();
                tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                if(tile.getColor() != Color.NONE){
                    tilePanel.setBackground(getColorForTile(tile));
                }
                else{
                    tilePanel.setBackground(getColorForTileType(tile.getType()));
                }
                tilePanel.setBorder(createTileBorder(tile));
                tilePanels[i][j] = tilePanel;
                add(tilePanel);

                if (tile.getType() == TileType.START) {
                    startX = i;
                    startY = j;
                }
            }
        }

        // Initialize the pawn at the START position
        pawn = new Pawn(startX, startY);
        highlightPawn();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String direction = switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> "n"; // Move north
                    case KeyEvent.VK_S -> "s"; // Move south
                    case KeyEvent.VK_A -> "w"; // Move west
                    case KeyEvent.VK_D -> "e"; // Move east
                    default -> null;
                };

                if (direction != null) {
                    movePawn(direction);
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void highlightPawn() {
        // Clear all tiles
        for (int i = 0; i < tilePanels.length; i++) {
            for (int j = 0; j < tilePanels[i].length; j++) {
                tilePanels[i][j].removeAll();
            }
        }

        // Highlight the pawn's position
        JPanel pawnTile = tilePanels[pawn.getX()][pawn.getY()];
        JLabel pawnIcon = new JLabel();
        pawnIcon.setPreferredSize(new Dimension(TILE_SIZE / 2, TILE_SIZE / 2));
        pawnIcon.setOpaque(true);
        pawnIcon.setBackground(java.awt.Color.YELLOW); // Pawn color
        pawnTile.setLayout(new GridBagLayout());
        pawnTile.add(pawnIcon);

        revalidate();
        repaint();
    }

    private java.awt.Color getColorForTileType(TileType type) {
        switch (type) {
            case START:
                return java.awt.Color.WHITE;
            case PATH:
                return java.awt.Color.LIGHT_GRAY;
            case OBSTACLE:
                return java.awt.Color.DARK_GRAY;
            case TIMER:
                return java.awt.Color.RED;
            default:
                return java.awt.Color.WHITE;
        }
    }

    private java.awt.Color getColorForTile(Tile tile) {
        switch (tile.getColor()) {
            case ORANGE:
                return java.awt.Color.ORANGE;
            case PURPLE:
                return java.awt.Color.MAGENTA;
            case GREEN:
                return java.awt.Color.GREEN;
            case YELLOW:
                return java.awt.Color.YELLOW;
            default:
                return java.awt.Color.WHITE;
        }
    }

    private Border createTileBorder(Tile tile) {
        int thickness = 5; // Thickness of the wall
        int top = tile.hasWallUp() ? thickness : 0;
        int bottom = tile.hasWallDown() ? thickness : 0;
        int left = tile.hasWallLeft() ? thickness : 0;
        int right = tile.hasWallRight() ? thickness : 0;

        return BorderFactory.createMatteBorder(top, left, bottom, right, java.awt.Color.BLACK);
    }

    private void movePawn(String direction) {
        pawn.move(direction, board);

        // Ensure the pawn stays within bounds
        int x = Math.max(0, Math.min(pawn.getX(), tilePanels.length - 1));
        int y = Math.max(0, Math.min(pawn.getY(), tilePanels[0].length - 1));
        pawn = new Pawn(x, y);

        highlightPawn();

//        // Check if the pawn has reached the goal
//        if (board.get(pawn.getX()).get(pawn.getY()).getType() == TileType.GOAL) {
//            JOptionPane.showMessageDialog(this, "Congratulations! You reached the goal!");
//            System.exit(0); // End the program
//        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BoardSetUp boardSetUp = new BoardSetUp();
            new BoardUI(boardSetUp);
        });
    }
}