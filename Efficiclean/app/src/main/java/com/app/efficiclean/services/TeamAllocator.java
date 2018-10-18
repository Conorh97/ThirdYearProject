package com.app.efficiclean.services;

import android.os.Bundle;
import com.app.efficiclean.classes.Team;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class TeamAllocator extends JobService {

    private String hid;
    private ArrayList<String> staffKeys = new ArrayList<>();
    private ArrayList<Team> teams = new ArrayList<>();
    private DatabaseReference mRootRef;
    private DatabaseReference mStaffRef;
    private DatabaseReference mTeamRef;
    private Bundle extras;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Get parameters passed to service
        extras = jobParameters.getExtras();
        hid = extras.getString("hid");

        //Create reference to hotel in Firebase
        mRootRef = FirebaseDatabase.getInstance().getReference(hid);

        mStaffRef = mRootRef.child("staff");
        mStaffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Iterate through staff members and add their id to the ArrayList
                for (DataSnapshot hk : dataSnapshot.getChildren()) {
                    staffKeys.add(hk.getKey());
                }
                randomiseKeys();
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

    public void randomiseKeys() {
        //Randomise the order of the staff ids
        Random rand = new Random();
        Collections.shuffle(staffKeys, rand);
        createTeams();
    }

    public void createTeams() {
        /*
            This service is designed to randomly allocate housekeeping
            teams for the next working day.
         */

        //Reference to teams branch in Firebase
        mTeamRef = mRootRef.child("teams");

        //For every 2 staff members, create a team and add the team to an ArrayList
        int i;
        for (i = 0; i < staffKeys.size() - 1; i += 2) {
            Team newTeam = new Team();
            newTeam.setStatus("Waiting");
            newTeam.setCleanCounter(0);
            newTeam.setPriorityCounter(0);
            newTeam.addMember(staffKeys.get(i));
            newTeam.addMember(staffKeys.get(i + 1));
            teams.add(newTeam);
        }

        //Create a team of one if there is an odd number of staff members
        if (i < staffKeys.size()) {
            Team newTeam = new Team();
            newTeam.setStatus("Waiting");
            newTeam.setCleanCounter(0);
            newTeam.setPriorityCounter(0);
            newTeam.addMember(staffKeys.get(i));
            teams.add(newTeam);
        }

        //Push teams to firebase and update staff members values
        for (Team team : teams) {
            String key = mTeamRef.push().getKey();
            mTeamRef.child(key).setValue(team);
            for (int j = 0; j < team.getMembers().size(); j++) {
                String staffID = team.getMember(j);
                mStaffRef.child(staffID).child("teamID").setValue(key);
            }
        }
    }
}
