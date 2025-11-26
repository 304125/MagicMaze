package org.game.utils;

import org.game.model.Action;
import org.game.model.Color;

public class InputStringRenderer {
    private final ActionDelegator actionDelegator;

    public InputStringRenderer(ActionDelegator actionDelegator){
        this.actionDelegator = actionDelegator;
    }

    public void renderInputString(String input){
        // split input into color and direction
        if (input.length() != 2 && input.length() != 3 && input.length() != 4) {
            System.out.println("Invalid input. Please provide both color and direction.");
            return;
        }
        String colorString = String.valueOf(input.charAt(0));
        String actionString = String.valueOf(input.charAt(1));
        int optionalNumber = 0;
        try {
            if (input.length() == 3) {
                optionalNumber = Character.getNumericValue(input.charAt(2));
            } else if (input.length() == 4) {
                optionalNumber = Integer.parseInt(input.substring(2, 4));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid vortex number. Please enter a valid number.");
            return;
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
        if ((optionalNumber != 0 &&  action != Action.VORTEX && action != Action.DISCOVER) || (optionalNumber == 0 && action == Action.VORTEX)) {
            System.out.println("Invalid input. Vortex action requires a number (e.g., 'yv1' for yellow vortex 1).");
            return;
        }

        if (action == Action.DISCOVER && pawnColor != null) {
            if(optionalNumber != 0){
                actionDelegator.discoverGivenCard(pawnColor, optionalNumber);
            }
            else{
                actionDelegator.discoverRandomCard(pawnColor);
            }
        }
        else

        if (action != null && pawnColor != null && optionalNumber == 0) {
            actionDelegator.movePawn(pawnColor, action);
        }
        // optionalNumber != 0
        else if (action == Action.VORTEX && pawnColor != null){
            actionDelegator.vortexPawn(pawnColor, optionalNumber);
        }
        else {
            System.out.println("Invalid input. Please provide both color and direction in correct format.");
        }
    }
}
