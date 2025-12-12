package org.game.model.AI.PlayerType;

/**
 * @param startingCapacity       how many chunks of information the player has in the beginning
 * @param processingRatio        determines the delay (processingRatio 2 => half delay) in processing information
 * @param blindness              blindness: 0 always notices moving pawn, 1 sometimes misses, 2 misses often
 * @param patience               how many ticks to wait before placing do something token
 * @param heuristicType          heuristic (0: euclidian, 1: manhattan)
 * @param timerLogBase           timer log base for payoff calculation
 * @param stressedByDoSomething  stressing factors
 * @param stubborn               if stubborn, they don't panic but place do something further if no actions found
 * @param alwaysPlaceDoSomething if false, they flip a die whether to place do something or not
 */
public record PlayerTypeParameters(int startingCapacity, float processingRatio, int blindness, int patience,
                                   int heuristicType, float timerLogBase, boolean stressedByDoSomething,
                                   boolean stressByLowTime, boolean stressedByPlacingDoSomething, boolean stubborn,
                                   boolean alwaysPlaceDoSomething, boolean discoverUntilGoals) {
}
