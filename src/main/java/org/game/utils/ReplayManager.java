package org.game.utils;

import org.game.model.Color;
import org.game.model.Coordinate;
import org.game.utils.output.GameRecord;

import java.time.Instant;
import java.util.Map;

public class ReplayManager {
    private GameRecord gameRecord;
    private JsonReader jsonReader;
    private ActionDelegator actionDelegator;
    private InputStringRenderer inputStringRenderer;
    private Runnable onGameFinishedCallback;
    private Thread replayThread;
    private volatile boolean running;

    public ReplayManager(String folderName, String fileName){
        this.jsonReader = new JsonReader();
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
        replayThread = new Thread(() -> {
            Map.Entry<Instant, String> first = gameRecord.next();
            Map.Entry<Instant, String> next = gameRecord.next();
            while(running && next != null){
                inputStringRenderer.renderInputString(first.getValue());
                long delay = next.getKey().toEpochMilli() - first.getKey().toEpochMilli();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                first = next;
                next = gameRecord.next();
            }
            inputStringRenderer.renderInputString(first.getValue());
            if(onGameFinishedCallback != null){
                onGameFinishedCallback.run();
            }

        });
        replayThread.start();
    }

    public void setOnGameFinishedCallback(Runnable callback) {
        this.onGameFinishedCallback = callback;
    }

    private void stopReplay(){
        running = false;
        if(replayThread != null && replayThread.isAlive()){
            replayThread.interrupt();
        }
    }
}
