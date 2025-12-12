package org.game.ui;

import org.game.model.*;
import org.game.model.Color;
import org.game.model.board.Board;
import org.game.model.board.BoardEscalator;
import org.game.utils.Config;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardUI extends JFrame {
    private static final int TILE_SIZE = 28;
    private final JPanel[][] tilePanels;
    private LinePanel linePanel;
    private final Board board;
    private Map<TileType, ImageIcon> tileTypeImages;
    private final JPanel gridPanel;

    private final int numberOfRows;
    private final int numberOfCols;

    public BoardUI(Board board) {
        setTitle("Magic Maze Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.board = board;
        numberOfRows = board.getNumRows();
        numberOfCols = board.getNumCols();
        tilePanels = new JPanel[numberOfRows][numberOfCols];

        loadImages();

        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(numberOfRows+1, numberOfCols));

        // set content of the first row to be the top border of the board, showing timer
        for (int j = 0; j < numberOfCols; j++) {
            JPanel tilePanel = new JPanel();
            tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
            if(j == numberOfCols / 2){
                // fetch game.getTimer().getTimeLeftInTimer() every second and display it
                JLabel timerLabel = new JLabel(""+board.getTimer().getTimeLeftInTimer());
                timerLabel.setFont(new Font("Arial", Font.BOLD, 12));
                tilePanel.setLayout(new BorderLayout());
                tilePanel.add(timerLabel, BorderLayout.CENTER);
                // update timer every second
                new Timer(1000, _ -> timerLabel.setText(""+board.getTimer().getTimeLeftInTimer())).start();
                tilePanel.setBackground(java.awt.Color.LIGHT_GRAY);

            }
            else{
                tilePanel.setBackground(java.awt.Color.LIGHT_GRAY);
            }
            tilePanels[0][j] = tilePanel;
            gridPanel.add(tilePanel);
        }

        // Initialize the board and find the START coordinate
        for (int i = 1; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfCols; j++) {
                Tile tile = board.getTiles()[i][j];
                JPanel tilePanel = new JPanel();
                tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                if(tile == null){
                    tilePanel.setBackground(java.awt.Color.LIGHT_GRAY);
                }
                else{
                    if(Config.PRINT_EVERYTHING){
                        System.out.println("Rendering tile at: (" + i + ", " + j + ") of type " + tile.getType());
                    }
                    java.awt.Color bgColor;
                    if(tile.getColor() != Color.NONE){
                        bgColor = getColorForTile(tile);
                    }
                    else{
                        bgColor = getColorForTileType(tile.getType(), tile);
                    }

                    ImageIcon tileImage = tileTypeImages.get(tile.getType());
                    if (tileImage != null) {
                        tilePanel = new ImagePanel(tileImage.getImage(), 0.6, bgColor);
                    } else {
                        tilePanel = new JPanel();
                        tilePanel.setBackground(bgColor);
                        tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                    }

                    if(tile.getType() == TileType.VORTEX){
                        // Add cardId as a JLabel on top of the tile
                        JLabel cardIdLabel = new JLabel(String.valueOf(tile.getCardId()));
                        cardIdLabel.setFont(new Font("Arial", Font.BOLD, 9));
                        cardIdLabel.setForeground(java.awt.Color.BLACK);
                        cardIdLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        cardIdLabel.setVerticalAlignment(SwingConstants.CENTER);

                        tilePanel.setLayout(new BorderLayout());
                        tilePanel.add(cardIdLabel, BorderLayout.CENTER);
                    }

                    tilePanel.setBorder(createTileBorder(tile));
                }
                tilePanels[i][j] = tilePanel;
                gridPanel.add(tilePanel);
            }
        }

        // Initialize the pawn at the START coordinate
        List<Pawn> allPawns = board.getPawns();
        for (Pawn pawn : allPawns) {
            highlightPawn(pawn);
        }

        initializeLinePanel();
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(gridPanel, Integer.valueOf(0));
        layeredPane.add(linePanel, Integer.valueOf(1));

        setContentPane(layeredPane);
        setVisible(true);

        drawEscalators();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadImages() {
        // Load images for each TileType
        tileTypeImages = new HashMap<>();
        loadIndividualImage("images/vortex.png", TileType.VORTEX);
        loadIndividualImage("images/discover.png", TileType.DISCOVERY);
        loadIndividualImage("images/exit.png", TileType.GOAL_EXIT);
        loadIndividualImage("images/item.png", TileType.GOAL_ITEM);
    }

    private void loadIndividualImage(String path, TileType type) {
        BufferedImage image;
        try{
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        tileTypeImages.put(type, new ImageIcon(image));
    }

    private void drawEscalators() {
        for (BoardEscalator escalator : board.getEscalators()) {
            Coordinate start = escalator.getStart();
            Coordinate end = escalator.getEnd();
            if (start != null && end != null) {
                drawLineBetweenTiles(start.x(), start.y(), end.x(), end.y());
            }
        }
    }

    public void highlightPawn(Pawn pawn) {
        // Highlight the pawn's coordinate
        JPanel pawnTile = getTilePanelAt(pawn.getCoordinate());

        // if there is a vortex number on the tile, remove it
        if(pawnTile.getComponentCount() > 0){
            for (Component child : pawnTile.getComponents()) {
                if (child instanceof JLabel) {
                    pawnTile.remove(child);
                }
            }
        }

        if(Config.PRINT_EVERYTHING){
            System.out.println("Highlighting pawn at: " + pawn.getCoordinate());
        }
        JLabel pawnIcon = new JLabel();
        pawnIcon.setPreferredSize(new Dimension((2*TILE_SIZE) / 3, 2*(TILE_SIZE) / 3));
        pawnIcon.setOpaque(true);
        Color pawnColor = pawn.getColor();
        pawnIcon.setBackground(java.awt.Color.decode(pawnColor.getHexCode())); // Pawn color
        // put black border around
        pawnIcon.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));
        pawnTile.setLayout(new GridBagLayout());
        pawnTile.add(pawnIcon);

        revalidate();
        repaint();
    }

    public void unhighlightPawn(Pawn pawn){
        if(Config.PRINT_EVERYTHING){
            System.out.println("Un-highlighting pawn at: " + pawn.getCoordinate());
        }
        // get children of tilePanels component at x and y

        JPanel pawnTile = getTilePanelAt(pawn.getCoordinate());

        for (Component child : pawnTile.getComponents()) {
            if (child instanceof JLabel) {
                pawnTile.remove(child);
            }
        }

        // re-print the vortex number if it is a vortex
        Tile tile = board.getTileAt(pawn.getCoordinate());
        visualizeVortexTile(tile, pawnTile);

        //tilePanels[pawn.getX()][pawn.getY()].removeAll();
        getTilePanelAt(pawn.getCoordinate()).setLayout(new GridBagLayout());
        getTilePanelAt(pawn.getCoordinate()).revalidate();
        getTilePanelAt(pawn.getCoordinate()).repaint();
        revalidate();
        repaint();
    }

    private java.awt.Color getColorForTileType(TileType type, Tile tile) {
        return switch (type) {
            case START -> java.awt.Color.WHITE;
            case OBSTACLE -> java.awt.Color.decode(Color.BROWN.getHexCode());
            case TIMER -> {
                if (tile.isUsed()) {
                    yield java.awt.Color.decode(Color.DARK_RED.getHexCode());
                }
                yield java.awt.Color.decode(Color.RED.getHexCode());
            }
            default -> java.awt.Color.decode(Color.NONE.getHexCode());
        };
    }

    private java.awt.Color getColorForTile(Tile tile) {
        return switch (tile.getColor()) {
            case ORANGE -> java.awt.Color.decode(Color.ORANGE.getHexCode());
            case PURPLE -> java.awt.Color.decode(Color.PURPLE.getHexCode());
            case GREEN -> java.awt.Color.decode(Color.GREEN.getHexCode());
            case YELLOW -> java.awt.Color.decode(Color.YELLOW.getHexCode());
            default -> java.awt.Color.decode(Color.NONE.getHexCode());
        };
    }

    private Border createTileBorder(Tile tile) {
        int thickness = 2; // Thickness of the wall
        int top = tile.hasWallUp() ? thickness : 0;
        int bottom = tile.hasWallDown() ? thickness : 0;
        int left = tile.hasWallLeft() ? thickness : 0;
        int right = tile.hasWallRight() ? thickness : 0;

        return BorderFactory.createMatteBorder(top, left, bottom, right, java.awt.Color.decode(Color.BROWN.getHexCode()));
    }

    public void renderDiscoveredTiles(Coordinate corner) {
        int startX = corner.x();
        int startY = corner.y();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int boardX = startX + i;
                int boardY = startY + j;
                if (boardX >= 0 && boardX < numberOfRows && boardY >= 0 && boardY < numberOfCols) {
                    Tile tile = board.getTiles()[boardX][boardY];
                    if (tile != null) {
                        JPanel tilePanel;
                        java.awt.Color bgColor;
                        if(tile.getColor() != Color.NONE){
                            bgColor = getColorForTile(tile);
                        }
                        else{
                            bgColor = getColorForTileType(tile.getType(), tile);
                        }

                        if(tile.hasEscalator()){
                            if(Config.PRINT_EVERYTHING){
                                System.out.println("Rendering escalator at: (" + boardX + ", " + boardY + ")");
                            }
                        }

                        ImageIcon tileImage = tileTypeImages.get(tile.getType());
                        if (tileImage != null) {
                            tilePanel = new ImagePanel(tileImage.getImage(), 0.8, bgColor);
                        } else {
                            tilePanel = new JPanel();
                            tilePanel.setBackground(bgColor);
                            tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                        }

                        visualizeVortexTile(tile, tilePanel);

                        tilePanel.setBorder(createTileBorder(tile));
                        tilePanels[boardX][boardY] = tilePanel;
                        // replace the panel in the grid layout
                        gridPanel.remove(boardX * numberOfCols + boardY);
                        gridPanel.add(tilePanel, boardX * numberOfCols + boardY);
                    }
                }
            }
        }
        drawEscalators();
        revalidate();
        repaint();
    }

    private void visualizeVortexTile(Tile tile, JPanel tilePanel) {
        if(tile.getType() == TileType.VORTEX){
            // Add cardId as a JLabel on top of the tile
            JLabel cardIdLabel = new JLabel(String.valueOf(tile.getCardId()));
            cardIdLabel.setFont(new Font("Arial", Font.BOLD, 12));
            cardIdLabel.setForeground(java.awt.Color.BLACK);
            cardIdLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cardIdLabel.setVerticalAlignment(SwingConstants.CENTER);

            tilePanel.setLayout(new BorderLayout());
            tilePanel.add(cardIdLabel, BorderLayout.CENTER);
        }
    }

    private void initializeLinePanel() {
        linePanel = new LinePanel();
        linePanel.setOpaque(false); // Make it transparent
        linePanel.setBounds(0, 0, getWidth(), getHeight()); // Match the board size
    }

    private void drawLineBetweenTiles(int x1, int y1, int x2, int y2) {
        // switch x and y
        int temp = x1;
        x1 = y1;
        y1 = temp;
        temp = x2;
        x2 = y2;
        y2 = temp;


        int startX = x1 * TILE_SIZE + TILE_SIZE / 2;
        int startY = y1 * TILE_SIZE + TILE_SIZE / 2;
        int endX = x2 * TILE_SIZE + TILE_SIZE / 2;
        int endY = y2 * TILE_SIZE + TILE_SIZE / 2;

        linePanel.addLine(startX, startY, endX, endY);
    }

    private JPanel getTilePanelAt(Coordinate coordinate) {
        return tilePanels[coordinate.x()][coordinate.y()];
    }

    public void changeTimerColorToDark(Coordinate coordinate){
        // change color of timer tile to dark red
        Tile timerTile = board.getTileAt(coordinate);
        JPanel timerTilePanel = getTilePanelAt(coordinate);
        java.awt.Color bgColor = getColorForTileType(timerTile.getType(), timerTile);
        timerTilePanel.setBackground(bgColor);
    }

    public JPanel getTilePanelAt(int x, int y) {
        return tilePanels[x][y];
    }
}