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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BoardUI extends JFrame {
    private final Game game;
    private static final int TILE_SIZE = 30;
    private JPanel[][] tilePanels;
    private LinePanel linePanel;
    private Board board;
    private java.util.Map<TileType, ImageIcon> tileTypeImages;
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
        tileTypeImages = new java.util.HashMap<>();
        BufferedImage vortexImage = null;
        BufferedImage discoverImage = null;
        try {
            vortexImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/vortex.png"));
            discoverImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/discover.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tileTypeImages.put(TileType.VORTEX, new ImageIcon(vortexImage));
        tileTypeImages.put(TileType.DISCOVERY, new ImageIcon(discoverImage));


        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(rows, cols));

        // Initialize the board and find the START position
        for (int i = 0; i < rows; i++) {
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
                    if(tile.getColor() != org.game.model.Color.NONE){
                        bgColor = getColorForTile(tile);
                    }
                    else{
                        bgColor = getColorForTileType(tile.getType());
                    }
                    System.out.println("escalator: " + tile.getEscalator());

                    ImageIcon tileImage = tileTypeImages.get(tile.getType());
                    if (tileImage != null) {
                        tilePanel = new ImagePanel(tileImage.getImage(), 0.8, bgColor);
                    } else {
                        tilePanel = new JPanel();
                        tilePanel.setBackground(bgColor);
                        tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
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

        for (BoardEscalator escalator : board.getEscalators()) {
            Coordinate start = escalator.getStart();
            Coordinate end = escalator.getEnd();
            if (start != null && end != null) {
                drawLineBetweenTiles(start.getX(), start.getY(), end.getX(), end.getY());
            }
        }

        new Thread(() -> {
            try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
                while (true) {
                    System.out.print("Enter color of pawn (y: yellow, o: orange, p: purple, g: green) and direction (n: north, s: south, w: west, e: east): ");
                    String input = scanner.nextLine().trim().toLowerCase();
                    // split input into color and direction
                    if (input.length() != 2) {
                        System.out.println("Invalid input. Please provide both color and direction.");
                        continue;
                    }
                    String colorString = String.valueOf(input.charAt(0));
                    String actionString = String.valueOf(input.charAt(1));
                    org.game.model.Color pawnColor = switch (colorString) {
                        case "y" -> org.game.model.Color.YELLOW;
                        case "o" -> org.game.model.Color.ORANGE;
                        case "p" -> org.game.model.Color.PURPLE;
                        case "g" -> org.game.model.Color.GREEN;
                        default -> null;
                    };
                    org.game.model.Action action = switch (actionString) {
                        case "n" -> org.game.model.Action.MOVE_NORTH;
                        case "s" -> org.game.model.Action.MOVE_SOUTH;
                        case "w" -> org.game.model.Action.MOVE_WEST;
                        case "e" -> org.game.model.Action.MOVE_EAST;
                        case "d" -> org.game.model.Action.DISCOVER;
                        case "v" -> org.game.model.Action.VORTEX;
                        case "x" -> org.game.model.Action.ESCALATOR;
                        default -> null;
                    };

                    if (action != null && pawnColor != null) {
                        movePawn(pawnColor, action);
                    } else {
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

    private void highlightPawn(Pawn pawn) {
        // Highlight the pawn's position
        JPanel pawnTile = tilePanels[pawn.getX()][pawn.getY()];
        System.out.println("Highlighting pawn at: (" + pawn.getX() + ", " + pawn.getY() + ")");
        JLabel pawnIcon = new JLabel();
        pawnIcon.setPreferredSize(new Dimension(TILE_SIZE / 2, TILE_SIZE / 2));
        pawnIcon.setOpaque(true);
        org.game.model.Color pawnColor = pawn.getColor();
        pawnIcon.setBackground(java.awt.Color.decode(pawnColor.getHexCode())); // Pawn color
        // put black border around
        pawnIcon.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));
        pawnTile.setLayout(new GridBagLayout());
        pawnTile.add(pawnIcon);

        revalidate();
        repaint();
    }

    private void unhighlightPawn(Pawn pawn){
        System.out.println("Un-highlighting pawn at: (" + pawn.getX() + ", " + pawn.getY() + ")");
        // get children of tilePanels component at x and y

        JPanel pawnTile = tilePanels[pawn.getX()][pawn.getY()];

        for (Component child : pawnTile.getComponents()) {
            if (child instanceof JLabel) {
                pawnTile.remove(child);
            }
        }

        //tilePanels[pawn.getX()][pawn.getY()].removeAll();
        tilePanels[pawn.getX()][pawn.getY()].setLayout(new GridBagLayout());
        tilePanels[pawn.getX()][pawn.getY()].revalidate();
        tilePanels[pawn.getX()][pawn.getY()].repaint();
        revalidate();
        repaint();


    }

    private java.awt.Color getColorForTileType(TileType type) {
        switch (type) {
            case START:
                return java.awt.Color.WHITE;
            case OBSTACLE:
                return java.awt.Color.decode(org.game.model.Color.BROWN.getHexCode());
            case TIMER:
                return java.awt.Color.decode(org.game.model.Color.RED.getHexCode());
            case PATH:
            default:
                return java.awt.Color.decode(org.game.model.Color.NONE.getHexCode());
        }
    }

    private java.awt.Color getColorForTile(Tile tile) {
        switch (tile.getColor()) {
            case ORANGE:
                return java.awt.Color.decode(org.game.model.Color.ORANGE.getHexCode());
            case PURPLE:
                return java.awt.Color.decode(org.game.model.Color.PURPLE.getHexCode());
            case GREEN:
                return java.awt.Color.decode(org.game.model.Color.GREEN.getHexCode());
            case YELLOW:
                return java.awt.Color.decode(org.game.model.Color.YELLOW.getHexCode());
            default:
                return java.awt.Color.decode(org.game.model.Color.NONE.getHexCode());
        }
    }

    private Border createTileBorder(Tile tile) {
        int thickness = 2; // Thickness of the wall
        int top = tile.hasWallUp() ? thickness : 0;
        int bottom = tile.hasWallDown() ? thickness : 0;
        int left = tile.hasWallLeft() ? thickness : 0;
        int right = tile.hasWallRight() ? thickness : 0;

        return BorderFactory.createMatteBorder(top, left, bottom, right, java.awt.Color.decode(org.game.model.Color.BROWN.getHexCode()));
    }

    private void movePawn(org.game.model.Color pawnColor, org.game.model.Action action) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn;
        if(action == org.game.model.Action.MOVE_EAST || action == org.game.model.Action.MOVE_WEST || action == org.game.model.Action.MOVE_NORTH || action == org.game.model.Action.MOVE_SOUTH){
            updatedPawn = board.movePawn(pawnColor, action);
        }
        else if(action == org.game.model.Action.ESCALATOR){
            updatedPawn = board.useEscalator(pawnColor);
            if(updatedPawn.equals(previousPawn)){
                System.out.println("No escalator to use for pawn " + pawnColor);
            }
        }
        else if(action == org.game.model.Action.DISCOVER){
            updatedPawn = previousPawn;
            // check if the pawn is standing on discovery tile
            Tile currentTile = board.getTileAt(updatedPawn.getX(), updatedPawn.getY());
            if(currentTile.getType() == TileType.DISCOVERY && pawnColor == currentTile.getColor()){
                Coordinate corner = board.getLeftTopCornerOfNewCard(new Coordinate(updatedPawn.getX(), updatedPawn.getY()));
                game.discoverCard(new Coordinate(updatedPawn.getX(), updatedPawn.getY()));
                // re-render the board
                renderDiscoveredTiles(corner);
            }
        }
        else if(action == Action.VORTEX){
            System.out.println("not implemented yet");
            updatedPawn = previousPawn;
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
                            bgColor = getColorForTileType(tile.getType());
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

                        tilePanel.setBorder(createTileBorder(tile));
                        tilePanels[boardX][boardY] = tilePanel;
                        // replace the panel in the grid layout
                        gridPanel.remove(boardX * board.getNumCols() + boardY);
                        gridPanel.add(tilePanel, boardX * board.getNumCols() + boardY);
                    }
                }
            }
        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            new BoardUI(game);
        });
    }
}