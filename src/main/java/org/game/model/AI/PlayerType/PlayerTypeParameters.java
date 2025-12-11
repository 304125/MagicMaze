package org.game.model.AI.PlayerType;

public class PlayerTypeParameters {
    // how many chunks of information the player has in the beginning
    private final int startingCapacity;
    // determines the delay (processingRatio 2 => half delay) in processing information
    private final float processingRatio;
    // blindness: 0 always notices moving pawn, 1 sometimes misses, 2 misses often
    private final int blindness;
    // how many ticks to wait before placing do something token
    private final int patience;
    // heuristic (0: euclidian, 1: manhattan)
    private final int heuristicType;
    // timer log base for payoff calculation
    private final float timerLogBase;
    // stressing factors
    private final boolean stressedByDoSomething;
    private final boolean stressByLowTime;
    private final boolean stressedByPlacingDoSomething;
    // if stubborn, they don't panic but place do something further if no actions found
    private final boolean stubborn;
    // if false, they flip a die whether to place do something or not
    private final boolean alwaysPlaceDoSomething;

    public PlayerTypeParameters(int startingCapacity, float processingRatio, int blindness, int patience,
                                int heuristicType, float timerLogBase, boolean stressedByDoSomething,
                                boolean stressByLowTime, boolean stressedByPlacingDoSomething,
                                boolean stubborn, boolean alwaysPlaceDoSomething) {
        this.startingCapacity = startingCapacity;
        this.processingRatio = processingRatio;
        this.blindness = blindness;
        this.patience = patience;
        this.heuristicType = heuristicType;
        this.timerLogBase = timerLogBase;
        this.stressedByDoSomething = stressedByDoSomething;
        this.stressByLowTime = stressByLowTime;
        this.stressedByPlacingDoSomething = stressedByPlacingDoSomething;
        this.stubborn = stubborn;
        this.alwaysPlaceDoSomething = alwaysPlaceDoSomething;
    }

    public int getStartingCapacity() {
        return startingCapacity;
    }

    public float getProcessingRatio() {
        return processingRatio;
    }

    public int getBlindness() {
        return blindness;
    }

    public int getPatience() {
        return patience;
    }

    public int getHeuristicType() {
        return heuristicType;
    }

    public float getTimerLogBase() {
        return timerLogBase;
    }

    public boolean isStressedByDoSomething() {
        return stressedByDoSomething;
    }

    public boolean isStressByLowTime() {
        return stressByLowTime;
    }

    public boolean isStressedByPlacingDoSomething() {
        return stressedByPlacingDoSomething;
    }

    public boolean isStubborn() {
        return stubborn;
    }

    public boolean isAlwaysPlaceDoSomething() {
        return alwaysPlaceDoSomething;
    }
}
