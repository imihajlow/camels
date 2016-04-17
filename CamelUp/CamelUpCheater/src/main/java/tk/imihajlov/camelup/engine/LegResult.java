package tk.imihajlov.camelup.engine;

import java.io.Serializable;

public class LegResult implements Serializable {
    private double[][] result;
    private double[] wins;
    private double[] looses;
    private double[] absoluteWins;
    private double[] absoluteLooses;

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

    LegResult(int nCamels) {
        reset(nCamels);
    }

    void reset(int nCamels) {
        result = new double[nCamels][nCamels];
        wins = new double[nCamels];
        looses = new double[nCamels];
        absoluteWins = new double[nCamels];
        absoluteLooses = new double[nCamels];
        nResults = 0;
        nFinals = 0;
    }

    void addPositions(int[] positions) {
        if (nResults == 0) {
            reset(result.length);
        }
        for (int i = 0; i < positions.length; ++i) {
            result[positions[i]][i] += 1;
        }
        nResults += 1;
    }

    void addFinal(int[] positions) {
        addPositions(positions);
        nFinals += 1;
        wins[positions[0]] += 1;
        looses[positions[positions.length - 1]] += 1;
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
