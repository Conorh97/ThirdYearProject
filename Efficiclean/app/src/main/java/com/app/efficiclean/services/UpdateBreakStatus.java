package com.app.efficiclean.services;

import android.os.Bundle;
import com.app.efficiclean.classes.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.Date;

public class UpdateBreakStatus extends JobService {

    private final int ONE_MINUTE = 60000;
    private String hid;
    private Bundle extras;
    private DatabaseReference mRootRef;
    private DataSnapshot breaks;
    private DataSnapshot teams;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Get parameters passed to service
        extras = jobParameters.getExtras();
        hid = extras.getString("hid");

        //Create reference to hotel in Firebase
        mRootRef = FirebaseDatabase.getInstance().getReference(hid);
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store breaks and teams data as datasnapshots
                breaks = dataSnapshot.child("breakRequests");
                teams = dataSnapshot.child("teams");
                checkForBreak();
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

    public void checkForBreak() {
        /*
            The purpose of this service is to mark housekeeping teams
            as "On Break" based on their break requests. They are removed from
            the queue and there current job is placed back on to the queue. They
            are made available again once their break is completed.
         */

        //Iterate through all breaks
        for (DataSnapshot currentBreak : breaks.getChildren()) {
            String key = currentBreak.getKey();

            //Check if the break request was accepted
            if (currentBreak.child("accepted").getValue(boolean.class)) {
                //Get relevant break information
                Date breakTime = currentBreak.child("breakTime").getValue(Date.class);
                int breakLength = currentBreak.child("breakLength").getValue(int.class);
                String teamKey = currentBreak.child("teamID").getValue(String.class);

                //Create end time to compare against break start time
                Date endBreak = new Date(breakTime.getTime() + (breakLength * ONE_MINUTE));

                Calendar now = Calendar.getInstance();

                //Check if the requested break time is between the current time and the end time
                if (now.getTimeInMillis() >= breakTime.getTime() && now.getTimeInMillis() <= endBreak.getTime()) {
                    //Update team status and return current job to queue if present
                    mRootRef.child("teams").child(teamKey).child("status").setValue("On Break");
                    if (teams.child(teamKey).hasChild("currentJob")) {
                        Job job = teams.child(teamKey).child("currentJob").getValue(Job.class);
                        mRootRef.child("jobs").push().setValue(job);
                        mRootRef.child("teams").child(teamKey).child("currentJob").removeValue();
                    }
                } else if (now.getTimeInMillis() > endBreak.getTime()) {
                    //Add team back to queue if break finished
                    mRootRef.child("teams").child(teamKey).child("status").setValue("Waiting");
                    mRootRef.child("teams").child(teamKey).child("priorityCounter").setValue(0);
                    mRootRef.child("breakRequests").child(key).removeValue();
                }
            }
        }
    }
}
