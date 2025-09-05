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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BoardUI extends JFrame {
    private static final int TILE_SIZE = 50;
    private JPanel[][] tilePanels;
    private Board board;
    private java.util.Map<TileType, ImageIcon> tileTypeImages;

    public BoardUI(Game game) {
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


        setLayout(new GridLayout(rows, cols));

        // Initialize the board and find the START position
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile tile = board.getTiles()[i][j];
                JPanel tilePanel = new JPanel();
                tilePanel.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                if(tile == null){
                    tilePanel.setBackground(java.awt.Color.LIGHT_GRAY);
                    tilePanels[i][j] = tilePanel;
                    add(tilePanel);
                    continue;
                }
                else{
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
                }
            }
        }

        // Initialize the pawn at the START position
        List<Pawn> allPawns = board.getPawns();
        for (Pawn pawn : allPawns) {
            highlightPawn(pawn);
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
                        movePawn(pawnColor, direction);
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
        Color pawnColor = pawn.getColor();
        pawnIcon.setBackground(java.awt.Color.decode(pawnColor.getHexCode())); // Pawn color
        // put black border around
        pawnIcon.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 2));
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
                return java.awt.Color.decode(Color.BROWN.getHexCode());
            case TIMER:
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

        return BorderFactory.createMatteBorder(top, left, bottom, right, java.awt.Color.BLACK);
    }

    private void movePawn(Color pawnColor, String direction) {
        Pawn previousPawn = new Pawn(board.getPawnByColor(pawnColor));
        Pawn updatedPawn = board.movePawn(pawnColor, direction);
        board.printAllPawns();

        unhighlightPawn(previousPawn);
        highlightPawn(updatedPawn);

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
            Game game = new Game();
            new BoardUI(game);
        });
    }
}