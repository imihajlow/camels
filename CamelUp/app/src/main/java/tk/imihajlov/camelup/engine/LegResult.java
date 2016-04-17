package tk.imihajlov.camelup.engine;

public interface LegResult {
    double[][] getProbabilityMatrix();

    double[] getWinProbability();
    double[] getLooseProbability();

    double[] getAbsoluteWinProbability();
    double[] getAbsoluteLooseProbability();
}
