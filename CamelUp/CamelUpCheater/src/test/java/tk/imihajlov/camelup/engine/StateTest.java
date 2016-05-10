package tk.imihajlov.camelup.engine;

import org.junit.Test;

import static org.junit.Assert.*;

public class StateTest {

    private State createState() {
        Settings settings = new Settings();
        CamelPosition[] camels = new CamelPosition[] {
                new CamelPosition(1, 1),
                new CamelPosition(1, 0),
                new CamelPosition(0, 0),
                new CamelPosition(2, 0),
                new CamelPosition(0, 1)
        };
        return State.createOnLegBegin(settings, camels);
    }

    @Test
    public void testAreDiceLeft() throws Exception {
        State state = createState();
        assertEquals(true, state.areDiceLeft());
        state = state.jump(0, 2).jump(1,1).jump(2, 3).jump(3, 1).jump(4, 2);
        assertEquals(false, state.areDiceLeft());
    }

    @Test
    public void testListCamelsByPosition() throws Exception {
        State state = createState();
        int[] camels = state.listCamelsByPosition();
        assertArrayEquals(new int[] {3,0,1,4,2}, camels);
    }

    @Test
    public void testPutOasis() throws Exception {
        State state = createState();
        assertEquals(0, state.getOasises().length);
        assertEquals(null, state.putOasis(2));
        state = state.putOasis(3);
        assertNotEquals(null, state);
        assertArrayEquals(new int[] {3}, state.getOasises());
        assertEquals(null, state.putOasis(4));

        // To many oasises
        state = createState();
        state = state.putOasis(9);
        assertNotEquals(null, state);
        state = state.putOasis(11);
        assertNotEquals(null, state);
        state = state.putOasis(13);
        assertNotEquals(null, state);
        state = state.putOasis(15);
        assertEquals(null, state);
    }

    @Test
    public void testJump() throws Exception {
        State state = createState();
        // Jump single camel
        State newState = state.jump(0, 2);
        assertNotEquals(null, newState);
        assertEquals(new CamelPosition(3,0), newState.getCamelPosition(0));
        assertEquals(new CamelPosition(1,0), newState.getCamelPosition(1));
        assertEquals(new CamelPosition(0,0), newState.getCamelPosition(2));
        assertEquals(new CamelPosition(2,0), newState.getCamelPosition(3));
        assertEquals(new CamelPosition(0,1), newState.getCamelPosition(4));
        // Jump two camels onto one
        State two = newState.jump(2, 1);
        assertNotEquals(null, two);
        assertEquals(new CamelPosition(3,0), two.getCamelPosition(0));
        assertEquals(new CamelPosition(1,0), two.getCamelPosition(1));
        assertEquals(new CamelPosition(1,1), two.getCamelPosition(2));
        assertEquals(new CamelPosition(2,0), two.getCamelPosition(3));
        assertEquals(new CamelPosition(1,2), two.getCamelPosition(4));
        // Jump over oasis
        State oasis = two.putOasis(4);
        assertNotEquals(null, oasis);
        State over = oasis.jump(3, 2);
        assertNotEquals(null, over);
        assertEquals(new CamelPosition(3,0), over.getCamelPosition(0));
        assertEquals(new CamelPosition(1,0), over.getCamelPosition(1));
        assertEquals(new CamelPosition(1,1), over.getCamelPosition(2));
        assertEquals(new CamelPosition(5,0), over.getCamelPosition(3));
        assertEquals(new CamelPosition(1,2), over.getCamelPosition(4));

        // Jump over mirage
        State mirage = state.putMirage(3);
        assertNotEquals(null, mirage);
        State under = mirage.jump(1, 2);
        assertNotEquals(null, under);
        assertEquals(new CamelPosition(2,1), under.getCamelPosition(0));
        assertEquals(new CamelPosition(2,0), under.getCamelPosition(1));
        assertEquals(new CamelPosition(0,0), under.getCamelPosition(2));
        assertEquals(new CamelPosition(2,2), under.getCamelPosition(3));
        assertEquals(new CamelPosition(0,1), under.getCamelPosition(4));

        // Finish game
        state = State.createOnLegBegin(new Settings(3,3,10), new CamelPosition[] {
                new CamelPosition(8,0),
                new CamelPosition(9,0),
                new CamelPosition(9,1)
        });
        assertNotEquals(null, state);
        State finish = state.jump(0, 2);
        assertNotEquals(null, finish);
        assertEquals(true, finish.isGameEnd());
        finish = state.jump(1,1);
        assertNotEquals(null, finish);
        assertEquals(true, finish.isGameEnd());
        assertEquals(new CamelPosition(8,0), finish.getCamelPosition(0));
        assertEquals(new CamelPosition(10,0), finish.getCamelPosition(1));
        assertEquals(new CamelPosition(10,1), finish.getCamelPosition(2));
    }

    @Test
    public void testValidateAndCreate() {
        Settings settings = new Settings();

        // Duplicate camel position
        assertEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {}, new int[] {}));

        // Floating camel
        assertEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,2),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {}, new int[] {}));

        assertEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,0),
                        new CamelPosition(2,1),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {}, new int[] {}));

        // Oasis under camel
        assertEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {}, new int[] {2}));

        // Mirage close to oasis
        assertEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {5}, new int[] {4}));

        assertEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {5}, new int[] {5}));

        // Correct
        assertNotEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {6}, new int[] {4}));

        // Too many oasises
        assertEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {}, new int[] {8,10,12,14}));

        // Enough oasises
        assertNotEquals(null,
                State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(1,0),
                        new CamelPosition(3,0),
                        new CamelPosition(1,1)
                }, new boolean[settings.getNCamels()], new int[] {}, new int[] {8,10,12}));
    }

    @Test
    public void testEquals() {
        State state1 = createState();
        State state2 = createState();
        assertEquals(state1, state2);
        State state3 = State.createOnLegBegin(state1.getSettings(), new CamelPosition[] {
                new CamelPosition(1, 0),
                new CamelPosition(1, 1),
                new CamelPosition(0, 0),
                new CamelPosition(2, 0),
                new CamelPosition(0, 1)
        });
        assertNotEquals(state1, state3);

        State state4 = state3.putMirage(5);
        assertNotEquals(state4, state3);
        State state5 = state3.putMirage(5);
        assertEquals(state4, state5);
    }

    @Test
    public void testReachablePositions() {
        Settings settings = new Settings();
        State state = State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(4,0),
                        new CamelPosition(8,0),
                        new CamelPosition(8,1)
            },
                new boolean[] { true, false, true, true, true },
                new int[] {7},
                new int[] {});
        assertNotNull(state);
        assertArrayEquals(new int[] {1,3,5,9,10,11,12,13,14}, state.getReachableDesertPositions());

        // Reachable positions should allow desert tiles
        for (int x: state.getReachableDesertPositions()) {
            assertNotNull("Cannot put a desert tile onto a reachable position", state.putMirage(x));
        }

        state = State.validateAndCreate(settings, new CamelPosition[] {
                        new CamelPosition(0,0),
                        new CamelPosition(2,0),
                        new CamelPosition(3,2),
                        new CamelPosition(3,0),
                        new CamelPosition(3,1)
                },
                new boolean[] { true, false, true, true, true },
                new int[] {},
                new int[] {9});
        assertNotNull(state);
        assertArrayEquals(new int[] {1,4,5,6,7,11,12,13}, state.getReachableDesertPositions());

        // Reachable positions should allow desert tiles
        for (int x: state.getReachableDesertPositions()) {
            assertNotNull("Cannot put a desert tile onto a reachable position", state.putMirage(x));
        }
    }
}