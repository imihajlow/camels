package tk.imihajlov.camelup.engine;


import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.lang3.ArrayUtils;

public class State implements Serializable {
    private enum CellState implements Serializable {
        EMPTY,
        CAMEL,
        PLUS,
        MINUS;

        public boolean isPlusOrMinus() {
            return this.equals(PLUS) || this.equals(MINUS);
        }
    }

    private Settings settings;

    private boolean gameEnd;
    private boolean[] dice;
    private CamelPosition[] camels;
    private CellState[] cells;
    private List<List<Integer>> legBetCards;

    private State(Settings settings, CamelPosition[] camels, boolean[] dice, CellState[] cells) {
        this.settings = settings;
        this.dice = dice.clone();
        this.camels = camels.clone();
        this.cells = cells.clone();
        this.gameEnd = false;
        this.legBetCards = new ArrayList<List<Integer>>(settings.getNCamels());
        for (int i = 0; i < settings.getNCamels(); ++i) {
            List<Integer> q = new ArrayList<Integer>();
            this.legBetCards.add(q);
            q.add(5);
            q.add(3);
            q.add(2);
        }
    }

    @Override
    public boolean equals(Object o) {
        State s = (State) o;
        if (s != null) {
            return settings.equals(s.settings)
                    && gameEnd == s.gameEnd
                    && Arrays.equals(dice, s.dice)
                    && Arrays.equals(camels, s.camels)
                    && Arrays.equals(cells, s.cells)
                    && legBetCards.equals(s.legBetCards);
        }
        return false;
    }

    /** State at the endgame.
     *
     * @param settings
     * @param camels final camels positions.
     */
    private State(Settings settings, CamelPosition[] camels) {
        this.settings = settings;
        this.camels = camels.clone();
        this.gameEnd = true;
    }

    /** Create new state with given camels positions, no +1/-1 and all dice vacant.
     *
     * @param settings game settings
     * @param camels initial camels positions
     * @return new State.
     */
    public static State createOnLegBegin(Settings settings, CamelPosition[] camels) {

        boolean[] dice = new boolean[settings.getNCamels()];
        Arrays.fill(dice, true);
        return validateAndCreate(settings, camels, dice, new int[] {}, new int[] {});
    }

    public static State validateAndCreate(Settings settings, CamelPosition[] camels, boolean[] dice, int[] mirages, int[] oasises) {
        if (camels.length != settings.getNCamels() || dice.length != settings.getNCamels()
                || mirages.length + oasises.length > settings.getNPlayers()) {
            return null;
        }
        for (CamelPosition p : camels) {
            if (p == null || p.getX() < 0 || p.getX() >= settings.getNSteps() || p.getY() < 0) {
                return null;
            }
        }
        for (int x : mirages) {
            if (x < 0 || x >= settings.getNSteps()) {
                return null;
            }
        }
        for (int x : oasises) {
            if (x < 0 || x >= settings.getNSteps()) {
                return null;
            }
        }
        // Camel positions must be distinct
        CamelPosition[] sortedCamels = camels.clone();
        Arrays.sort(sortedCamels);
        CamelPosition last = null;
        for (CamelPosition p : sortedCamels) {
            if (p.equals(last)) {
                return null;
            }
            last = p;
        }
        // Max Y for each X must be the same as number-of-camels-for-that-X - 1
        int[] max = new int[settings.getNSteps()];
        int[] cnt = new int[settings.getNSteps()];
        for (CamelPosition p : camels) {
            max[p.getX()] = Math.max(max[p.getX()], p.getY());
            cnt[p.getX()] += 1;
        }
        for (int i = 0; i < max.length; ++i) {
            if (cnt[i] > 0 && cnt[i] != max[i] + 1) {
                return null;
            }
        }

        // Fill cell state
        CellState[] cells = new CellState[settings.getNSteps()];
        Arrays.fill(cells, CellState.EMPTY);
        for (CamelPosition p: camels) {
            cells[p.getX()] = CellState.CAMEL;
        }
        State state = new State(settings, camels, dice, cells);

        // Try adding mirages and oasises
        for (int x : mirages) {
            state = state.putMirage(x);
            if (state == null) {
                return null;
            }
        }
        for (int x : oasises) {
            state = state.putOasis(x);
            if (state == null) {
                return null;
            }
        }
        return state;
    }

    public boolean[] getDice() {
        return gameEnd ? null : dice;
    }

    public Settings getSettings() {
        return settings;
    }

    public boolean areDiceLeft() {
        if (gameEnd) {
            return false;
        }
        for (boolean d : dice) {
            if (d) {
                return true;
            }
        }
        return false;
    }

    /** Lists camel numbers by their positions.
     *
     * @return camel positions from the leader to the looser.
     */
    public int[] listCamelsByPosition() {
        class R implements Comparable<R> {
            public int n;
            public int p;

            public R(CamelPosition position, int n) {
                this.n = n;
                this.p = position.getX() * settings.getNCamels() + position.getY();
            }

            @Override
            public int compareTo(R other) {
                return other.p - p;
            }
        }
        R[] r = new R[camels.length];
        for (int i = 0; i < camels.length; ++i) {
            r[i] = new R(camels[i], i);
        }
        Arrays.sort(r);
        int[] result = new int[r.length];
        for (int i = 0; i < camels.length; ++i) {
            result[i] = r[i].n;
        }
        return result;
    }

    /** Put an oasis card (+1) onto the given position.
     *
     * @param position
     * @return null if the position is incorrect.
     */
    public State putOasis(int position) {
        return put(position, CellState.PLUS);
    }

    /** Put a mirage card (-1) onto the given position.
     *
     * @param position
     * @return null if the position is incorrect.
     */
    public State putMirage(int position) {
        return put(position, CellState.MINUS);
    }

    /** Roll a die and jump. The die gets reset (goes out from pyramid).
     *
     * @param camel camel number.
     * @param steps diced number of steps.
     * @return new State with camels moved and die reset.
     */
    public State jump(int camel, int steps) {
        if (gameEnd
                || camel < 0
                || camel >= settings.getNCamels()
                || steps < 1
                || steps > settings.maxDieValue) {
            return null;
        }
        boolean[] dice = this.dice.clone();
        dice[camel] = false;

        CamelPosition from = camels[camel];
        int xTo = from.getX() + steps;
        boolean under;
        boolean finish = false;
        if (xTo >= settings.getNSteps()) {
            under = false;
            finish = true;
        } else {
            under = cells[xTo] == CellState.MINUS;
            if (cells[xTo] == CellState.PLUS) {
                xTo += 1;
            } else if (cells[xTo] == CellState.MINUS) {
                xTo -= 1;
            }
        }
        CamelPosition[] newCamels = new CamelPosition[settings.getNCamels()];
        if (!under) {
            int yOffset = 0; // Number of camels on the "to" cell.
            for (CamelPosition p : camels) {
                if (p.getX() == xTo) {
                    yOffset += 1;
                }
            }
            for (int i = 0; i < camels.length; ++i) {
                CamelPosition p = camels[i];
                if (p.getX() == from.getX() && p.getY() >= from.getY()) {
                    newCamels[i] = new CamelPosition(xTo, yOffset + p.getY() - from.getY());
                } else {
                    newCamels[i] = p;
                }
            }
        } else {
            int yOffset = 0; // Number of camels to move.
            for (CamelPosition p : camels) {
                if (p.getX() == from.getX() && p.getY() >= from.getY()) {
                    yOffset += 1;
                }
            }
            for (int i = 0; i < camels.length; ++i) {
                CamelPosition p = camels[i];
                if (p.getX() == from.getX() && p.getY() >= from.getY()) {
                    // One of the camels that moves forward.
                    newCamels[i] = new CamelPosition(xTo, p.getY() - from.getY());
                } else if (p.getX() == xTo) {
                    // One of the camels on the "to" cell - goes up.
                    newCamels[i] = new CamelPosition(p.getX(), p.getY() + yOffset);
                } else {
                    // Untouched camel
                    newCamels[i] = p;
                }
            }
        }

        if (finish) {
            return new State(settings, newCamels);
        }

        CellState[] newCells = new CellState[settings.getNSteps()];
        Arrays.fill(newCells, CellState.EMPTY);
        for (CamelPosition p : newCamels) {
            newCells[p.getX()] = CellState.CAMEL;
        }
        for (int i = 0; i < cells.length; ++i) {
            if (cells[i].isPlusOrMinus()) {
                assert newCells[i] == CellState.EMPTY : "Cell is not empty. WTF?";
                newCells[i] = cells[i];
            }
        }
        return new State(settings, newCamels, dice, newCells);
    }

    /**
     * Finds out if after dicing the camel will step on a desert tile.
     * @param camel camel number
     * @param dieValue value on the dice (number of steps to go)
     * @return -1 if the camel will not step on a desert or the x-coordinate of the desert tile.
     */
    public int willStepOnDesert(int camel, int dieValue) {
        int xTo = camels[camel].getX() + dieValue;
        if (xTo < settings.getNSteps()) {
            CellState state = cells[xTo];
            if (state == CellState.MINUS || state == CellState.PLUS) {
                return xTo;
            }
        }
        return -1;
    }

    /**
     * Get an array of positions suitable for placing desert tiles and
     * reachable by camels in the current round.
     * @return array of x-coordinates.
     */
    public int[] getReachableDesertPositions() {
        boolean[] result = new boolean[cells.length];
        int[] camelsOnCells = new int[cells.length];
        int[] maxJump = new int[cells.length];

        // Calculate number of active camels on each tile
        for (CamelPosition p : camels) {
            camelsOnCells[p.getX()] = Math.max(camelsOnCells[p.getX()], p.getY() + 1);
        }
        for (int i = 0; i < camels.length; ++i) {
            if (!dice[i]) {
                camelsOnCells[camels[i].getX()] -= 1;
            }
        }

        // Calculate maximum possible jump from each tile
        for (int x = 0; x < cells.length; ++x) {
            if (x + 3 < cells.length) {
                switch (cells[x + 3]) {
                    case PLUS:
                        maxJump[x] = Math.min(4, cells.length - x - 1);
                        break;
                    case MINUS:
                        maxJump[x] = 2;
                        break;
                    default:
                        maxJump[x] = 3;
                        break;
                }
            } else {
                maxJump[x] = cells.length - x - 1;
            }
        }

        // n camels jump n times
        for (int x = 0; x < camelsOnCells.length; ++x) {
            int xFrom = x;
            for (int n = 0; n < camelsOnCells[x]; ++n) {
                for (int jump = 1; jump <= maxJump[xFrom]; ++jump) {
                    result[xFrom + jump] = true;
                }
                xFrom = xFrom + maxJump[xFrom];
            }
        }

        for (int x = 1; x < cells.length; ++x) {
            if (cells[x] != CellState.EMPTY) {
                result[x] = false;
            }
            if (cells[x].isPlusOrMinus()) {
                result[x - 1] = false;
                if (x + 1 < result.length) {
                    result[x + 1] = false;
                }
            }
        }
        List<Integer> ps = new ArrayList<Integer>();
        for (int x = 1; x < result.length; ++x) {
            if (result[x]) {
                ps.add(x);
            }
        }
        return ArrayUtils.toPrimitive(ps.toArray(new Integer[0]));
    }

    public CamelPosition getCamelPosition(int n) {
        return camels[n];
    }

    public int[] getOasises() {
        return gameEnd ? null : getCellStates(CellState.PLUS);
    }

    public int[] getMirages() {
        return gameEnd ? null : getCellStates(CellState.MINUS);
    }

    public boolean isGameEnd() {
        return gameEnd;
    }

    public int[] getLegWinnerGains() {
        List<Integer> result = new ArrayList<Integer>();
        for (List<Integer> q : legBetCards) {
            if (q.size() > 0) {
                result.add(q.get(0));
            } else {
                result.add(0);
            }
        }
        return ArrayUtils.toPrimitive(result.toArray(new Integer[0]));
    }

    private int[] getCellStates(CellState state) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < cells.length; ++i) {
            if (cells[i] == state) {
                list.add(i);
            }
        }
        return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
    }

    private State put(int position, CellState state) {
        assert state == CellState.MINUS || state == CellState.PLUS : "put() only accepts PLUS or MINUS";
        if (gameEnd
                || position <= 0
                || position >= settings.getNSteps()
                || cells[position] != CellState.EMPTY
                || cells[position - 1].isPlusOrMinus()
                || (position < settings.getNSteps() - 1 && cells[position + 1].isPlusOrMinus())) {
            return null;
        }
        int n = 1;
        for (CellState s : cells) {
            if (s == CellState.MINUS || s == CellState.PLUS) {
                n += 1;
            }
        }
        if (n > settings.getNPlayers()) {
            return null;
        }
        CellState[] cells = this.cells.clone();
        cells[position] = state;
        return new State(this.settings, this.camels, this.dice, cells);
    }
}
