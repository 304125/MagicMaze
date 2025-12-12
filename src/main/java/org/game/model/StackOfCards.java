package org.game.model;

import org.game.utils.Config;

import java.util.List;

public class StackOfCards {
    List<Card> unplayedCards;

    public StackOfCards(List<Card> unplayedCards) {
        if(Config.PRINT_EVERYTHING) {
            System.out.println("StackOfCards initialized with " + unplayedCards.size() + " cards.");
        }
        this.unplayedCards = unplayedCards;
    }

    public boolean isEmpty() {
        return unplayedCards.isEmpty();
    }

    public Card drawCard() {
        if(Config.PRINT_EVERYTHING) {
            System.out.println("Drawing a card. Cards left before draw: " + unplayedCards.size());
        }
        if (isEmpty()) {
            throw new IllegalStateException("No more cards to draw");
        }
        return unplayedCards.removeFirst();
    }

    public boolean areAllCardsDrawn(){
        return unplayedCards.isEmpty();
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
        if(Config.PRINT_EVERYTHING) {
            System.out.println("Returned card with ID " + card.getId() + " to unplayed cards. Total cards now: " + unplayedCards.size());
        }
    }
}
