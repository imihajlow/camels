package tk.imihajlov.camelup.engine.suggesters;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;

import tk.imihajlov.camelup.engine.PlayerAction;
import tk.imihajlov.camelup.engine.actions.PutDesert;

public class DesertSuggester implements Serializable, ISuggester {
    public interface IDesertGain {
        boolean isOasis();
        int getX();
        double getGain();
    }

    public DesertSuggester(int x, boolean isOasis) {
        myDesert = new Desert(x, isOasis);
    }

    public void legEnded() {
        myDesert.legEnded();
    }

    public void countDesert(int x) {
        if (x == myDesert.x) {
            myDesert.hit();
        }
    }

    @Override
    public ImmutableList<PlayerAction> getSuggestedActions() {
        return ImmutableList.<PlayerAction>of(new PutDesert(myDesert.getGain(), myDesert.getX(), myDesert.isOasis()));
    }

    private class Desert implements Serializable, IDesertGain {
        int x;
        boolean oasis;
        int nHits;
        int nResults;

        Desert(int x, boolean isOasis) {
            this.x = x;
            this.oasis = isOasis;
        }

        void hit() {
            nHits += 1;
        }

        void legEnded() {
            nResults += 1;
        }

        @Override
        public boolean isOasis() {
            return oasis;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public double getGain() {
            if (nResults != 0) {
                return (double) nHits / (double) nResults;
            } else {
                return 0;
            }
        }
    }
    private Desert myDesert;
}
