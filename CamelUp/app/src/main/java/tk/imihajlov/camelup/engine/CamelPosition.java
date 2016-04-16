package tk.imihajlov.camelup.engine;

public class CamelPosition {
    private int x;
    private int y;

    public CamelPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CamelPosition)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        CamelPosition cp = (CamelPosition)o;
        return cp.x == x && cp.y == y;
    }
}
