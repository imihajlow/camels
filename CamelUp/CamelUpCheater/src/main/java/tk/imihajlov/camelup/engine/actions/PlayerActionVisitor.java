package tk.imihajlov.camelup.engine.actions;

public interface PlayerActionVisitor {
    void visit(BetLegWinner action);
    void visit(Dice action);
    void visit(PutDesert action);
}
