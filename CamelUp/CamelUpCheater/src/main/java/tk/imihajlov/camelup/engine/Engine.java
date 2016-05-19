package tk.imihajlov.camelup.engine;

import java.io.IOException;
import java.io.Serializable;

import tk.imihajlov.camelup.engine.suggesters.CompositeSuggester;
import tk.imihajlov.camelup.engine.suggesters.DesertSuggester;
import tk.imihajlov.camelup.engine.suggesters.DiceSuggester;
import tk.imihajlov.camelup.engine.suggesters.ISuggester;
import tk.imihajlov.camelup.engine.suggesters.PositionsSuggester;

public class Engine implements Serializable {
    public interface IResultListener {
        void onCompleted();

        void onInterrupted();
    }

    public void setSettings(Settings settings) {
        if (!settings.equals(this.settings)) {
            this.settings = settings;
            this.state = null;
            this.resultCalculator = null;
            this.totalSuggester = null;
            this.positionsSuggester = null;
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

    public ISuggester getActionsSuggester() {
        return totalSuggester;
    }

    public PositionsSuggester getPositionsSuggester() {
        return positionsSuggester;
    }

    public Thread getCalculatorThread(IResultListener listener) {
        if (settings == null || state == null || listener == null) {
            return null;
        }
        resultCalculator = new ResultCalculator(settings, state, listener);
        return new Thread(resultCalculator);
    }

    public boolean calculateAsync(IResultListener listener) {
        Thread t = getCalculatorThread(listener);
        if (t != null) {
            t.start();
            return true;
        } else {
            return false;
        }
    }


    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(settings);
        stream.writeObject(state);
        stream.writeObject(totalSuggester);
        stream.writeObject(positionsSuggester);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        settings = (Settings) stream.readObject();
        state = (State) stream.readObject();
        totalSuggester = (CompositeSuggester) stream.readObject();
        positionsSuggester = (PositionsSuggester) stream.readObject();
        resultCalculator = null;
    }

    private Settings settings;
    private State state;
    private ResultCalculator resultCalculator;
    private CompositeSuggester totalSuggester;
    private PositionsSuggester positionsSuggester;

    private class ResultCalculator implements Runnable {

        public ResultCalculator(Settings settings, State state, IResultListener listener) {
            this.settings = settings;
            this.state = state;
            this.listener = listener;
        }

        @Override
        public void run() {
            positionsSuggester = new PositionsSuggester(settings.getNCamels(), state.getTopLegWinnerCards());
            totalSuggester = new CompositeSuggester(positionsSuggester, new DiceSuggester());
            positions(state, positionsSuggester, null);
            positionsSuggester.finish();

            for (int x: state.getReachableDesertPositions()) {
                DesertSuggester desertSuggester = new DesertSuggester(x, false);
                State newState = state.putMirage(x);
                positions(newState, null, desertSuggester);
                totalSuggester.add(desertSuggester);

                desertSuggester = new DesertSuggester(x, true);
                newState = state.putOasis(x);
                positions(newState, null, desertSuggester);
                totalSuggester.add(desertSuggester);
            }

            Engine.this.positionsSuggester = positionsSuggester;
            Engine.this.totalSuggester = totalSuggester;
            listener.onCompleted();
        }

        private void positions(State state, PositionsSuggester positionsSuggester, DesertSuggester desertSuggester) {
            if (state == null) {
                return;
            }
            if (state.isGameEnd()) {
                if (positionsSuggester != null) {
                    positionsSuggester.addFinal(state.listCamelsByPosition());
                }
                if (desertSuggester != null) {
                    desertSuggester.legEnded();
                }
            } else if (!state.areDiceLeft()) {
                if (positionsSuggester != null) {
                    positionsSuggester.addPositions(state.listCamelsByPosition());
                }
                if (desertSuggester != null) {
                    desertSuggester.legEnded();
                }
            } else {
                boolean[] dice = state.getDice();
                for (int i = 0; i < dice.length; ++i) {
                    if (dice[i]) {
                        for (int v = 1; v <= settings.maxDieValue; ++v) {
                            if (desertSuggester != null) {
                                desertSuggester.countDesert(state.willStepOnDesert(i, v));
                            }
                            positions(state.jump(i, v), positionsSuggester, desertSuggester);
                        }
                    }
                }
            }
        }

        private Settings settings;
        private State state;
        private IResultListener listener;
        private CompositeSuggester totalSuggester;
        private PositionsSuggester positionsSuggester;
    }
}
