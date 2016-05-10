package tk.imihajlov.camelup.engine.actions;

import tk.imihajlov.camelup.engine.LegWinnerCard;
import tk.imihajlov.camelup.engine.PlayerAction;

public class BetLegWinner extends PlayerAction {
    LegWinnerCard card;

    public BetLegWinner(LegWinnerCard card, double[] probabilities) {
        super(card.getExpectedGain(probabilities));

        this.card = card;
    }

    public final LegWinnerCard getCard() {
        return card;
    }

    @Override
    public void accept(PlayerActionVisitor v) {
        v.visit(this);
    }
}
