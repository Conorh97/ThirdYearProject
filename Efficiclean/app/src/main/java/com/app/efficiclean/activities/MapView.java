package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.app.efficiclean.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

import java.util.HashMap;

public class MapView extends AppCompatActivity {

    private HashMap<String, String> rooms;
    private DatabaseReference mRoomRef;
    private String hotelID;
    private String jobTitle;
    private Bundle extras;
    private VectorMasterView map;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_map_view);

        //Display back button in navbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Set screen orientation based on layout
        if(getResources().getBoolean(R.bool.landscape_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //Extract variables from intent bundle
        extras = getIntent().getExtras();
        if (extras != null) {
            hotelID = extras.getString("hotelID");
            jobTitle = extras.getString("jobTitle");
        }

        //Create reference to our scalable vector layout of the hotel rooms
        map = (VectorMasterView) findViewById(R.id.vmMap);

        //Create reference to hotel rooms in the Firebase database
        mRoomRef = FirebaseDatabase.getInstance().getReference(hotelID).child("rooms");
        mRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //HashMap to store rooms with the room number as key and current status as value
                rooms = new HashMap<String, String>();

                //Iterate through all rooms in datasnapshot
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Get relevant data and push to HashMap
                    String roomNumber = ds.getKey();
                    String status = ds.child("status").getValue(String.class);
                    if (roomNumber != null && status != null) {
                        rooms.put(roomNumber, status);
                    }
                }
                changeRoomColours();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Create Firebase authenticator
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth fbAuth) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Add authentication listener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        //Start relevant activity based on whether a housekeeper or supervisor viewed the map
        if (jobTitle.equals("housekeeper")) {
            Intent i = new Intent(MapView.this, StaffHome.class);
            i.putExtras(extras);
            startActivity(i);
            finish();
        } else if (jobTitle.equals("supervisor")) {
            Intent i = new Intent(MapView.this, SupervisorHome.class);
            i.putExtras(extras);
            startActivity(i);
            finish();
        }
    }

    public void changeRoomColours() {
        //Iterate through all rooms in the HashMap
        for (String roomNumber : rooms.keySet()) {
            //Get room id used in vector path display
            String status = rooms.get(roomNumber);
            String pathName = "pRoom" + roomNumber;

            //Call VectorMasterView method to access that specific room in the vector graphics
            PathModel room = map.getPathModelByName(pathName);

            //Change path fill colour based on room status
            if (status.equals("Waiting")) {
                room.setFillColor(Color.parseColor("#1589FF"));
            } else if (status.equals("Do Not Disturb")) {
                room.setFillColor(Color.parseColor("#000000"));
            } else if (status.equals("To Be Cleaned")) {
                room.setFillColor(Color.parseColor("#800000"));
            } else if (status.equals("Completed")) {
                room.setFillColor(Color.parseColor("#008000"));
            } else if (status.equals("In Process")) {
                room.setFillColor(Color.parseColor("#ff6600"));
            } else {
                room.setFillColor(Color.parseColor("#ffeeaa"));
            }
        }
        map.update();
    }
}
