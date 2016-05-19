package tk.imihajlov.camelup.engine.suggesters;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tk.imihajlov.camelup.engine.LegWinnerCard;
import tk.imihajlov.camelup.engine.PlayerAction;
import tk.imihajlov.camelup.engine.actions.BetLegWinner;

public class PositionsSuggester implements Serializable, ISuggester {
    private final LegWinnerCard[] topLegWinnerCards;
    private double[][] result;
    private double[] wins;
    private double[] looses;
    private double[] absoluteWins;
    private double[] absoluteLooses;
    private int nResults;
    private int nFinals;

    /**
     * Get matrix d[i][j] of probabilities for camel i to be on position j.
     */
    public double[][] getProbabilityMatrix() {
        return result;
    }

    /** Get vector of probabilities for each camel to win the game relative
     * to the total number of completed games.
     * The sum of the vector is either 1 or 0.
     */
    public double[] getWinProbability() {
        return wins;
    }

    /** Get vector of probabilities for each camel to loose the game relative
     * to the total number of completed games.
     * The sum of the vector is either 1 or 0.
     */
    public double[] getLooseProbability() {
        return looses;
    }

    /** Get vector of probabilities for each camel to win the game relative
     * to the total number of all games.
     */
    public double[] getAbsoluteWinProbability() {
        return absoluteWins;
    }

    /** Get vector of probabilities for each camel to loose the game relative
     * to the total number of all games.
     */
    public double[] getAbsoluteLooseProbability() {
        return absoluteLooses;
    }

    public PositionsSuggester(int nCamels, LegWinnerCard[] topLegWinnerCards) {
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
    }

    /** Add end-leg positions
     *
     * @param positions positions[i] = n means camel n is on the ith position.
     */
    public void addPositions(int[] positions) {
        for (int i = 0; i < positions.length; ++i) {
            result[positions[i]][i] += 1;
        }
        nResults += 1;
    }

    /** Add end-game positions
     *
     * @param positions positions[i] = n means camel n is on the ith position.
     */
    public void addFinal(int[] positions) {
        addPositions(positions);
        nFinals += 1;
        wins[positions[0]] += 1;
        looses[positions[positions.length - 1]] += 1;
    }

    /** Finish counting positions.
     */
    public void finish() {
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

    @Override
    public ImmutableList<PlayerAction> getSuggestedActions() {
        List<PlayerAction> actions = new ArrayList<PlayerAction>();
        for (int i = 0; i < result.length; ++i) {
            if (topLegWinnerCards[i] != null) {
                actions.add(new BetLegWinner(topLegWinnerCards[i], result[i]));
            }
        }
        return ImmutableList.copyOf(actions);
    }
}
