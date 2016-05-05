package tk.imihajlov.camelup;

import tk.imihajlov.camelup.engine.LegResult;
import tk.imihajlov.camelup.engine.Settings;
import tk.imihajlov.camelup.engine.State;

public interface InteractionListener {
    void onGameStateUpdated(Object source, State state);

    void onCalculatePressed();

    Settings getSettings();

    State getState();

    LegResult getResult();
}