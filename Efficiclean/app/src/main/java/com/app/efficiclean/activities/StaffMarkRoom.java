package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class StaffMarkRoom extends AppCompatActivity {

    private String staffKey;
    private String hotelID;
    private String teamKey;
    private Bundle extras;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRootRef;
    private DatabaseReference mRoomRef;
    private DatabaseReference mJobRef;
    private DataSnapshot rooms;
    private RadioGroup status;
    private EditText roomNumber;
    private Button markRoom;
    private Button finishCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_markroom);

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
            staffKey = extras.getString("staffKey");
        }

        roomNumber = (EditText) findViewById(R.id.etRoomNumber);

        //Reference radio group of room statuses
        status = (RadioGroup) findViewById(R.id.rgStatus);

        //Add click listener to mark room button
        markRoom = (Button) findViewById(R.id.btMarkRoom);
        markRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = (RadioButton) findViewById(status.getCheckedRadioButtonId());
                String rNumber = roomNumber.getText().toString();
                if (!rNumber.equals("") && rb != null) {
                    addJob();
                } else {
                    Toast.makeText(StaffMarkRoom.this, "Please fill in a valid room number and select a status.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //Add click listener to finish check button
        finishCheck = (Button) findViewById(R.id.btFinishCheck);
        finishCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCheck();
            }
        });

        //Get instance of Firebase database
        mRootRef = FirebaseDatabase.getInstance().getReference(hotelID);
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teamKey = dataSnapshot.child("staff").child(staffKey).child("teamID").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRoomRef = mRootRef.child("rooms");
        mRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rooms = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mJobRef = mRootRef.child("jobs");

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
        Intent i = new Intent(StaffMarkRoom.this, StaffHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }

    public void addJob() {
        //Get values from selected Radiobutton
        RadioButton rb = (RadioButton) findViewById(status.getCheckedRadioButtonId());
        String roomStatus = rb.getText().toString();
        String rNumber = roomNumber.getText().toString();

        //Make sure guest inputted valid room number
        if (rooms.hasChild(rNumber) && rb != null) {
            Log.v("Status", rooms.child(rNumber).child("status").getValue(String.class));
            //Make sure that room has not already been marked
            if (rooms.child(rNumber).child("status").getValue(String.class).equals("Idle")) {
                if (roomStatus.equals("Do not Disturb")) {
                    mRoomRef.child(rNumber).child("status").setValue("Do Not Disturb");
                } else {
                    //Create new job and add it to database
                    Job job = new Job();
                    job.setRoomNumber(rNumber);
                    job.setPriority(0);
                    mRoomRef.child(rNumber).child("status").setValue("To Be Cleaned");
                    mJobRef.push().setValue(job);
                }

                Intent i = new Intent(StaffMarkRoom.this, StaffHome.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(StaffMarkRoom.this, "The selected room already has a service job assigned to it today.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(StaffMarkRoom.this, "This room number does not exist. Please try again.",
                    Toast.LENGTH_LONG).show();
        }

    }

    public void finishCheck() {
        mRootRef.child("teams").child(teamKey).child("status").setValue("Waiting");

        Intent i = new Intent(StaffMarkRoom.this, StaffHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }
}
