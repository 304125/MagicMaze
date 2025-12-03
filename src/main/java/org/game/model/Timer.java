package org.game.model;

public class Timer {
    private final int maxTime = 180;
    private int totalTime;
    private int timeLeftInTimer;
    private Runnable onTimerFinishCallback;
    private Thread timerThread;
    private volatile boolean running;

    public Timer(){
        totalTime = 0;
        timeLeftInTimer = maxTime;
        startTimer();
    }

    public void setOnTimerFinishCallback(Runnable callback) {
        this.onTimerFinishCallback = callback;
    }

    // method that every 1 second passed decreases timeLeftInTimer by 1
    public void secondPassed(){
        if(timeLeftInTimer > 0){
            timeLeftInTimer--;
            totalTime++;
        }
        else{
            System.out.println("Timer finished!");
            // exit the game
            if (onTimerFinishCallback != null) {
                onTimerFinishCallback.run();
            }
            stopTimer();
        }
    }

    public void flipTimer(){
        timeLeftInTimer = maxTime - timeLeftInTimer;
    }

    // execute secondPassed every 1 second
    public void startTimer(){
        running = true;
        timerThread = new Thread(() -> {
            try {
                while (running && timeLeftInTimer >= 0) {
                    Thread.sleep(1000);
                    secondPassed();
                }
            } catch (InterruptedException e) {
                System.out.println("Timer thread interrupted. Stopping timer...");
            }
        });
        timerThread.start();
    }

    public void stopTimer(){
        running = false;
        if(timerThread != null && timerThread.isAlive()){
            timerThread.interrupt();
        }
    }

    public int getTimeLeftInTimer() {
        return timeLeftInTimer;
    }

    public int getMaxTime(){
        return maxTime;
    }
}
