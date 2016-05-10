package tk.imihajlov.camelup.engine;

import org.junit.Test;

import java.io.InterruptedIOException;

import tk.imihajlov.camelup.engine.actions.BetLegWinner;
import tk.imihajlov.camelup.engine.actions.Dice;
import tk.imihajlov.camelup.engine.actions.PlayerActionVisitor;
import tk.imihajlov.camelup.engine.actions.PutDesert;

import static org.junit.Assert.*;

public class EngineTest {

    private LegResult calculateResult(Settings s, State state) {
        Engine e = new Engine();
        e.setSettings(s);
        e.setState(state);
        class Listener implements Engine.ResultListener {
            public boolean isCompleted = false;
            public boolean isInterrupted = false;

            @Override
            public void onCompleted() {
                isCompleted = true;
            }

            @Override
            public void onInterrupted() {
                isInterrupted = true;
            }
        }

        Listener listener = new Listener();
        Thread t = e.getCalculatorThread(listener);
        t.start();
        try {
            t.join();
        } catch (InterruptedException exc) {
            assertTrue("Interrupted", false);
        }
        assertTrue("Should be completed", listener.isCompleted);
        assertFalse("Should not be interrupted", listener.isInterrupted);
        assertNotNull(e.getResult());
        return e.getResult();
    }

    @Test
    public void testDesertGains() {
        final Settings s = new Settings(3);
        State state = State.validateAndCreate(s, new CamelPosition[] {
                    new CamelPosition(0, 0),
                    new CamelPosition(0, 1),
                    new CamelPosition(1, 0),
                    new CamelPosition(1, 1),
                    new CamelPosition(2, 0)
                },
                new boolean[] { false, false, false, false, true },
                new int[] {},
                new int[] {});
        LegResult result = calculateResult(s, state);

        class ActionChecker implements PlayerActionVisitor {
            public int desertTipsCount = 0;

            public void visit(BetLegWinner action) {

            }
            public void visit(Dice action) {

            }
            public void visit(PutDesert action) {
                // Placing deserts on invalid cells should not be suggested
                assertTrue("Invalid cell for a desert", action.getX() > 2 && action.getX() < s.getNSteps());
                if (action.getX() <= 5) {
                    // Reachable deserts should give equal gains of 1/3
                    assertEquals("Invalid gain", 0.33, action.getProfitExpectation(), 0.01);
                } else {
                    // Unreachable desert should give zero gain
                    assertEquals("Non-zero gain for unreachable desert", 0, action.getProfitExpectation(), 0.01);
                }
                desertTipsCount += 1;
            }
        }

        ActionChecker checker = new ActionChecker();
        for (PlayerAction action : result.getSuggestedActions()) {
            action.accept(checker);
        }
        assertEquals("Invalid number of suggested desert tips", state.getReachableDesertPositions().length * 2, checker.desertTipsCount);
    }
}
