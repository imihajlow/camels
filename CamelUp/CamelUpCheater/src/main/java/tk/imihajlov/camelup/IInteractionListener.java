package tk.imihajlov.camelup;

import tk.imihajlov.camelup.engine.Settings;
import tk.imihajlov.camelup.engine.State;
import tk.imihajlov.camelup.engine.suggesters.ISuggester;
import tk.imihajlov.camelup.engine.suggesters.PositionsSuggester;

public interface IInteractionListener {
    void onGameStateUpdated(Object source, State state);

    void onCalculatePressed();

    Settings getSettings();

    State getState();

    ISuggester getActionsSuggester();

    PositionsSuggester getPositionsSuggester();
}
