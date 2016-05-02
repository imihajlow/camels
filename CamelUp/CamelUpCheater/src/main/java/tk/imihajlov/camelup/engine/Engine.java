package tk.imihajlov.camelup.engine;

import java.io.IOException;
import java.io.Serializable;

public class Engine implements Serializable {
    public interface ResultListener {
        void onCompleted();

        void onInterrupted();
    }

    public void setSettings(Settings settings) {
        if (!settings.equals(this.settings)) {
            this.settings = settings;
            this.state = null;
            this.resultCalculator = null;
            this.result = null;
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public LegResult getResult() {
        return result;
    }

    public boolean calculate(ResultListener listener) {
        if (settings == null || state == null || listener == null) {
            return false;
        }
        resultCalculator = new ResultCalculator(settings, state, listener);
        (new Thread(resultCalculator)).start();
        return true;
    }

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(settings);
        stream.writeObject(state);
        stream.writeObject(result);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        settings = (Settings) stream.readObject();
        state = (State) stream.readObject();
        result = (LegResult) stream.readObject();
        resultCalculator = null;
    }

    private Settings settings;
    private State state;
    private ResultCalculator resultCalculator;
    private LegResult result;

    private class ResultCalculator implements Runnable {

        public ResultCalculator(Settings settings, State state, ResultListener listener) {
            this.settings = settings;
            this.state = state;
            this.listener = listener;
        }

        @Override
        public void run() {
            resultAccumulator = new LegResult(settings.getNCamels(), state.getLegWinnerGains());
            positions(state);
            resultAccumulator.finish();
            result = resultAccumulator;
            listener.onCompleted();
        }

        private void positions(State state) {
            if (state == null) {
                return;
            }
            if (state.isGameEnd()) {
                resultAccumulator.addFinal(state.listCamelsByPosition());
            } else if (!state.areDiceLeft()) {
                resultAccumulator.addPositions(state.listCamelsByPosition());
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
        private LegResult resultAccumulator;
    }
}
