package com.app.efficiclean.classes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Observable;
import java.util.Observer;

public class QueueHandler implements Observer {

    private String hotelID;
    private JobQueue jQueue;
    private TeamQueue tQueue;

    public QueueHandler(String hid, JobQueue jq, TeamQueue tq) {
        hotelID = hid;
        jQueue = jq;
        tQueue = tq;
    }

    public void update(Observable obj, Object queue) {
        //Check if both queues are not empty
        if (!jQueue.isEmpty() && !tQueue.isEmpty()) {
            //Get job and team from queues and assign them to each other
            Job job = jQueue.dequeue();
            Team team = tQueue.dequeue();
            job.assignTo(team.key);
            team.setCurrentJob(job);

            //Update values in database
            DatabaseReference mTeamRef = FirebaseDatabase.getInstance().getReference(hotelID).child("teams");
            DatabaseReference mRoomRef = FirebaseDatabase.getInstance().getReference(hotelID).child("rooms");
            mRoomRef.child(job.getRoomNumber()).child("status").setValue("In Process");
            mTeamRef.child(team.key).setValue(team);
            tQueue.clearQueue();
        }
    }
}
