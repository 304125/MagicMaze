package org.game;

import java.util.List;

public class StackOfCards {
    List<Card> unplayedCards;

    public StackOfCards(List<Card> unplayedCards) {
        this.unplayedCards = unplayedCards;
    }

    public boolean isEmpty() {
        return unplayedCards.isEmpty();
    }

    public Card drawCard() {
        if (isEmpty()) {
            throw new IllegalStateException("No more cards to draw");
        }
        return unplayedCards.removeFirst();
    }
}
