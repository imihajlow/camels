package tk.imihajlov.camelup.engine;

import java.io.Serializable;

public class Settings implements Serializable {
    public final static int maxDieValue = 3;
    private int nPlayers = 3;
    private int nCamels = 5;
    private int nSteps = 16;

    public Settings() {
    }

    public Settings(int nPlayers) {
        this.nPlayers = nPlayers;
    }

    public Settings(int nPlayers, int nCamels, int nSteps) {
        this.nPlayers = nPlayers;
        this.nCamels = nCamels;
        this.nSteps = nSteps;
    }

    public int getNPlayers() {
        return nPlayers;
    }

    public int getNCamels() {
        return nCamels;
    }

    public int getNSteps() {
        return nSteps;
    }

    @Override
    public boolean equals(Object o) {
        Settings s = (Settings) o;
        if (s != null) {
            return s.nPlayers == nPlayers && s.nSteps == nSteps && s.nCamels == nCamels;
        } else {
            return false;
        }
    }
}
