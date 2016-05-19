package tk.imihajlov.camelup.engine.actions;

public interface IPlayerActionVisitor {
    void visit(BetLegWinner action);
    void visit(Dice action);
    void visit(PutDesert action);
}
