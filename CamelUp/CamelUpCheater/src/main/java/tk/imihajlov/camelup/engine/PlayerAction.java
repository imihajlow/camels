package tk.imihajlov.camelup.engine;


import tk.imihajlov.camelup.engine.actions.IPlayerActionVisitor;

public abstract class PlayerAction implements Comparable<PlayerAction> {
    final private double profitExpectation;

    protected PlayerAction(double profitExpectation) {
        this.profitExpectation = profitExpectation;
    }

    final public double getProfitExpectation() {
        return profitExpectation;
    }

    public int compareTo(PlayerAction other) {
        return (int) Math.signum(profitExpectation - other.profitExpectation);
    }

    public abstract void accept(IPlayerActionVisitor visitor);
}
