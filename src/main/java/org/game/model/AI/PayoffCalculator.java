package org.game.model.AI;

import org.game.model.Timer;
import org.game.utils.Config;

public class PayoffCalculator {
    private final int maxTime;
    private final Timer timer;

    public PayoffCalculator(Timer timer){
        maxTime = timer.getMaxTime();
        this.timer = timer;
    }

    public int calculateLogarithmicTimePayoff(){
        int timeLeftInTimer = timer.getTimeLeftInTimer();
        // Payoff is 0 if time remaining is over half full - not worth it
        if (timeLeftInTimer > maxTime / 2.0) {
            return 0;
        }

        double threshold = maxTime / 2.0;
        int payoff = 0;

        // Loop checks if timeRemaining is less than the current interval threshold
        // Intervals are logarithmic - every next interval is half the size of the previous one
        while (threshold >= 1.0) {
            if (timeLeftInTimer <= threshold) {
                payoff++;
                // Halve the threshold to check the next smaller interval.
                threshold /= 2.0;
            } else {
                // Time remaining is larger than the current threshold, so we've found the interval.
                break;
            }
        }

        if(Config.PRINT_EVERYTHING){
            System.out.println("Current time payoff: "+payoff);
        }

        return payoff;
    }
}
