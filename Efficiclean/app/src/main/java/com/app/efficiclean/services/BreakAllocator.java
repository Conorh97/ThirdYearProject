package com.app.efficiclean.services;

import android.os.Bundle;
import com.app.efficiclean.classes.Break;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.Date;

public class BreakAllocator extends JobService {

    private final int ONE_MINUTE = 60000;
    private Bundle extras;
    private String hid;
    private DatabaseReference mRootRef;
    private DatabaseReference mBreakRef;
    private DatabaseReference mTeamRef;
    private DataSnapshot teams;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Get parameters passed to service
        extras = jobParameters.getExtras();
        hid = extras.getString("hid");

        //Create references to different branches in Firebase
        mRootRef = FirebaseDatabase.getInstance().getReference(hid);
        mBreakRef = mRootRef.child("breakRequests");
        mTeamRef = mRootRef.child("teams");

        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store teams datasnapshot
                teams = dataSnapshot.child("teams");
                allocateBreaks();
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

    public void allocateBreaks() {
        /*
            Purpose of this service is to automatically allocate breaks to teams
            at 12:30 with any remaining minutes they have for the day. These breaks will be
            added sequentially one after the other.
         */

        //Create first break start time
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 12);
        now.set(Calendar.MINUTE, 30);
        now.set(Calendar.SECOND, 0);

        //Create Date instance of current time
        Date time = new Date(now.getTimeInMillis());

        //Iterate through teams
        for (DataSnapshot team : teams.getChildren()) {
            //Create variables with relevant values
            String key = team.getKey();
            int breakRemaining = team.child("breakRemaining").getValue(int.class);

            //Check if team has any break minutes remaining
            if (breakRemaining > 0) {
                //Create new Break and set values
                Break newBreak = new Break();
                newBreak.setBreakLength(breakRemaining);
                newBreak.setBreakTimeDate(time);
                newBreak.setTeamID(key);
                newBreak.setAccepted(true);

                //Update time value to be at the end of current break
                time = new Date(time.getTime() + (breakRemaining * ONE_MINUTE));

                //Push values to Firebase
                mBreakRef.push().setValue(newBreak);
                mTeamRef.child(key).child("breakRemaining").setValue(0);
            }
        }
    }
}
