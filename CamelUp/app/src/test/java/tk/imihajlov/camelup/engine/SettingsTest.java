package tk.imihajlov.camelup.engine;

import org.junit.Test;

import static org.junit.Assert.*;

public class SettingsTest {

    @Test
    public void test() throws Exception {
        Settings s = new Settings();
        assertEquals(3, s.getNPlayers());
        assertEquals(5, s.getNCamels());
        assertEquals(16, s.getNSteps());
    }
}