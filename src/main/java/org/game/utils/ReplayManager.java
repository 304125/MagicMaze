package org.game.utils;

import org.game.model.ActionType;
import org.game.model.Color;
import org.game.model.Coordinate;
import org.game.utils.output.GameRecord;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ReplayManager {
    private final GameRecord gameRecord;
    private InputStringRenderer inputStringRenderer;
    private Runnable onGameFinishedCallback;
    private volatile boolean running;
    private ActionDelegator actionDelegator;

    public ReplayManager(String folderName, String fileName){
        JsonReader jsonReader = new JsonReader();
        this.gameRecord = jsonReader.loadGameFromJson(folderName, fileName);
    }

    public Map<Color, Coordinate> getInitialPawnPositions(){
        return gameRecord.getInitialPawnPositions();
    }

    public void setActionDelegator(ActionDelegator actionDelegator){
        this.actionDelegator = actionDelegator;
        inputStringRenderer = new InputStringRenderer(actionDelegator);
    }

    public void startReplay(){
        running = true;
        // Time to render a "do something" action
        // Time to render a normal game move
        Thread replayThread = new Thread(() -> {
            Map.Entry<Instant, String> first = gameRecord.next();
            Map.Entry<Instant, String> next = gameRecord.next();

            Map.Entry<Instant, String> doSomethingFirst = gameRecord.nextDoSomething();
            Map.Entry<Instant, String> doSomethingNext = gameRecord.nextDoSomething();

            while (running && (next != null || doSomethingNext != null)) {
                if (doSomethingNext != null && (next == null || doSomethingFirst.getKey().isBefore(first.getKey()))) {
                    // Time to render a "do something" action
                    List<ActionType> actionList = inputStringRenderer.renderActions(doSomethingFirst.getValue());
                    actionDelegator.placeDoSomethingUI(actionList);
                    long doSomethingDelay = doSomethingNext.getKey().toEpochMilli() - doSomethingFirst.getKey().toEpochMilli();
                    assert next != null;
                    long actionDelay = first.getKey().toEpochMilli() - doSomethingFirst.getKey().toEpochMilli();
                    long delay = Math.min(doSomethingDelay, actionDelay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        System.out.println("Replay thread interrupted during do something delay. Stopping replay...");
                    }
                    doSomethingFirst = doSomethingNext;
                    doSomethingNext = gameRecord.nextDoSomething();
                } else {
                    // Time to render a normal game move
                    inputStringRenderer.renderInputString(first.getValue());
                    long actionDelay = next.getKey().toEpochMilli() - first.getKey().toEpochMilli();
                    assert doSomethingNext != null;
                    long doSomethingDelay = doSomethingFirst.getKey().toEpochMilli() - first.getKey().toEpochMilli();
                    long delay = Math.min(actionDelay, doSomethingDelay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        System.out.println("Replay thread interrupted during action delay. Stopping replay...");
                    }
                    first = next;
                    next = gameRecord.next();
                }

            }
            inputStringRenderer.renderInputString(first.getValue());
            if (onGameFinishedCallback != null) {
                onGameFinishedCallback.run();
            }

        });
        replayThread.start();
    }

    public void setOnGameFinishedCallback(Runnable callback) {
        this.onGameFinishedCallback = callback;
    }
}
