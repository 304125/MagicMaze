package org.game.utils;

import org.game.model.Action;
import org.game.model.ActionType;
import org.game.model.Color;

import java.util.List;

import static org.game.model.ActionType.*;

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
        ActionType actionType = switch (actionString) {
            case "n" -> MOVE_NORTH;
            case "s" -> MOVE_SOUTH;
            case "w" -> MOVE_WEST;
            case "e" -> MOVE_EAST;
            case "d" -> DISCOVER;
            case "v" -> VORTEX;
            case "x" -> ESCALATOR;
            default -> null;
        };

        //
        if ((optionalNumber != 0 &&  actionType != VORTEX && actionType != DISCOVER) || (optionalNumber == 0 && actionType == VORTEX)) {
            System.out.println("Invalid input. Vortex actionType requires a number (e.g., 'yv1' for yellow vortex 1).");
            return;
        }

        if (actionType == DISCOVER && pawnColor != null) {
            if(optionalNumber != 0){
                actionDelegator.discoverGivenCard(pawnColor, optionalNumber);
            }
            else{
                actionDelegator.discoverRandomCard(pawnColor);
            }
        }
        else

        if (actionType != null && pawnColor != null && optionalNumber == 0) {
            Action action = new Action(actionType);
            actionDelegator.movePawn(pawnColor, action);
        }
        // optionalNumber != 0
        else if (actionType == VORTEX && pawnColor != null){
            // use manhattan (1) as default heuristic
            actionDelegator.vortexPawn(pawnColor, optionalNumber, 1);
        }
        else {
            System.out.println("Invalid input. Please provide both color and direction in correct format.");
        }
    }

    public List<ActionType> renderActions(String actionsList){
        // render the String including the List<Action> casted to String back to List<Action>
        actionsList = actionsList.replaceAll("[\\[\\]\\s]", ""); // remove brackets and spaces
        String[] actionStrings = actionsList.split(",");
        List<ActionType> actions = new java.util.ArrayList<>();
        for (String actionString : actionStrings) {
            ActionType action = switch (actionString) {
                case "MOVE_NORTH" -> MOVE_NORTH;
                case "MOVE_SOUTH" -> MOVE_SOUTH;
                case "MOVE_WEST" -> MOVE_WEST;
                case "MOVE_EAST" -> MOVE_EAST;
                case "DISCOVER" -> DISCOVER;
                case "VORTEX" -> VORTEX;
                case "ESCALATOR" -> ESCALATOR;
                default -> null;
            };
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }
}
