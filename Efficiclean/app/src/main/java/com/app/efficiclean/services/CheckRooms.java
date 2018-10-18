package com.app.efficiclean.services;

import android.os.Bundle;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.*;

public class CheckRooms extends JobService {

    private String hid;
    private Bundle extras;
    private DatabaseReference mRootRef;
    private DataSnapshot teams;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Get parameters passed to service
        extras = jobParameters.getExtras();
        hid = extras.getString("hid");

        //Create reference to relevant hotel in Firebase
        mRootRef = FirebaseDatabase.getInstance().getReference(hid);
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store teams datasnapshot
                teams = dataSnapshot.child("teams");
                allocateMarkingTask();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public void allocateMarkingTask() {
        /*
            This task allocates a team the task of adding manually to the queue.
            This is for guests who are still using the old system of hanging signs on doors
            to ensure that their rooms are still serviced.
         */

        //Iterate through all teams
        for (DataSnapshot team : teams.getChildren()) {
            String status = team.child("status").getValue(String.class);

            //Check if team is already checking rooms
            if (!status.equals("Checking Rooms")) {
                //Update team status in database and break so that only one team is allocated this task
                mRootRef.child("teams").child(team.getKey()).child("status").setValue("Checking Rooms");
                break;
            }
        }
    }
}
