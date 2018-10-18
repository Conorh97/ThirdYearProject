package com.app.efficiclean.classes;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Observable;

public class TeamQueue extends Observable {

    private ArrayList<Team> tQueue = new ArrayList<Team>();
    private String hotelID;
    private DatabaseReference mTeamRef;

    public TeamQueue(String hid) {
        hotelID = hid;

        //Create reference to teams in Firebase
        mTeamRef = FirebaseDatabase.getInstance().getReference(hotelID).child("teams");

        //Read in teams ordered by priority
        mTeamRef.orderByChild("priorityCounter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Clear existing queue
                if (!tQueue.isEmpty()) {
                    clearQueue();
                }

                //Iterate through teams and add available teams to queue
                for (DataSnapshot team : dataSnapshot.getChildren()) {
                    if (team.hasChild("status") && team.child("status").getValue().equals("Waiting")) {
                        Team newTeam = team.getValue(Team.class);
                        newTeam.key = team.getKey();
                        checkReturned(newTeam);
                    }
                }

                //Notify observer that a change has occurred
                setChanged();
                notifyObservers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Team dequeue() {
        //Remove team and update their status in the database
        Team nextTeam = tQueue.remove(tQueue.size() - 1);
        nextTeam.setStatus("Cleaning");
        mTeamRef.child(nextTeam.key).setValue(nextTeam);
        return nextTeam;
    }

    public Boolean isEmpty() {
        return tQueue.size() == 0;
    }

    public void checkReturned(Team newTeam) {
        //Mark returned job as current if not current job for team
        if (!newTeam.getStatus().equals("Checking Rooms") && newTeam.getCurrentJob() == null && newTeam.getReturnedJob() != null) {
            mTeamRef.child(newTeam.key).child("currentJob").setValue(newTeam.getReturnedJob());
            mTeamRef.child(newTeam.key).child("status").setValue("In Progress");
            mTeamRef.child(newTeam.key).child("returnedJob").removeValue();
        } else {
            tQueue.add(newTeam);
        }
    }

    public void clearQueue() {
        tQueue = new ArrayList<Team>();
    }
}
