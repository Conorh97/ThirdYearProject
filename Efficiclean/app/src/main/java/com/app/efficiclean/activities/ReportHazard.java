package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.HazardApproval;
import com.app.efficiclean.classes.Job;
import com.app.efficiclean.classes.Team;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ReportHazard extends AppCompatActivity {

    private EditText description;
    private Button reportHazard;
    private DatabaseReference mTeamRef;
    private DatabaseReference mSupervisorRef;
    private Team team;
    private String staffKey;
    private String hotelID;
    private String teamKey;
    private String supervisorKey;
    private Bundle extras;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_staff_report_hazard);

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
            teamKey = extras.getString("teamKey");
            supervisorKey = extras.getString("supervisorKey");
        }

        description = (EditText) findViewById(com.app.efficiclean.R.id.etDescription);

        //Add listener to submit button
        reportHazard = (Button) findViewById(R.id.btHazardSubmit);
        reportHazard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignToSupervisor();
            }
        });

        //Make reference to team branch in Firebase
        mTeamRef = FirebaseDatabase.getInstance().getReference(hotelID).child("teams").child(teamKey);
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store datasnapshot of teams
                team = dataSnapshot.getValue(Team.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Reference to supervisor values in database
        mSupervisorRef = FirebaseDatabase.getInstance().getReference(hotelID).child("supervisor");

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
        Intent i = new Intent(ReportHazard.this, StaffHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }

    public void assignToSupervisor() {
        //Get job of team
        Job job = team.getCurrentJob();

        //Create new HazardApproval and set values
        HazardApproval approval = new HazardApproval();
        approval.setJob(job);
        approval.setCreatedBy(teamKey);
        approval.setDescription(description.getText().toString());

        //Send approval to the supervisor and update values in the database
        DatabaseReference mRoomRef = FirebaseDatabase.getInstance().getReference(hotelID).child("rooms");
        mRoomRef.child(job.getRoomNumber()).child("status").setValue("Waiting");
        mSupervisorRef.child(supervisorKey).child("approvals").push().setValue(approval);
        if (team.getStatus().equals("Checking Rooms") == false) {
            mTeamRef.child("status").setValue("Waiting");
        }
        mTeamRef.child("currentJob").removeValue();

        //Display popup to user
        Toast.makeText(ReportHazard.this, "This room has been marked hazardous and an approval request has been sent to the supervisor.",
                Toast.LENGTH_LONG).show();
        Intent i = new Intent(ReportHazard.this, StaffHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }
}

