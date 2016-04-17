package tk.imihajlov.camelup.engine;

public class Engine {
    public interface ResultListener {
        void onCompleted(LegResult result);

        void onInterrupted();
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        this.state = null;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setState(State state) {
        this.state = state;
        stateModified = true;
    }

    public State getState() {
        return state;
    }

    public boolean calculate(ResultListener listener) {
        if (settings == null || state == null || listener == null) {
            return false;
        }
        /*if (!stateModified) {
            listener.onCompleted(result);
            return true;
        }*/
        result = new Result(settings, state, listener);
        (new Thread(result)).start();
        return true;
    }

    private Settings settings;
    private State state;
    private boolean stateModified = true;
    private Result result;

    private class Result implements Runnable {

        public Result(Settings settings, State state, ResultListener listener) {
            this.settings = settings;
            this.state = state;
            this.listener = listener;
        }

        private class ResultAccumulator implements LegResult {
            private double[][] result;
            private double[] wins;
            private double[] looses;
            private double[] absoluteWins;
            private double[] absoulteLooses;

            private int nResults;
            private int nFinals;

            @Override
            public double[][] getProbabilityMatrix() {
                return result;
            }

            @Override
            public double[] getWinProbability() {
                return wins;
            }

            @Override
            public double[] getLooseProbability() {
                return looses;
            }

            @Override
            public double[] getAbsoluteWinProbability() {
                return absoluteWins;
            }

            @Override
            public double[] getAbsoluteLooseProbability() {
                return absoulteLooses;
            }

            public ResultAccumulator(int nCamels) {
                reset(nCamels);
            }

            public void reset(int nCamels) {
                result = new double[nCamels][nCamels];
                wins = new double[nCamels];
                looses = new double[nCamels];
                absoluteWins = new double[nCamels];
                absoulteLooses = new double[nCamels];
                nResults = 0;
                nFinals = 0;
            }

            public void addPositions(int[] positions) {
                if (nResults == 0) {
                    reset(result.length);
                }
                for (int i = 0; i < positions.length; ++i) {
                    result[positions[i]][i] += 1;
                }
                nResults += 1;
            }

            public void addFinal(int[] positions) {
                addPositions(positions);
                nFinals += 1;
                wins[positions[0]] += 1;
                looses[positions[positions.length - 1]] += 1;
            }

            public void finalize() {
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
                        absoulteLooses[i] = looses[i] / nResults;
                        wins[i] /= nFinals;
                        looses[i] /= nFinals;
                    }
                }
                nResults = 0;
                nFinals = 0;
            }
        }

        @Override
        public void run() {
            result = new ResultAccumulator(settings.getNCamels());
            positions(state);
            result.finalize();
            listener.onCompleted(result);
        }

        private void positions(State state) {
            if (state == null) {
                return;
            }
            if (state.isGameEnd()) {
                result.addFinal(state.listCamelsByPosition());
            } else if (!state.areDiceLeft()) {
                result.addPositions(state.listCamelsByPosition());
            } else {
                boolean[] dice = state.getDice();
                for (int i = 0; i < dice.length; ++i) {
                    if (dice[i]) {
                        for (int v = 1; v <= settings.maxDieValue; ++v) {
                            // Just jump
                            positions(state.jump(i, v));
                            // Or if +1 or -1 can be put, put it and jump
                            int xTo = state.getCamelPosition(i).getX() + v;
                            State newState = state.putMirage(xTo);
                            if (newState != null) {
                                positions(newState.jump(i, v));
                                newState = state.putOasis(xTo);
                                positions(newState.jump(i, v));
                            }
                        }
                    }
                }
            }
        }

        private Settings settings;
        private State state;
        private ResultListener listener;
        private ResultAccumulator result;
    }
}
