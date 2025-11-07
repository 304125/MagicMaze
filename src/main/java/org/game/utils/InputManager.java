package org.game.utils;

import org.game.model.Action;
import org.game.model.Color;

import java.util.Scanner;

public class InputManager {
    ActionDelegator actionDelegator;

    public InputManager(ActionDelegator actionDelegator) {
        readInput();
        this.actionDelegator = actionDelegator;
    }

    public void readInput(){
        Thread executionThread = new Thread(() -> {
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

                    if (action == Action.DISCOVER && pawnColor != null) {
                        actionDelegator.discover(pawnColor);
                    }
                    else

                    if (action != null && pawnColor != null && vortexNumber == 0) {
                        actionDelegator.movePawn(pawnColor, action);
                    }
                    // vortexNumber != 0
                    else if (action == Action.VORTEX && pawnColor != null){
                        actionDelegator.vortexPawn(pawnColor, vortexNumber);
                    }
                    else {
                        System.out.println("Invalid input. Please provide both color and direction in correct format.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executionThread.setDaemon(true); // Optional: Set as daemon thread
        executionThread.start();
    }


}
