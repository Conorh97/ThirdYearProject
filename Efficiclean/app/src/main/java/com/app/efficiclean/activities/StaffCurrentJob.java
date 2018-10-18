package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Approval;
import com.app.efficiclean.classes.Job;
import com.app.efficiclean.classes.Team;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class StaffCurrentJob extends AppCompatActivity {

    private Button btMarkClean;
    private Button btReportHazard;
    private Button btReportSevereMess;
    private DatabaseReference mStaffRef;
    private DatabaseReference mTeamRef;
    private DatabaseReference mSupervisorRef;
    private Team team;
    private String staffKey;
    private String teamKey;
    private String hotelID;
    private Bundle extras;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String supervisorKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_staff_current_room);

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

        mStaffRef = FirebaseDatabase.getInstance().getReference(hotelID).child("staff").child(staffKey);
        mStaffRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teamKey = dataSnapshot.child("teamID").getValue(String.class);
                getTeam();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Reference to supervisor values in database
        mSupervisorRef = FirebaseDatabase.getInstance().getReference(hotelID).child("supervisor");
        mSupervisorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("onDuty").getValue(Boolean.class) == true) {
                        supervisorKey = ds.getKey();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btMarkClean = (Button) findViewById(com.app.efficiclean.R.id.btMarkClean);
        btMarkClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignToSupervisor();
            }
        });

        //Add listener to submit button
        btReportHazard = (Button) findViewById(com.app.efficiclean.R.id.btReportHazard);
        btReportHazard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StaffCurrentJob.this, ReportHazard.class);
                i.putExtras(extras);
                i.putExtra("teamKey", teamKey);
                i.putExtra("supervisorKey", supervisorKey);
                startActivity(i);
                finish();
            }
        });

        btReportSevereMess = (Button) findViewById(R.id.btReportSevereMess);
        btReportSevereMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StaffCurrentJob.this, ReportSevereMess.class);
                i.putExtras(extras);
                i.putExtra("teamKey", teamKey);
                i.putExtra("supervisorKey", supervisorKey);
                startActivity(i);
                finish();
            }
        });

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
        Intent i = new Intent(StaffCurrentJob.this, StaffHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }
    
    public void getTeam() {
        //Make reference to team branch in Firebase
        mTeamRef = FirebaseDatabase.getInstance().getReference(hotelID).child("teams").child(teamKey);
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                team = dataSnapshot.getValue(Team.class);

                TextView tvRoom = (TextView) findViewById(com.app.efficiclean.R.id.tvCurrentRoom);
                String roomText;

                if (team.getCurrentJob() == null) {
                    roomText = "You have no current room.";
                    btMarkClean.setVisibility(View.GONE);
                    btReportHazard.setVisibility(View.GONE);
                    btReportSevereMess.setVisibility(View.GONE);
                } else if (team.getCurrentJob().getDescription() == null){
                    roomText = "Your current room is: " + team.getCurrentJob().getRoomNumber();
                } else {
                    roomText = "Room " + team.getCurrentJob().getRoomNumber() + " feedback: " + team.getCurrentJob().getDescription();
                }

                tvRoom.setText(roomText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void assignToSupervisor() {
        //Get job of team
        Job job = team.getCurrentJob();

        //Create new Approval and set values
        Approval approval = new Approval();
        approval.setJob(job);
        approval.setCreatedBy(team.getKey());

        //Send approval to the supervisor and update values in the database
        DatabaseReference mRoomRef = FirebaseDatabase.getInstance().getReference(hotelID).child("rooms");
        mRoomRef.child(job.getRoomNumber()).child("status").setValue("Waiting");
        mSupervisorRef.child(supervisorKey).child("approvals").push().setValue(approval);
        if (team.getStatus().equals("Checking Rooms") == false) {
            mTeamRef.child("status").setValue("Waiting");
        }
        mTeamRef.child("currentJob").removeValue();

        //Display popup to user
        Toast.makeText(StaffCurrentJob.this, "This room has been marked clean and an approval request has been sent to the supervisor.",
                Toast.LENGTH_LONG).show();
        Intent i = new Intent(StaffCurrentJob.this, StaffHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }
}
