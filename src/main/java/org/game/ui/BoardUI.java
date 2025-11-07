package org.game.ui;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.game.model.*;
import org.game.model.Action;
import org.game.model.Color;
import org.game.model.board.Board;
import org.game.model.board.BoardEscalator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BoardUI extends JFrame {
    private final Game game;
    private static final int TILE_SIZE = 30;
    private JPanel[][] tilePanels;
    private LinePanel linePanel;
    private Board board;
    private Map<TileType, ImageIcon> tileTypeImages;
    private JPanel gridPanel;

    public BoardUI(Game game) {
        this.game = game;
        setTitle("Magic Maze Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.board = game.getBoard(); // Initialize the board
        int rows = board.getNumRows();
        int cols = board.getNumCols();
        tilePanels = new JPanel[rows][cols];

        // Load images for each TileType
        tileTypeImages = new HashMap<>();
        BufferedImage vortexImage = null;
        BufferedImage discoverImage = null;
        BufferedImage exitImage = null;
        BufferedImage itemImage = null;
        try {
            vortexImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/vortex.png"));
            discoverImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/discover.png"));
            exitImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/exit.png"));
            itemImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/item.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tileTypeImages.put(TileType.VORTEX, new ImageIcon(vortexImage));
        tileTypeImages.put(TileType.DISCOVERY, new ImageIcon(discoverImage));
        tileTypeImages.put(TileType.GOAL_EXIT, new ImageIcon(exitImage));
        tileTypeImages.put(TileType.GOAL_ITEM, new ImageIcon(itemImage));


        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(rows+1, cols));

        // set content of the first row to be the top border of the board, showing timer
        for (int j = 0; j < cols; j++) {
            JPanel tilePanel = new JPanel();
            tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
            if(j == cols / 2){
                // fetch game.getTimer().getTimeLeftInTimer() every second and display it
                JLabel timerLabel = new JLabel(""+game.getBoard().getTimer().getTimeLeftInTimer());
                timerLabel.setFont(new Font("Arial", Font.BOLD, 12));
                tilePanel.setLayout(new BorderLayout());
                tilePanel.add(timerLabel, BorderLayout.CENTER);
                // update timer every second
                new Timer(1000, e -> timerLabel.setText(""+game.getBoard().getTimer().getTimeLeftInTimer())).start();
                tilePanel.setBackground(java.awt.Color.LIGHT_GRAY);

            }
            else{
                tilePanel.setBackground(java.awt.Color.LIGHT_GRAY);
            }
            tilePanels[0][j] = tilePanel;
            gridPanel.add(tilePanel);
        }

        // Initialize the board and find the START position
        for (int i = 1; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile tile = board.getTiles()[i][j];
                JPanel tilePanel = new JPanel();
                tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                if(tile == null){
                    tilePanel.setBackground(java.awt.Color.LIGHT_GRAY);
                    tilePanels[i][j] = tilePanel;
                    gridPanel.add(tilePanel);
                    continue;
                }
                else{
                    System.out.println("Rendering tile at: (" + i + ", " + j + ") of type " + tile.getType());
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
                    tilePanels[i][j] = tilePanel;
                    gridPanel.add(tilePanel);
                }
            }
        }

        // Initialize the pawn at the START position
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

        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("Enter color of pawn (y: yellow, o: orange, p: purple, g: green) and direction (n: north, s: south, w: west, e: east) or action (d: discover, v: vortex, x: escalator):");
                    String input = scanner.nextLine().trim().toLowerCase();
                    // split input into color and direction
                    if (input.length() != 2 && input.length() != 3 && input.length() != 4) {
                        System.out.println("Invalid input. Please provide both color and direction.");
                        continue;
                    }
                    String colorString = String.valueOf(input.charAt(0));
                    String actionString = String.valueOf(input.charAt(1));
                    int vortexNumber = 0;
                    try {
                        if (input.length() == 3) {
                            vortexNumber = Character.getNumericValue(input.charAt(2));
                        } else if (input.length() == 4) {
                            vortexNumber = Integer.parseInt(input.substring(2, 4));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid vortex number. Please enter a valid number.");
                        continue;
                    }

                    Color pawnColor = switch (colorString) {
                        case "y" -> Color.YELLOW;
                        case "o" -> Color.ORANGE;
                        case "p" -> Color.PURPLE;
                        case "g" -> Color.GREEN;
                        default -> null;
                    };
                    Action action = switch (actionString) {
                        case "n" -> Action.MOVE_NORTH;
                        case "s" -> Action.MOVE_SOUTH;
                        case "w" -> Action.MOVE_WEST;
                        case "e" -> Action.MOVE_EAST;
                        case "d" -> Action.DISCOVER;
                        case "v" -> Action.VORTEX;
                        case "x" -> Action.ESCALATOR;
                        default -> null;
                    };

                    //
                    if ((vortexNumber != 0 && action != Action.VORTEX) || vortexNumber == 0 && action == Action.VORTEX) {
                        System.out.println("Invalid input. Vortex action requires a number (e.g., 'yv1' for yellow vortex 1).");
                        continue;
                    }

                    if (action != null && pawnColor != null && vortexNumber == 0) {
                        movePawn(pawnColor, action);
                    }
                    // vortexNumber != 0
                    else if (action == Action.VORTEX && pawnColor != null){
                        vortexPawn(pawnColor, vortexNumber);
                    }
                    else {
                        System.out.println("Invalid input. Please provide both color and direction in correct format.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void drawEscalators() {
        for (BoardEscalator escalator : board.getEscalators()) {
            Coordinate start = escalator.getStart();
            Coordinate end = escalator.getEnd();
            if (start != null && end != null) {
                drawLineBetweenTiles(start.getX(), start.getY(), end.getX(), end.getY());
            }
        }
    }

    private void highlightPawn(Pawn pawn) {
        // Highlight the pawn's position
        JPanel pawnTile = getTilePanelAt(pawn.getCoordinate());

        // if there is a vortex number on the tile, remove it
        if(pawnTile.getComponentCount() > 0){
            for (Component child : pawnTile.getComponents()) {
                if (child instanceof JLabel) {
                    pawnTile.remove(child);
                }
            }
        }

        System.out.println("Highlighting pawn at: " + pawn.getCoordinate());
        JLabel pawnIcon = new JLabel();
        pawnIcon.setPreferredSize(new Dimension(TILE_SIZE / 2, TILE_SIZE / 2));
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

    private void unhighlightPawn(Pawn pawn){
        System.out.println("Un-highlighting pawn at: " + pawn.getCoordinate());
        // get children of tilePanels component at x and y

        JPanel pawnTile = getTilePanelAt(pawn.getCoordinate());

        for (Component child : pawnTile.getComponents()) {
            if (child instanceof JLabel) {
                pawnTile.remove(child);
            }
        }

        // re-print the vortex number if it is a vortex
        Tile tile = board.getTileAt(pawn.getCoordinate());
        if(tile.getType() == TileType.VORTEX){
            // Add cardId as a JLabel on top of the tile
            JLabel cardIdLabel = new JLabel(String.valueOf(tile.getCardId()));
            cardIdLabel.setFont(new Font("Arial", Font.BOLD, 12));
            cardIdLabel.setForeground(java.awt.Color.BLACK);
            cardIdLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cardIdLabel.setVerticalAlignment(SwingConstants.CENTER);

            pawnTile.setLayout(new BorderLayout());
            pawnTile.add(cardIdLabel, BorderLayout.CENTER);
        }

        //tilePanels[pawn.getX()][pawn.getY()].removeAll();
        getTilePanelAt(pawn.getCoordinate()).setLayout(new GridBagLayout());
        getTilePanelAt(pawn.getCoordinate()).revalidate();
        getTilePanelAt(pawn.getCoordinate()).repaint();
        revalidate();
        repaint();


    }

    private java.awt.Color getColorForTileType(TileType type, Tile tile) {
        switch (type) {
            case START:
                return java.awt.Color.WHITE;
            case OBSTACLE:
                return java.awt.Color.decode(Color.BROWN.getHexCode());
            case TIMER:
                if(tile.isUsed()){
                    return java.awt.Color.decode(Color.DARK_RED.getHexCode());
                }
                return java.awt.Color.decode(Color.RED.getHexCode());
            case PATH:
            default:
                return java.awt.Color.decode(Color.NONE.getHexCode());
        }
    }

    private java.awt.Color getColorForTile(Tile tile) {
        switch (tile.getColor()) {
            case ORANGE:
                return java.awt.Color.decode(Color.ORANGE.getHexCode());
            case PURPLE:
                return java.awt.Color.decode(Color.PURPLE.getHexCode());
            case GREEN:
                return java.awt.Color.decode(Color.GREEN.getHexCode());
            case YELLOW:
                return java.awt.Color.decode(Color.YELLOW.getHexCode());
            default:
                return java.awt.Color.decode(Color.NONE.getHexCode());
        }
    }

    private Border createTileBorder(Tile tile) {
        int thickness = 2; // Thickness of the wall
        int top = tile.hasWallUp() ? thickness : 0;
        int bottom = tile.hasWallDown() ? thickness : 0;
        int left = tile.hasWallLeft() ? thickness : 0;
        int right = tile.hasWallRight() ? thickness : 0;

        return BorderFactory.createMatteBorder(top, left, bottom, right, java.awt.Color.decode(Color.BROWN.getHexCode()));
    }

    private void movePawn(Color pawnColor, Action action) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn;
        if(action == Action.MOVE_EAST || action == Action.MOVE_WEST || action == Action.MOVE_NORTH || action == Action.MOVE_SOUTH){
            updatedPawn = board.movePawn(pawnColor, action);
            if(board.isPawnAtTimerTile(updatedPawn)){
                // change color of timer tile to dark red
                Tile timerTile = board.getTileAt(updatedPawn.getCoordinate());
                JPanel timerTilePanel = getTilePanelAt(updatedPawn.getCoordinate());
                java.awt.Color bgColor = getColorForTileType(timerTile.getType(), timerTile);
                timerTilePanel.setBackground(bgColor);
            }
        }
        else if(action == Action.ESCALATOR){
            updatedPawn = board.useEscalator(pawnColor);
            if(updatedPawn.equals(previousPawn)){
                System.out.println("No escalator to use for pawn " + pawnColor);
            }
        }
        else if(action == Action.DISCOVER){
            updatedPawn = previousPawn;
            // check if the pawn is standing on discovery tile
            Tile currentTile = board.getTileAt(updatedPawn.getCoordinate());
            if(currentTile.getType() == TileType.DISCOVERY && pawnColor == currentTile.getColor()){
                Coordinate corner = board.getLeftTopCornerOfNewCard(updatedPawn.getCoordinate());
                boolean discovered = game.discoverCard(updatedPawn.getCoordinate());
                // re-render the board
                if(discovered){
                    renderDiscoveredTiles(corner);
                }
            }
        }
        else{
            System.out.println("Unknown action");
            updatedPawn = previousPawn;
            return;
        }

        board.printAllPawns();

        unhighlightPawn(previousPawn);
        highlightPawn(updatedPawn);
    }

    private void vortexPawn(Color pawnColor, int vortexNumber) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn = board.useVortex(pawnColor, vortexNumber);
        if(updatedPawn.equals(previousPawn)){
            System.out.println("No vortex to use for pawn " + pawnColor + " with number " + vortexNumber);
            return;
        }

        board.printAllPawns();

        unhighlightPawn(previousPawn);
        highlightPawn(updatedPawn);
    }

    private void renderDiscoveredTiles(Coordinate corner) {
        int startX = corner.getX();
        int startY = corner.getY();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int boardX = startX + i;
                int boardY = startY + j;
                if (boardX >= 0 && boardX < board.getNumRows() && boardY >= 0 && boardY < board.getNumCols()) {
                    Tile tile = board.getTiles()[boardX][boardY];
                    if (tile != null) {
                        JPanel tilePanel = tilePanels[boardX][boardY];
                        java.awt.Color bgColor;
                        if(tile.getColor() != Color.NONE){
                            bgColor = getColorForTile(tile);
                        }
                        else{
                            bgColor = getColorForTileType(tile.getType(), tile);
                        }

                        if(tile.hasEscalator()){
                            System.out.println("Rendering escalator at: (" + boardX + ", " + boardY + ")");
                        }

                        ImageIcon tileImage = tileTypeImages.get(tile.getType());
                        if (tileImage != null) {
                            tilePanel = new ImagePanel(tileImage.getImage(), 0.8, bgColor);
                        } else {
                            tilePanel = new JPanel();
                            tilePanel.setBackground(bgColor);
                            tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                        }

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

                        tilePanel.setBorder(createTileBorder(tile));
                        tilePanels[boardX][boardY] = tilePanel;
                        // replace the panel in the grid layout
                        gridPanel.remove(boardX * board.getNumCols() + boardY);
                        gridPanel.add(tilePanel, boardX * board.getNumCols() + boardY);
                    }
                }
            }
        }
        drawEscalators();
        revalidate();
        repaint();
    }


    private BufferedImage renderSVGToBufferedImage(String svgPath, int width, int height) {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            InputStream svgInputStream = getClass().getClassLoader().getResourceAsStream(svgPath);
            if (svgInputStream == null) {
                throw new RuntimeException("SVG file not found: " + svgPath);
            }

            var document = factory.createDocument(svgPath, svgInputStream);
            UserAgentAdapter userAgent = new UserAgentAdapter();
            BridgeContext bridgeContext = new BridgeContext(userAgent);
            bridgeContext.setDynamicState(BridgeContext.STATIC);
            GVTBuilder builder = new GVTBuilder();
            GraphicsNode graphicsNode = builder.build(bridgeContext, document);

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.scale((double) width / graphicsNode.getPrimitiveBounds().getWidth(),
                    (double) height / graphicsNode.getPrimitiveBounds().getHeight());
            graphicsNode.paint(g2d);
            g2d.dispose();

            return bufferedImage;
        } catch (Exception e) {
            throw new RuntimeException("Failed to render SVG: " + svgPath, e);
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
        return tilePanels[coordinate.getX()][coordinate.getY()];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            new BoardUI(game);
        });
    }
}