package com.app.efficiclean.classes;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Observable;

public class JobQueue extends Observable {

    private ArrayList<Job> jQueue = new ArrayList<Job>();
    private String hotelID;
    private DatabaseReference mJobRef;

    public JobQueue(String hid) {
        hotelID = hid;

        //Create reference to jobs in Firebase
        mJobRef = FirebaseDatabase.getInstance().getReference(hotelID).child("jobs");

        //Read in jobs ordered by priority
        mJobRef.orderByChild("priority").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Clear existing queue
                if (!jQueue.isEmpty()) {
                    jQueue = new ArrayList<Job>();
                }

                //Iterate through jobs and add jobs to queue
                for (DataSnapshot job : dataSnapshot.getChildren()) {
                    Job newJob = job.getValue(Job.class);
                    newJob.key = job.getKey();
                    jQueue.add(newJob);
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

    public Job dequeue() {
        //Remove job from queue
        Job nextJob = jQueue.remove(jQueue.size() - 1);
        mJobRef.child(nextJob.key).removeValue();
        return nextJob;
    }

    public Boolean isEmpty() {
        return jQueue.size() == 0;
    }

}
