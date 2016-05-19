package tk.imihajlov.camelup.engine.suggesters;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;

import tk.imihajlov.camelup.engine.PlayerAction;
import tk.imihajlov.camelup.engine.actions.Dice;

public class DiceSuggester implements Serializable, ISuggester {
    @Override
    public ImmutableList<PlayerAction> getSuggestedActions() {
        return ImmutableList.<PlayerAction>of(new Dice());
    }
}
