package com.app.efficiclean.services;

import android.os.Bundle;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.*;

/**
 * Created by shaum on 20/02/18.
 */

public class ResetSystemStatus extends JobService {

    private String hid;
    private DatabaseReference mRootRef;
    private DatabaseReference mJobRef;
    private DatabaseReference mRoomRef;
    private DatabaseReference mTeamRef;
    private DatabaseReference mSuperRef;
    private DatabaseReference mBreakRef;
    private DataSnapshot rooms;
    private DataSnapshot supervisor;
    private Bundle extras;

    @Override
    public boolean onStartJob (JobParameters job) {
        /*
            This service's purpose is to reset all database values to be updated
            for the next working day.
         */

        //Get parameters passed to service
        extras = job.getExtras();
        hid = extras.getString("hid");

        //Create references to different branches in Firebase
        mRootRef = FirebaseDatabase.getInstance().getReference(hid);
        mJobRef = mRootRef.child("jobs");
        mRoomRef = mRootRef.child("rooms");
        mTeamRef = mRootRef.child("teams");
        mSuperRef = mRootRef.child("supervisor");
        mBreakRef = mRootRef.child("breakRequests");

        //Remove today's created values
        mJobRef.removeValue();
        mTeamRef.removeValue();
        mBreakRef.removeValue();

        mRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store value of rooms as datasnapshot
                rooms = dataSnapshot;
                editRooms();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSuperRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store value of supervisor as datasnapshot
                supervisor = dataSnapshot;
                editSupervisor();
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

    public void editRooms() {
        //Iterate through rooms and reset them to default values for next day
        for (DataSnapshot room : rooms.getChildren()) {
            String key = room.getKey();
            mRoomRef.child(key).child("status").setValue("Idle");
            if (room.hasChild("cleanTime")) {
                mRoomRef.child(key).child("cleanTime").removeValue();
            }
        }
    }

    public void editSupervisor() {
        //Reset supervisor approvals for next day
        for (DataSnapshot sVisor : supervisor.getChildren()) {
            String key = sVisor.getKey();
            mSuperRef.child(key).child("approvals").removeValue();
        }
    }
}
