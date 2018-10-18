package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.NotificationHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class TeamBreakApproval extends AppCompatActivity {

    private Bundle extras;
    private String hotelID;
    private String teamID;
    private String breakKey;
    private String membersText;
    private String timeText;
    private int breakLength;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRootRef;
    private DataSnapshot breaks;
    private DataSnapshot teams;
    private CheckBox approve;
    private CheckBox disapprove;
    private EditText reason;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_approve_team_break);

        //Display back button in navbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Extract variables from intent bundle
        extras = getIntent().getExtras();
        if (extras != null) {
            hotelID = extras.getString("hotelID");
            teamID = extras.getString("teamID");
            breakKey = extras.getString("breakKey");
            membersText = extras.getString("membersText");
            timeText = extras.getString("timeText");
            breakLength = extras.getInt("breakLength");
        }

        //Set screen orientation based on layout
        if(getResources().getBoolean(R.bool.landscape_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        TextView header = (TextView) findViewById(R.id.tvTeam);
        header.setText("Team: " + membersText);

        TextView time = (TextView) findViewById(R.id.tvTime);
        time.setText("Time: " + timeText);

        TextView length = (TextView) findViewById(R.id.tvLength);
        length.setText("Length: " + breakLength + " minutes");

        //Reference display file check boxes
        approve = (CheckBox) findViewById(R.id.cbApprove);
        disapprove = (CheckBox) findViewById(R.id.cbDisapprove);

        //Add toggle functionality to block both check boxes being selected
        approve.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (approve.isChecked()) {
                    disapprove.setChecked(false);
                }
            }
        });

        disapprove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (disapprove.isChecked()) {
                    approve.setChecked(false);
                }
            }
        });

        //Reference other elements from activity layouts
        reason = (EditText) findViewById(R.id.etReason);

        submit = (Button) findViewById(R.id.btTeamBreakSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (approve.isChecked()) {
                    approvedSubmit();
                } else if (disapprove.isChecked()) {
                    disapprovedSubmit();
                } else {
                    Toast.makeText(TeamBreakApproval.this, "You haven't selected an option. Please check one of the boxes and try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //Reference to root of current hotel
        mRootRef = FirebaseDatabase.getInstance().getReference(hotelID);
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teams = dataSnapshot.child("teams");
                breaks = dataSnapshot.child("breakRequests");
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
        Intent i = new Intent(TeamBreakApproval.this, BreakApproval.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }

    public void approvedSubmit() {
        //Get Firebase values for team
        int breakRemaining = teams.child(teamID).child("breakRemaining").getValue(int.class);

        if (breakRemaining >= breakLength) {
            mRootRef.child("breakRequests").child(breakKey).child("accepted").setValue(true);
            for (DataSnapshot member : teams.child(teamID).child("members").getChildren()) {
                String staffKey = member.getValue(String.class);
                //Send notification using OneSignal to all team members
                NotificationHandler.sendNotification(hotelID,
                        staffKey,
                        "Your requested break starting at " +
                                timeText +
                                "has been accepted.");
            }
            int newBreakRemaining = breakRemaining - breakLength;
            mRootRef.child("teams").child(teamID).child("breakRemaining").setValue(newBreakRemaining);

            Intent i = new Intent(TeamBreakApproval.this, SupervisorHome.class);
            i.putExtras(extras);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(TeamBreakApproval.this, "This team does not have enough minutes left for their break today.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void disapprovedSubmit() {
        //Get Firebase values for team
        mRootRef.child("breakRequests").child(breakKey).removeValue();
        for (DataSnapshot member : teams.child(teamID).child("members").getChildren()) {
            String staffKey = member.getValue(String.class);
            //Send notification using OneSignal to all team members
            NotificationHandler.sendNotification(hotelID,
                    staffKey,
                    "Your requested break starting at " +
                            timeText +
                            "has been denied. Reason: " +
                            reason.getText().toString());
        }
        //Return to SupervisorHome
        Intent i = new Intent(TeamBreakApproval.this, SupervisorHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }
}

