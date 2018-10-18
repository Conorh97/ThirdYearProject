package com.app.efficiclean;

import com.app.efficiclean.classes.Job;
import org.junit.Test;

import static org.junit.Assert.*;

public class JobTest {

    @Test
    public void test() {
        Job job1 = new Job();
        job1.setPriority(0);
        job1.setRoomNumber("101");
        job1.setDescription("Clean room");
        job1.setCreatedBy("Manager");

        assertFalse(job1.getStatus());
        assertTrue(job1.getTimestamp() > 0);
        assertNotNull(job1.getCreatedBy());
        assertNotNull(job1.getDescription());
        assertNotNull(job1.getPriority());
        assertNotNull(job1.getRoomNumber());

        Job job2 = new Job();
        job2.setPriority(0);
        job2.setRoomNumber("304");
        job2.setDescription("Wash sink");
        job2.setCreatedBy("Manager");

        assertEquals(job1.getPriority(), job2.getPriority());
        assertTrue(job1.getTimestamp() <= job2.getTimestamp());
        assertEquals(job1.getCreatedBy(), job2.getCreatedBy());
        assertNotEquals(job1.getDescription(), job2.getDescription());

        job1.markCompleted();
        assertTrue(job1.getStatus());
    }
}
