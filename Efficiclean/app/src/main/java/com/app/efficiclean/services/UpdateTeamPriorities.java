package com.app.efficiclean.services;

import android.os.Bundle;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.*;

public class UpdateTeamPriorities extends JobService {

    private String hid;
    private DatabaseReference mTeamRef;
    private DataSnapshot teams;
    private Bundle extras;

    @Override
    public boolean onStartJob(final JobParameters job) {
        //Get parameters passed to service
        extras = job.getExtras();
        hid = extras.getString("hid");

        //Create reference to the hotel teams in Firebase database
        mTeamRef = FirebaseDatabase.getInstance().getReference(hid).child("teams");
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teams = dataSnapshot;
                updatePriorities();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    public void updatePriorities() {
        /*
            This service increments the priority counter of all
            teams on the queue. This is so teams that are waiting
            the longest will be assigned to jobs first.
         */

        if (teams.hasChildren()) {
            for (DataSnapshot team : teams.getChildren()) {
                String key = team.getKey();
                if (team.hasChild("priorityCounter") && team.hasChild("status")) {
                    int priority = team.child("priorityCounter").getValue(int.class);
                    String status = team.child("status").getValue(String.class);
                    if (status.equals("Waiting")) {
                        priority++;
                        mTeamRef.child(key).child("priorityCounter").setValue(priority);
                    }
                }
            }
        }
    }
}
