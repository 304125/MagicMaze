package org.game.model;

public enum Color {
    PURPLE("#8B549A"),
    YELLOW("#FFD403"),
    GREEN("#6BB52C"),
    ORANGE("#EE7E16"),
    NONE("#E0EEF6"),
    BROWN("#7B6555"),
    RED("#E30815"),
    DARK_RED("#510307");

    private final String hexCode;

    // Constructor
    Color(String hexCode) {
        this.hexCode = hexCode;
    }

    // Getter
    public String getHexCode() {
        return hexCode;
    }

    public String toString() {
        return this.name().toLowerCase();
    }
}
