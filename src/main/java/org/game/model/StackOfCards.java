package org.game.model;

import java.util.List;

public class StackOfCards {
    List<Card> unplayedCards;

    public StackOfCards(List<Card> unplayedCards) {
        System.out.println("StackOfCards initialized with " + unplayedCards.size() + " cards.");
        this.unplayedCards = unplayedCards;
    }

    public boolean isEmpty() {
        return unplayedCards.isEmpty();
    }

    public Card drawCard() {
        System.out.println("Drawing a card. Cards left before draw: " + unplayedCards.size());
        if (isEmpty()) {
            throw new IllegalStateException("No more cards to draw");
        }
        return unplayedCards.removeFirst();
    }

    public Card drawGivenCard(int cardId){
        for (int i = 0; i < unplayedCards.size(); i++) {
            if (unplayedCards.get(i).getId() == cardId) {
                return unplayedCards.remove(i);
            }
        }
        return null;
    }

    public void returnCard(Card card){
        unplayedCards.add(card);
        System.out.println("Returned card with ID " + card.getId() + " to unplayed cards. Total cards now: " + unplayedCards.size());
    }
}
