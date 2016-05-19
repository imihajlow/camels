package tk.imihajlov.camelup.engine.suggesters;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tk.imihajlov.camelup.engine.PlayerAction;

public class CompositeSuggester implements Serializable, ISuggester {
    List<ISuggester> suggesters;

    public CompositeSuggester(ISuggester ...suggesters) {
        this.suggesters = new ArrayList<ISuggester>(suggesters.length);
        for (ISuggester suggester : suggesters) {
            add(suggester);
        }
    }

    public void add(ISuggester suggester) {
        suggesters.add(suggester);
    }

    @Override
    public ImmutableList<PlayerAction> getSuggestedActions() {
        List<PlayerAction> actions = new ArrayList<PlayerAction>();
        for (ISuggester suggester : suggesters) {
            actions.addAll(suggester.getSuggestedActions());
        }
        Collections.sort(actions);
        Collections.reverse(actions);
        return ImmutableList.copyOf(actions);
    }
}
