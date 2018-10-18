package com.app.efficiclean;

import com.app.efficiclean.classes.Break;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class BreakTest {

    @Test
    public void test() {
        Break break1 = new Break();
        break1.setTeamID("Team A");
        break1.setBreakTime("1230");
        break1.setBreakLength(45);

        Calendar t1 = Calendar.getInstance();
        t1.set(Calendar.HOUR_OF_DAY, 12);
        t1.set(Calendar.MINUTE, 30);
        t1.set(Calendar.SECOND, 0);
        Date time1 = t1.getTime();

        Calendar t2 = Calendar.getInstance();
        t2.set(Calendar.HOUR_OF_DAY, 14);
        t2.set(Calendar.MINUTE, 20);
        t2.set(Calendar.SECOND, 10);
        Date time2 = t2.getTime();

        assertFalse(break1.isAccepted());
        assertNotNull(break1.getBreakLength());
        assertNotNull(break1.getBreakTime());
        assertNotNull(break1.getTeamID());

        assertEquals(time1.getHours(), break1.getBreakTime().getHours());
        assertEquals(time1.getMinutes(), break1.getBreakTime().getMinutes());
        assertEquals(time1.getSeconds(), break1.getBreakTime().getSeconds());

        assertNotEquals(time2.getHours(), break1.getBreakTime().getHours());
        assertNotEquals(time2.getMinutes(), break1.getBreakTime().getMinutes());
        assertNotEquals(time2.getSeconds(), break1.getBreakTime().getSeconds());

        break1.setAccepted(true);

        assertTrue(break1.isAccepted());
        assertTrue(break1.getBreakLength() >= 0);
    }
}
