package tk.imihajlov.camelup.engine;


import com.google.common.collect.ImmutableList;

public class Player {
    public static final int MYSELF = 0;

    private int id;
    private ImmutableList<LegWinnerCard> legWinnerCards;

    public Player(int id) {
        this(id, ImmutableList.<LegWinnerCard>of());
    }

    private Player(int id, ImmutableList<LegWinnerCard> legWinnerCards) {
        this.id = id;
        this.legWinnerCards = legWinnerCards;
    }

    public int getId() {
        return id;
    }

    public ImmutableList<LegWinnerCard> getLegWinnerCards() {
        return legWinnerCards;
    }

    public Player takeLegWinnerCard(LegWinnerCard card) {
        ImmutableList<LegWinnerCard> newCards = ImmutableList.<LegWinnerCard>builder()
                .addAll(legWinnerCards)
                .add(card)
                .build();
        return new Player(this.id, newCards);
    }

    public double getGain(LegResult legResult) {
        double gain = 0;
        double[][] matrix = legResult.getProbabilityMatrix();
        for (LegWinnerCard card : legWinnerCards) {
            gain += card.getExpectedGain(matrix);
        }
        return gain;
    }
}
