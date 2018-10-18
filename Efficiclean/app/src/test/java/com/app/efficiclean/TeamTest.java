package com.app.efficiclean;

import com.app.efficiclean.classes.Job;
import com.app.efficiclean.classes.Team;
import org.junit.Test;

import static junit.framework.Assert.*;

public class TeamTest {

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

        Team team = new Team();
        team.setCleanCounter(0);
        team.setPriorityCounter(0);
        team.setStatus("Waiting");
        team.setCurrentJob(job1);
        team.setReturnedJob(job2);

        assertNotNull(team.getBreakRemaining());
        assertNotNull(team.getCleanCounter());
        assertNotNull(team.getCurrentJob());
        assertNotNull(team.getMembers());
        assertNotNull(team.getPriorityCounter());
        assertNotNull(team.getStatus());
        assertNotNull(team.getReturnedJob());

        assertEquals(team.getBreakRemaining(), 60);
        assertEquals(team.getMembers().size(), 0);

        team.addMember("James");
        team.addMember("Derek");
        team.addMember("Holly");
        team.addMember("Anne");

        assertEquals(team.getMembers().size(), 4);
        assertEquals(team.removeMember(1), "Derek");
        assertEquals(team.getMembers().size(), 3);
        assertNotSame(team.getCurrentJob(), team.getReturnedJob());
    }
}
