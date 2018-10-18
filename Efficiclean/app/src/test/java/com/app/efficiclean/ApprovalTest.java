package com.app.efficiclean;

import com.app.efficiclean.classes.Approval;
import com.app.efficiclean.classes.HazardApproval;
import com.app.efficiclean.classes.Job;
import com.app.efficiclean.classes.SevereMessApproval;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApprovalTest {

    @Test
    public void test() {
        Job job1 = new Job();
        job1.setPriority(0);
        job1.setRoomNumber("101");
        job1.setDescription("Clean room");
        job1.setCreatedBy("Conor");

        Job job2 = new Job();
        job2.setPriority(1);
        job2.setRoomNumber("205");
        job2.setDescription("Clean room");
        job2.setCreatedBy("Luke");

        Job job3 = new Job();
        job3.setPriority(0);
        job3.setRoomNumber("310");
        job3.setDescription("Clean room");
        job3.setCreatedBy("Shauna");

        Approval service = new Approval();
        service.setCreatedBy("Dave");
        service.setJob(job1);

        HazardApproval hazard = new HazardApproval();
        hazard.setCreatedBy("Shane");
        hazard.setDescription("Corrosive chemicals in bath.");
        hazard.setJob(job2);

        SevereMessApproval severeMess = new SevereMessApproval();
        severeMess.setCreatedBy("Brian");
        severeMess.setDescription("Bathroom floor flooded.");
        severeMess.setJob(job3);

        assertFalse(service.getApproved());
        assertFalse(hazard.getApproved());
        assertFalse(severeMess.getApproved());

        assertNotNull(service.getCreatedBy());
        assertNotNull(service.getJob());
        assertNotNull(service.getPriorityCounter());

        assertNotNull(hazard.getDescription());
        assertNotNull(hazard.getCreatedBy());
        assertNotNull(hazard.getJob());
        assertNotNull(hazard.getPriorityCounter());

        assertNotNull(severeMess.getDescription());
        assertNotNull(severeMess.getCreatedBy());
        assertNotNull(severeMess.getJob());
        assertNotNull(severeMess.getPriorityCounter());

        assertTrue(hazard.getPriorityCounter() > severeMess.getPriorityCounter());
        assertTrue(hazard.getPriorityCounter() > service.getPriorityCounter());
        assertTrue(severeMess.getPriorityCounter() > service.getPriorityCounter());

        service.incrementPriorityCounter();

        assertTrue(hazard.getPriorityCounter() > service.getPriorityCounter());
        assertEquals(severeMess.getPriorityCounter(), service.getPriorityCounter());

        service.setApproved(true);

        assertTrue(service.getApproved());
        assertFalse(service.getJob().getStatus());

        service.getJob().markCompleted();

        assertTrue(service.getJob().getStatus());
    }
}
