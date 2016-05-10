package tk.imihajlov.camelup.engine;

public class LegWinnerCard {
    private int camel;
    private int winnerGain;
    private int secondGain;
    private int looserGain;

    public LegWinnerCard(int camel, int winnerGain) {
        this.camel = camel;
        this.winnerGain = winnerGain;
        this.secondGain = 1;
        this.looserGain = -1;
    }

    public double getExpectedGain(double[] probabilities) {
        double result = winnerGain * probabilities[0] + secondGain * probabilities[1];
        for (int i = 2; i < probabilities.length; ++i) {
            result += looserGain * probabilities[i];
        }
        return result;
    }

    public int getCamel() {
        return camel;
    }

    @Override
    public boolean equals(Object o) {
        LegWinnerCard c = (LegWinnerCard) o;
        if (c != null) {
            return c.camel == camel && c.winnerGain == winnerGain && c.secondGain == secondGain
                    && c.looserGain == looserGain;
        } else {
            return false;
        }
    }
}
