package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Break;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class StaffRequestBreak extends AppCompatActivity {

    private String staffKey;
    private String hotelID;
    private Bundle extras;
    private TextView header;
    private EditText breakTime;
    private RadioGroup breakOptions;
    private Button requestBreak;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRootRef;
    private DatabaseReference mStaffRef;
    private DatabaseReference mTeamRef;
    private DataSnapshot staff;
    private String teamID;
    private String requestedTime;
    private int breakRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_request_break);
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

        //Reference to page layout elements
        header = (TextView) findViewById(R.id.tvTeam);
        breakTime = (EditText) findViewById(R.id.etTime);
        breakOptions = (RadioGroup) findViewById(R.id.rgBreakLength);

        requestBreak = (Button) findViewById(R.id.btRequestBreakSubmit);
        requestBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Make sure that the user has selected a break length
                if (breakOptions.getCheckedRadioButtonId() != 0) {
                    requestToSupervisor();
                } else {
                    Toast.makeText(StaffRequestBreak.this, "Please select the length of your break.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //Make references to staff values in database
        mRootRef = FirebaseDatabase.getInstance().getReference(hotelID);
        mStaffRef = mRootRef.child("staff");
        mStaffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store staff datasnapshot and get relevant teamID
                staff = dataSnapshot;
                teamID = staff.child(staffKey).child("teamID").getValue(String.class);
                getTeam();
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
        Intent i = new Intent(StaffRequestBreak.this, StaffHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }

    public void getTeam() {
        //Reference user's housekeeping team in database
        mTeamRef = mRootRef.child("teams").child(teamID);
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get break remaining for the text and format string to be displayed
                breakRemaining = dataSnapshot.child("breakRemaining").getValue(int.class);
                String text = "Team: ";
                for (DataSnapshot ds : dataSnapshot.child("members").getChildren()) {
                    String hk = ds.getValue(String.class);
                    if (text.equals("Team: ")) {
                        text += staff.child(hk).child("username").getValue(String.class);
                    } else {
                        text += " & " + staff.child(hk).child("username").getValue(String.class);
                    }
                }
                header.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void requestToSupervisor() {
        //Get user's selected break length and break time
        RadioButton selectedTime = (RadioButton) findViewById(breakOptions.getCheckedRadioButtonId());
        requestedTime = breakTime.getText().toString();

        //Check that option is selected and time is in correct format
        if (requestedTime.length() == 4 && selectedTime != null) {
            //Make sure time is before cut-off point for requesting breaks
            if (Integer.parseInt(requestedTime.substring(0, 2)) <= 16
                    && Integer.parseInt(requestedTime.substring(2, 4)) <= 59) {
                //Create new break and set values
                Break breakRequest = new Break();
                breakRequest.setBreakLength(Integer.parseInt(selectedTime.getText().toString().substring(0, 2)));
                breakRequest.setBreakTime(requestedTime);
                breakRequest.setTeamID(teamID);

                //Make sure that team has enough minutes left to request a break of that length
                if (breakRequest.getBreakLength() <= breakRemaining) {
                    //Create request and push to Firebase
                    DatabaseReference mBreakRef = mRootRef.child("breakRequests");
                    mBreakRef.push().setValue(breakRequest);

                    Intent i = new Intent(StaffRequestBreak.this, StaffHome.class);
                    i.putExtras(extras);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(StaffRequestBreak.this, "The break requested exceeds your remaining amount for the day. You have " +
                                    breakRemaining + " minutes remaining.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(StaffRequestBreak.this, "Please input a valid time before 17:00.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(StaffRequestBreak.this, "Please use the correct time format and select a break length.",
                    Toast.LENGTH_LONG).show();
        }
    }
}

