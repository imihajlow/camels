package tk.imihajlov.camelup.engine.actions;

import tk.imihajlov.camelup.engine.PlayerAction;

public class PutDesert extends PlayerAction {
    private int x;
    private boolean oasis;

    public PutDesert(double gain, int x, boolean isOasis) {
        super(gain);
        this.x = x;
        this.oasis = isOasis;
    }

    public int getX() {
        return x;
    }

    public boolean isOasis() {
        return oasis;
    }

    @Override
    public void accept(IPlayerActionVisitor visitor) {
        visitor.visit(this);
    }
}
