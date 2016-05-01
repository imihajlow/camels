package tk.imihajlov.camelup.engine;

public class Engine {
    public interface ResultListener {
        void onCompleted(LegResult result);

        void onInterrupted();
    }

    public void setSettings(Settings settings) {
        if (!settings.equals(this.settings)) {
            this.settings = settings;
            this.state = null;
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

    public boolean calculate(ResultListener listener) {
        if (settings == null || state == null || listener == null) {
            return false;
        }
        result = new Result(settings, state, listener);
        (new Thread(result)).start();
        return true;
    }

    private Settings settings;
    private State state;
    private Result result;

    private class Result implements Runnable {

        public Result(Settings settings, State state, ResultListener listener) {
            this.settings = settings;
            this.state = state;
            this.listener = listener;
        }

        @Override
        public void run() {
            result = new LegResult(settings.getNCamels(), state.getLegWinnerGains());
            positions(state);
            result.finish();
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
        private LegResult result;
    }
}
