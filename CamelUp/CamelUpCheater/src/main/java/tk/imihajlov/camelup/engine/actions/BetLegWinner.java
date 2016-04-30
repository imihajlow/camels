package tk.imihajlov.camelup.engine.actions;

import tk.imihajlov.camelup.engine.PlayerAction;

public class BetLegWinner extends PlayerAction {
    private static final int secondGain = 1;
    private static final int looserGain = -1;

    private final int camel;
    private final int gain;

    public BetLegWinner(int camel, int winnerGain, double[] probabilities) {
        super(calculateExpectation(winnerGain, probabilities));

        this.camel = camel;
        this.gain = winnerGain;
    }

    public final int getCamel() {
        return camel;
    }

    public final int getWinnerGain() {
        return gain;
    }

    private static double calculateExpectation(int gain, double[] probabilities) {
        // Actually there are 5 camels in the game, but let this be general
        // and expect there be at least a winner, a second place and a looser.
        if (probabilities.length <= 2) {
            throw new IllegalArgumentException("There are not enough camels");
        }
        double expectation = gain * probabilities[0] + secondGain * probabilities[1];
        for (int i = 2; i < probabilities.length; ++i) {
            expectation += looserGain * probabilities[i];
        }
        return expectation;
    }
}
