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
        if (!stateModified) {
            listener.onCompleted(result);
            return true;
        }
        result = new Result(settings, state, listener);
        (new Thread(result)).start();
        return true;
    }

    private Settings settings;
    private State state;
    private boolean stateModified = true;
    private Result result;

    private class Result implements LegResult, Runnable {

        public Result(Settings settings, State state, ResultListener listener) {
            this.settings = settings;
            this.state = state;
            this.listener = listener;
        }

        @Override
        public double[][] getProbabilityMatrix() {
            return result;
        }

        @Override
        public void run() {
            result = new double[settings.getNCamels()][settings.getNCamels()];
            int n = positions(state);
            for (double[] row : result) {
                for (int i = 0; i < row.length; ++i) {
                    row[i] /= n;
                }
            }
            listener.onCompleted(this);
        }

        private int positions(State state) {
            if (state == null) {
                return 0;
            }
            if (!state.areDiceLeft()) {
                int[] camels = state.listCamelsByPosition();
                for (int i = 0; i < camels.length; ++i) {
                    result[camels[i]][i] += 1;
                }
                return 1;
            } else {
                boolean[] dice = state.getDice();
                int counter = 0;
                for (int i = 0; i < dice.length; ++i) {
                    if (dice[i]) {
                        for (int v = 1; v < settings.maxDieValue; ++v) {
                            // Just jump
                            counter += positions(state.jump(i, v));
                            // Or if +1 or -1 can be put, put it and jump
                            int xTo = state.getCamelPosition(i).getX() + v;
                            State newState = state.putMirage(xTo);
                            if (newState != null) {
                                counter += positions(newState.jump(i, v));
                                newState = state.putOasis(xTo);
                                counter += positions(newState.jump(i, v));
                            }
                        }
                    }
                }
                return counter;
            }
        }

        private Settings settings;
        private State state;
        private double[][] result;
        private ResultListener listener;
    }
}
