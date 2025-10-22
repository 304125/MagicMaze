package org.game.model;

public class Timer {
    private final int maxTime = 180;
    private int totalTime;
    private int timeLeftInTimer;

    public Timer(){
        totalTime = 0;
        timeLeftInTimer = maxTime;
        startTimer();
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
            System.exit(0);
        }
    }

    public void flipTimer(){
        timeLeftInTimer = maxTime - timeLeftInTimer;
    }

    // execute secondPassed every 1 second
    public void startTimer(){
        Thread timerThread = new Thread(() -> {
            try {
                while (timeLeftInTimer >= 0) {
                    Thread.sleep(1000);
                    secondPassed();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        timerThread.start();
    }

    public int getTimeLeftInTimer() {
        return timeLeftInTimer;
    }

    public int getTotalTime() {
        return totalTime;
    }
}
