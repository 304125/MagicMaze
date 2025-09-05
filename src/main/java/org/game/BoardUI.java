package org.game;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BoardUI extends JFrame {
    private static final int TILE_SIZE = 50;
    private JPanel[][] tilePanels;
    private Pawn yello_pawn;
    private Pawn orange_pawn;
    private Pawn purple_pawn;
    private Pawn green_pawn;
    private List<Pawn> allPawns;
    private List<List<Tile>> board;
    private java.util.Map<TileType, ImageIcon> tileTypeImages;

    public BoardUI(BoardSetUp boardSetUp) {
        setTitle("Magic Maze Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.board = boardSetUp.getBoard(); // Initialize the board
        int rows = board.size();
        int cols = board.get(0).size();
        tilePanels = new JPanel[rows][cols];

        // Load images for each TileType
        tileTypeImages = new java.util.HashMap<>();
        BufferedImage vortexImage = null;
        try {
            vortexImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/vortex.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tileTypeImages.put(TileType.VORTEX, new ImageIcon(vortexImage));
//        tileTypeImages.put(TileType.VORTEX, new ImageIcon(renderSVGToBufferedImage("images/vortex.svg", TILE_SIZE, TILE_SIZE)));


        setLayout(new GridLayout(rows, cols));

        // Initialize the board and find the START position
        int startX = 0, startY = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile tile = board.get(i).get(j);
                JPanel tilePanel = new JPanel();
                tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                java.awt.Color bgColor;
                if(tile.getColor() != Color.NONE){
                    bgColor = getColorForTile(tile);
                }
                else{
                    bgColor = getColorForTileType(tile.getType());
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
                tilePanels[i][j] = tilePanel;
                add(tilePanel);

                if (tile.getType() == TileType.START) {
                    startX = i;
                    startY = j;
                }
            }
        }

        // Initialize the pawn at the START position
        allPawns = new java.util.ArrayList<>();
        yello_pawn = new Pawn(startX, startY, Color.YELLOW);
        allPawns.add(yello_pawn);
        highlightPawn(yello_pawn);

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
                    String directionString = String.valueOf(input.charAt(1));
                    Color pawnColor = switch (colorString) {
                        case "y" -> Color.YELLOW;
                        case "o" -> Color.ORANGE;
                        case "p" -> Color.PURPLE;
                        case "g" -> Color.GREEN;
                        default -> null;
                    };
                    String direction = switch (directionString) {
                        case "n", "s", "w", "e" -> directionString; // Return the input directly if it's valid
                        default -> null;
                    };

                    if (direction != null && pawnColor != null) {
                        boolean movePerformed = movePawn(pawnColor, direction);
                        if(movePerformed) {
                            System.out.println("Moved " + direction.toUpperCase() + " pawn of color " + pawnColor);
                        } else {
                            System.out.println("Could not move pawn of color " + pawnColor + " in direction " + direction.toUpperCase());
                        }
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
        int thickness = 2; // Thickness of the wall
        int top = tile.hasWallUp() ? thickness : 0;
        int bottom = tile.hasWallDown() ? thickness : 0;
        int left = tile.hasWallLeft() ? thickness : 0;
        int right = tile.hasWallRight() ? thickness : 0;

        return BorderFactory.createMatteBorder(top, left, bottom, right, java.awt.Color.BLACK);
    }

    private boolean movePawn(Color pawnColor, String direction) {
        // get pawn from allPawns where pawnColor == pawn.Color
        Pawn pawn = allPawns.stream().filter(p -> p.getColor() == pawnColor).findFirst().orElse(null);

        if(pawn == null){
            System.out.println("Pawn of color " + pawnColor + " not found.");
            return false;
        }

        int oldX = pawn.getX();
        int oldY = pawn.getY();

        pawn.move(direction, board);

        // Ensure the pawn stays within bounds
        int x = Math.max(0, Math.min(pawn.getX(), tilePanels.length - 1));
        int y = Math.max(0, Math.min(pawn.getY(), tilePanels[0].length - 1));
        pawn = new Pawn(x, y, pawn.getColor());

        highlightPawn(pawn);

        if(oldX == pawn.getX() && oldY == pawn.getY()) {
            return false;
        } else {
            return true;
        }

//        // Check if the pawn has reached the goal
//        if (board.get(pawn.getX()).get(pawn.getY()).getType() == TileType.GOAL) {
//            JOptionPane.showMessageDialog(this, "Congratulations! You reached the goal!");
//            System.exit(0); // End the program
//        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BoardSetUp boardSetUp = new BoardSetUp();
            new BoardUI(boardSetUp);
        });
    }
}