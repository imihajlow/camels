package tk.imihajlov.camelup.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tk.imihajlov.camelup.engine.actions.BetLegWinner;
import tk.imihajlov.camelup.engine.actions.Dice;
import tk.imihajlov.camelup.engine.actions.PutDesert;

public class LegResult implements Serializable {
    private final LegWinnerCard[] topLegWinnerCards;
    private double[][] result;
    private double[] wins;
    private double[] looses;
    private double[] absoluteWins;
    private double[] absoluteLooses;

    private class Desert implements Serializable {
        int x;
        boolean isOasis;
        int nHits;
        int nResults;
        double gain;
        Desert(int x, boolean isOasis) {
            this.x = x;
            this.isOasis = isOasis;
        }

        public void finish() {
            if (nResults != 0) {
                gain = (double) nHits / (double) nResults;
            }
        }

        public void hit() {
            nHits += 1;
        }

        public void legEnded() {
            nResults += 1;
        }
    }
    private Desert myDesert;
    private List<Desert> desertGains;

    private int nResults;
    private int nFinals;

    public double[][] getProbabilityMatrix() {
        return result;
    }

    public double[] getWinProbability() {
        return wins;
    }

    public double[] getLooseProbability() {
        return looses;
    }

    public double[] getAbsoluteWinProbability() {
        return absoluteWins;
    }

    public double[] getAbsoluteLooseProbability() {
        return absoluteLooses;
    }

    /**
     * Get an array of actions sorted by expected gain.
     * @return array of player actions.
     */
    public PlayerAction[] getSuggestedActions() {
        List<PlayerAction> actions = new ArrayList<PlayerAction>();
        actions.add(new Dice());
        for (int i = 0; i < result.length; ++i) {
            if (topLegWinnerCards[i] != null) {
                actions.add(new BetLegWinner(topLegWinnerCards[i], result[i]));
            }
        }
        for (Desert d : desertGains) {
            actions.add(new PutDesert(d.gain, d.x, d.isOasis));
        }
        Collections.sort(actions);
        Collections.reverse(actions);
        return actions.toArray(new PlayerAction[0]);
    }

    LegResult(int nCamels, LegWinnerCard[] topLegWinnerCards) {
        this.topLegWinnerCards = topLegWinnerCards;
        reset(nCamels);
    }

    private void reset(int nCamels) {
        result = new double[nCamels][nCamels];
        wins = new double[nCamels];
        looses = new double[nCamels];
        absoluteWins = new double[nCamels];
        absoluteLooses = new double[nCamels];
        nResults = 0;
        nFinals = 0;
        myDesert = null;
        desertGains = new ArrayList<Desert>();
    }

    void addPositions(int[] positions) {
        for (int i = 0; i < positions.length; ++i) {
            result[positions[i]][i] += 1;
        }
        nResults += 1;
        if (myDesert != null) {
            myDesert.legEnded();
        }
    }

    void addFinal(int[] positions) {
        addPositions(positions);
        nFinals += 1;
        wins[positions[0]] += 1;
        looses[positions[positions.length - 1]] += 1;
    }

    void startCountingDesert(int x, boolean isOasis) {
        myDesert = new Desert(x, isOasis);
    }

    void countDesert(int x) {
        if (myDesert != null && x == myDesert.x) {
            myDesert.hit();
        }
    }

    void finishCountingDesert() {
        if (myDesert != null) {
            myDesert.finish();
            desertGains.add(myDesert);
            myDesert = null;
        }
    }

    void finish() {
        if (nResults > 0) {
            for (double[] row : result) {
                for (int i = 0; i < row.length; ++i) {
                    row[i] /= nResults;
                }
            }
        }
        assert wins.length == looses.length;
        if (nFinals > 0) {
            for (int i = 0; i < wins.length; ++i) {
                absoluteWins[i] = wins[i] / nResults;
                absoluteLooses[i] = looses[i] / nResults;
                wins[i] /= nFinals;
                looses[i] /= nFinals;
            }
        }
        nResults = 0;
        nFinals = 0;
    }
}
