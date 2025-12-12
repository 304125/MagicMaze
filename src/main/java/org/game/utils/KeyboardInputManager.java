package org.game.utils;

import java.util.Scanner;

public class KeyboardInputManager {
    ActionDelegator actionDelegator;
    InputStringRenderer inputStringRenderer;

    public KeyboardInputManager(ActionDelegator actionDelegator) {
        this.actionDelegator = actionDelegator;
        this.inputStringRenderer = new InputStringRenderer(actionDelegator);
        readKeyboardInput();
    }

    public void readKeyboardInput(){
        Thread executionThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    if(Config.PRINT_EVERYTHING) {
                        System.out.print("Enter color of pawn (y: yellow, o: orange, p: purple, g: green) and direction (n: north, s: south, w: west, e: east) or action (d: discover, v: vortex, x: escalator):");
                    }
                    String input = scanner.nextLine().trim().toLowerCase();

                    inputStringRenderer.renderInputString(input);
                }
            } catch (Exception e) {
                System.out.println("Error reading keyboard input: " + e.getMessage());
            }
        });

        executionThread.setDaemon(true); // Optional: Set as daemon thread
        executionThread.start();
    }


}
