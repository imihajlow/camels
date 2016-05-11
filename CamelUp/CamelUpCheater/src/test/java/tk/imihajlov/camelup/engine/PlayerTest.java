package tk.imihajlov.camelup.engine;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {
    @Test
    public void testTakeCard() {
        LegWinnerCard card1 = new LegWinnerCard(1,5);
        LegWinnerCard card2 = new LegWinnerCard(1,3);
        LegWinnerCard card3 = new LegWinnerCard(2,5);
        Player p0 = new Player(0);
        Player p1 = p0.takeLegWinnerCard(card1);
        Player p2 = p1.takeLegWinnerCard(card2);
        Player p3 = p2.takeLegWinnerCard(card3);

        ImmutableList<LegWinnerCard> list = ImmutableList.of(card1,card2,card3);
        assertEquals(list, p3.getLegWinnerCards());
    }
}
