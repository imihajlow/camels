package tk.imihajlov.camelup.engine.suggesters;

import com.google.common.collect.ImmutableList;

import tk.imihajlov.camelup.engine.PlayerAction;

public interface ISuggester {
    ImmutableList<PlayerAction> getSuggestedActions();
}
