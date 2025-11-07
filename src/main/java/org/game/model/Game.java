package org.game.model;

import org.game.model.AI.AIPlayer;
import org.game.model.AI.AIPlayerType;
import org.game.model.board.Board;
import org.game.utils.JsonReader;

import java.util.List;

public class Game {
    private Board board;
    private StackOfCards unplayedCards;
    private final JsonReader jsonReader = new JsonReader();
    private Card startingCard;
    private final int boardMaxSize = 23;
    private List<Player> players;

    public Game(int numberOfPlayers, List<AIPlayerType> aiPlayerTypes) {
        initializeCards();
        initializeBoard();
        initializePlayers(numberOfPlayers, aiPlayerTypes);
        printPlayers();
    }

    private void initializeCards(){
        List<Card> allCards = jsonReader.loadCardsFromJson();
        // find the starting card with id startingId
        Card startingCard = null;
        for (Card card : allCards) {
            int startingCardId = 1;
            if (card.getId() == startingCardId) {
                startingCard = card;
                break;
            }
        }
        this.startingCard = startingCard;

        // remove starting card from allCards
        allCards.remove(startingCard);

        // shuffle allCards
        java.util.Collections.shuffle(allCards);
        this.unplayedCards = new StackOfCards(allCards);
    }

    private void initializePlayers(int numberOfPlayers, List<AIPlayerType> aiPlayerTypes){
         this.
        players = jsonReader.loadPlayersFromJson(numberOfPlayers, aiPlayerTypes, this.board);
    }

    private void printPlayers(){
        for(int i = 0; i < players.size(); i++){
            System.out.println("Player " + (i+1) + ":");
            for(Action action : players.get(i).getActions()){
                System.out.println(" - " + action);
            }
        }
    }

    private void initializeBoard(){
        List<Pawn> initialPawns = initializePawns();

        this.board = new Board(boardMaxSize);
        board.initializeStartingTile(this.startingCard);
        board.initializeStartingPawns(initialPawns);
        // add every AI player to the board's pawn manager as pawn move listener
        for(Player player : players){
            if(player instanceof AIPlayer aiPlayer){
                board.getPawnManager().addPawnMoveListener(aiPlayer);
            }
        }
        board.testPathFinder();
    }

    public Board getBoard() {
        return board;
    }

    private List<Pawn> initializePawns(){
        int middle = (boardMaxSize / 2)+1;
        List<Pawn> pawns;
        // make a list of starting positions as List of (int, int)
        List<int[]> startingPositions = new java.util.ArrayList<>(List.of(
                new int[]{middle, middle}, // left top
                new int[]{middle, middle+1}, // right top
                new int[]{middle+1, middle}, // left bottom
                new int[]{middle+1, middle+1}  // right bottom
        ));

        // shuffle the list in a random way
        java.util.Collections.shuffle(startingPositions);
        pawns = List.of(
                new Pawn(new Coordinate(startingPositions.get(0)[0], startingPositions.get(0)[1]), Color.YELLOW),
                new Pawn(new Coordinate(startingPositions.get(1)[0], startingPositions.get(1)[1]), Color.GREEN),
                new Pawn(new Coordinate(startingPositions.get(2)[0], startingPositions.get(2)[1]), Color.PURPLE),
                new Pawn(new Coordinate(startingPositions.get(3)[0], startingPositions.get(3)[1]), Color.ORANGE)
        );
        return pawns;
    }

    public boolean discoverCard(Coordinate coordinate) {
        Card nextCard = unplayedCards.drawCard();
        System.out.println("Discovered card with ID: " + nextCard.getId());
        return board.addCardToBoard(nextCard, coordinate);
    }
}
