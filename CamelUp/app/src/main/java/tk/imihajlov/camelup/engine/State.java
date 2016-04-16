package tk.imihajlov.camelup.engine;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class State {
    private enum CellState {
        EMPTY,
        CAMEL,
        PLUS,
        MINUS;

        public boolean isPlusOrMinus() {
            return this.equals(PLUS) || this.equals(MINUS);
        }
    }

    private Settings settings;

    private boolean[] dice;
    private CamelPosition[] camels;
    private CellState[] cells;

    private State(Settings settings, CamelPosition[] camels, boolean[] dice, CellState[] cells) {
        this.settings = settings;
        this.dice = dice.clone();
        this.camels = camels.clone();
        this.cells = cells.clone();
    }

    /** Create new state with given camels positions, no +1/-1 and all dice vacant.
     *
     * @param settings game settings
     * @param camels initial camels positions
     * @return new State.
     */
    public static State createOnLegBegin(Settings settings, CamelPosition[] camels) {
        assert settings.getNCamels() == camels.length : "Different camels count in settings and in camels parameter";
        boolean[] dice = new boolean[settings.getNCamels()];
        Arrays.fill(dice, true);
        CellState[] cells = new CellState[settings.getNSteps()];
        Arrays.fill(cells, CellState.EMPTY);
        for (CamelPosition p: camels) {
            assert p.getX() >= 0 && p.getX() < settings.getNSteps() : "Wrong camel X position";
            cells[p.getX()] = CellState.CAMEL;
        }
        return new State(settings, camels, dice, cells);
    }

    public boolean[] getDice() {
        return dice;
    }

    public boolean areDiceLeft() {
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
        if (camel < 0
                || camel >= settings.getNCamels()
                || steps < 1
                || steps > settings.maxDieValue) {
            return null;
        }
        boolean[] dice = this.dice.clone();
        dice[camel] = false;

        CamelPosition from = camels[camel];
        int xTo = from.getX() + steps;
        if (xTo >= settings.getNSteps()) {
            return null; // TODO finish the game
        }
        boolean under = cells[xTo] == CellState.MINUS;
        if (cells[xTo] == CellState.PLUS) {
            xTo += 1;
        } else if (cells[xTo] == CellState.MINUS) {
            xTo -= 1;
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

    public CamelPosition getCamelPosition(int n) {
        return camels[n];
    }

    public int[] getOasises() {
        return getCellStates(CellState.PLUS);
    }

    public int[] getMirages() {
        return getCellStates(CellState.MINUS);
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
        if (position <= 0
                || position >= settings.getNSteps()
                || cells[position] != CellState.EMPTY
                || cells[position - 1].isPlusOrMinus()
                || (position < settings.getNSteps() - 1 && cells[position + 1].isPlusOrMinus())) {
            return null;
        }
        CellState[] cells = this.cells.clone();
        cells[position] = state;
        return new State(this.settings, this.camels, this.dice, cells);
    }
}
