package com.app.efficiclean.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Team;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class SupervisorHome extends AppCompatActivity {

    private String supervisorKey;
    private DataSnapshot staff;
    private DataSnapshot teams;
    private DatabaseReference mStaffRef;
    private DatabaseReference mTeamRef;
    private Button btViewMap;
    private Button hazardApproval;
    private Button serviceApproval;
    private Button breakApproval;
    private Button approveSevereMess;
    private Button reportAbsences;
    private String hotelID;
    private Bundle extras;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_supervisor_home);

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
            supervisorKey = extras.getString("staffKey");
        }

        btViewMap = (Button) findViewById(com.app.efficiclean.R.id.btViewMap);
        btViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SupervisorHome.this, MapView.class);
                extras.putString("jobTitle", "supervisor");
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        hazardApproval = (Button) findViewById(com.app.efficiclean.R.id.btHazardApproval);
        hazardApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SupervisorHome.this, HazardApprovalsList.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        serviceApproval = (Button) findViewById(com.app.efficiclean.R.id.btServiceApproval);
        serviceApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SupervisorHome.this, SupervisorApprovals.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        breakApproval = (Button) findViewById(com.app.efficiclean.R.id.btApproveBreak);
        breakApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SupervisorHome.this, BreakApproval.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        approveSevereMess = (Button) findViewById(com.app.efficiclean.R.id.btSevereMessApproval);
        approveSevereMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SupervisorHome.this, SevereMessApprovalsList.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        reportAbsences = (Button) findViewById(com.app.efficiclean.R.id.btReportAbsence);
        reportAbsences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SupervisorHome.this, ReportAbsences.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        //Reference staff values in database
        mStaffRef = FirebaseDatabase.getInstance().getReference(hotelID).child("staff");
        mStaffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                staff = dataSnapshot;
                getTeams();
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
        //Display confirmation popup when guest goes to sign out
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Are you sure you want to sign out?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
        return true;
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }

    public void getTeams() {
        //Reference to teams value in database
        mTeamRef = FirebaseDatabase.getInstance().getReference(hotelID).child("teams");
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teams = dataSnapshot;
                setRoomApprovals();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setRoomApprovals(){
        //Reference TableLayout and template for dynamic TextView
        TableLayout table = (TableLayout) findViewById(com.app.efficiclean.R.id.tbTeamProgress);
        TextView template = (TextView) findViewById(R.id.tvRow1);

        //Remove all TableRows except heading
        table.removeViews(1, table.getChildCount() - 1);

        //Iterate through teams
        for(DataSnapshot ds : teams.getChildren()) {
            //Get team values and create new table row
            Team team = ds.getValue(Team.class);
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            String text = "";

            //Generate text to be displayed
            for (String staffKey : team.getMembers()) {
                if (staffKey != null) {
                    if (text.equals("")) {
                        text += staff.child(staffKey).child("username").getValue(String.class);
                    } else {
                        text += " & " + staff.child(staffKey).child("username").getValue(String.class);
                    }
                }
            }

            //Check to make sure that there is text to be displayed
            if (text.equals("") == false) {
                text = text + " : " + Integer.toString(team.getCleanCounter());

                if (template != null) {
                    //Create new TableRow and add it to the TableLayout
                    TextView roomNumber = new TextView(this);
                    roomNumber.setText(text);
                    roomNumber.setTextSize(template.getTextSize() / 2);
                    roomNumber.setWidth(template.getWidth());
                    roomNumber.setMinHeight(template.getHeight());
                    roomNumber.setPadding(
                            template.getPaddingLeft(),
                            template.getPaddingTop() - 5,
                            template.getPaddingRight(),
                            template.getPaddingBottom());
                    roomNumber.setBackground(template.getBackground());
                    roomNumber.setGravity(template.getGravity());

                    tr.addView(roomNumber);
                    table.addView(tr);
                }
            }
        }
    }

}


