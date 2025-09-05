package org.game;

import java.util.List;

public class Game {
    private Board board;
    private StackOfCards unplayedCards;
    private final JsonReader jsonReader = new JsonReader();
    private int startingCardId = 3;
    private Card startingCard;
    private final int boardMaxSize = 32;

    public Game() {
        initializeCards();
        initializeBoard();
    }

    private void initializeCards(){
        List<Card> allCards = jsonReader.loadCardsFromJson();
        // find the starting card with id startingId
        Card startingCard = null;
        for (Card card : allCards) {
            if (card.getId() == startingCardId) {
                startingCard = card;
                break;
            }
        }
        this.startingCard = startingCard;

        // remove starting card from allCards
        allCards.remove(startingCard);
        this.unplayedCards = new StackOfCards(allCards);
    }

    private void initializeBoard(){
        List<Pawn> initialPawns = initializePawns();

        this.board = new Board(boardMaxSize);
        board.initializeStartingTile(this.startingCard);
        board.initializeStartingPawns(initialPawns);
    }

    public Board getBoard() {
        return board;
    }

    private List<Pawn> initializePawns(){
        int middle = (boardMaxSize / 2)+1;
        List<Pawn> pawns;
        // make a list of starting positions as List of (int, int)
        List<int[]> startingPositions = new java.util.ArrayList<>(List.of(
                new int[]{middle, middle}, // center
                new int[]{middle, middle+1}, // left
                new int[]{middle+1, middle}, // right
                new int[]{middle+1, middle+1}  // up
        ));

        // shuffle the list in a random way
        java.util.Collections.shuffle(startingPositions);
        pawns = List.of(
                new Pawn(startingPositions.get(0)[0], startingPositions.get(0)[1], Color.YELLOW),
                new Pawn(startingPositions.get(1)[0], startingPositions.get(1)[1], Color.GREEN),
                new Pawn(startingPositions.get(2)[0], startingPositions.get(2)[1], Color.PURPLE),
                new Pawn(startingPositions.get(3)[0], startingPositions.get(3)[1], Color.ORANGE)
        );
        return pawns;
    }

    public int getBoardMaxSize() {
        return boardMaxSize;
    }
}
