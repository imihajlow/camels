package tk.imihajlov.camelup.engine.actions;

import tk.imihajlov.camelup.engine.PlayerAction;

public class Dice extends PlayerAction {
    public Dice() {
        super(1);
    }

    @Override
    public void accept(IPlayerActionVisitor v) {
        v.visit(this);
    }
}
