package org.game.model;

import org.game.model.AI.AIPlayer;
import org.game.model.AI.AIPlayerType;
import org.game.model.AI.StateChangeListener;
import org.game.model.board.Board;
import org.game.model.board.GeneralGoalManager;
import org.game.utils.ActionDelegator;
import org.game.utils.Config;
import org.game.utils.JsonReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Game {
    private Board board;
    private StackOfCards unplayedCards;
    private final JsonReader jsonReader = new JsonReader();
    private Card startingCard;
    private final int boardMaxSize = 30;
    private List<Player> players;
    private final List<StateChangeListener> listeners = new ArrayList<>();

    public Game(int numberOfPlayers, List<AIPlayerType> aiPlayerTypes) {
        initializeCards();
        initializeBoard();
        initializePlayers(numberOfPlayers, aiPlayerTypes);
        if(Config.PRINT_EVERYTHING){
            printPlayers();
        }
    }

    public Game(Map<Color, Coordinate> initialPawnPositions){
        initializeCards();
        initializeBoardWithGivenPawns(initialPawnPositions);
        // no players in this constructor
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
        Collections.shuffle(allCards);
        this.unplayedCards = new StackOfCards(allCards);
    }

    private void initializePlayers(int numberOfPlayers, List<AIPlayerType> aiPlayerTypes){
         this.
        players = jsonReader.loadPlayersFromJson(numberOfPlayers, aiPlayerTypes, this.board);

        // add every AI player to the board's pawn manager as pawn move listener
        for(Player player : players){
            if(player instanceof AIPlayer aiPlayer){
                board.getPawnManager().addStateChangeListener(aiPlayer);
                addStateChangeListener(aiPlayer);
            }
        }
    }

    private void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
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
        // board.testPathFinder();
    }

    private void initializeBoardWithGivenPawns(Map<Color, Coordinate> initialPawnPositions){
        List<Pawn> initialPawns = new ArrayList<>();
        for (Map.Entry<Color, Coordinate> entry : initialPawnPositions.entrySet()) {
            initialPawns.add(new Pawn(entry.getValue(), entry.getKey()));
        }

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
        List<int[]> startingPositions = new ArrayList<>(List.of(
                new int[]{middle, middle}, // left top
                new int[]{middle, middle+1}, // right top
                new int[]{middle+1, middle}, // left bottom
                new int[]{middle+1, middle+1}  // right bottom
        ));

        // shuffle the list in a random way
        Collections.shuffle(startingPositions);
        pawns = List.of(
                new Pawn(new Coordinate(startingPositions.get(0)[0], startingPositions.get(0)[1]), Color.YELLOW),
                new Pawn(new Coordinate(startingPositions.get(1)[0], startingPositions.get(1)[1]), Color.GREEN),
                new Pawn(new Coordinate(startingPositions.get(2)[0], startingPositions.get(2)[1]), Color.PURPLE),
                new Pawn(new Coordinate(startingPositions.get(3)[0], startingPositions.get(3)[1]), Color.ORANGE)
        );
        return pawns;
    }

    public int discoverRandomCard(Pawn pawn) {
        Card nextCard = unplayedCards.drawCard();
        return discover(nextCard, pawn);
    }

    public int discoverGivenCard(Pawn pawn, int cardId) {
        Card nextCard = unplayedCards.drawGivenCard(cardId);
        if(nextCard == null){
            if(Config.PRINT_EVERYTHING){
                System.out.println("Card with ID " + cardId + " not found in unplayed cards.");
            }
            return 0;
        }
        return discover(nextCard, pawn);
    }

    private int discover(Card nextCard, Pawn pawn){
        if(Config.PRINT_EVERYTHING){
            System.out.println("Discovered card with ID: " + nextCard.getId());
        }
        boolean success = board.addCardToBoard(nextCard, pawn.getCoordinate());
        if(success){
            updateStateOfGame(pawn);
            return nextCard.getId();
        }
        return 0;
    }

    private void updateStateOfGame(Pawn pawn){
        // Notify all listeners about the pawn move
        for (StateChangeListener listener : listeners) {
            listener.onDiscovered(pawn);
        }
    }

    public void giveActionDelegatorToAIPlayers(ActionDelegator actionDelegator){
        for(Player player : players){
            if(player instanceof AIPlayer aiPlayer){
                aiPlayer.setActionDelegator(actionDelegator);
            }
        }
    }

    public void startGame(){
        for(Player player : players){
            if(player instanceof AIPlayer aiPlayer){
                aiPlayer.startGame();
            }
        }
    }

    public void setTimerFinishCallback(Runnable callback) {
        board.setTimerFinishCallback(callback);
    }

    public void endGame(){
        for(Player player : players){
            if(player instanceof AIPlayer aiPlayer){
                aiPlayer.endGame();
            }
        }
        GeneralGoalManager.getInstance().reset();
    }

    public void setGameWonCallback(Runnable callback){
        board.setGameWonCallback(callback);
    }

    public void placeDoSomething(Action action){
        System.out.println("Placing do something on "+action.toString());
        Player chosenPlayer = null;
        for(Player player : players){
            if(player.canPerformAction(action)){
                chosenPlayer = player;
            }
        }
        if(chosenPlayer == null){
            // should not get here, somone has to have that action
            return;
        }
        else{
            chosenPlayer.doSomething();
        }
        for(Player player : players){
            player.doSomethingPlaced(chosenPlayer);
        }
    }
}
