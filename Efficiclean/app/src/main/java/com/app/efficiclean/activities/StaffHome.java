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

public class StaffHome extends AppCompatActivity {

    public Button btRequestBreak;
    public Button btCurrentJob;
    public Button btViewMap;
    public String staffKey;
    public String hotelID;
    public Bundle extras;
    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;
    public DatabaseReference mTeamRef;
    public DatabaseReference mJobRef;
    public DatabaseReference mStaffRef;
    private DataSnapshot staff;
    private DataSnapshot jobs;
    private DataSnapshot teams;
    private TableLayout tb1;
    private TableLayout tb2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_staff_home);

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

        //Get TableLayouts from layout file
        tb1 = (TableLayout) findViewById(com.app.efficiclean.R.id.tbTeams);
        tb2 = (TableLayout) findViewById(com.app.efficiclean.R.id.tbQueue);

        btViewMap = (Button) findViewById(com.app.efficiclean.R.id.btViewMap);
        btViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StaffHome.this, MapView.class);
                extras.putString("jobTitle", "housekeeper");
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        btRequestBreak = (Button) findViewById(com.app.efficiclean.R.id.btRequestBreak);
        btRequestBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StaffHome.this, StaffRequestBreak.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            }
        });

        btCurrentJob = (Button) findViewById(com.app.efficiclean.R.id.btCurrentJob);
        btCurrentJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get team status and key from the database
                String teamKey = staff.child(staffKey).child("teamID").getValue(String.class);
                String teamStatus = teams.child(teamKey).child("status").getValue(String.class);

                //Set intent to correct activity based on what job is currently assigned to housekeeping team
                Intent i;
                if (teamStatus.equals("Checking Rooms") && staff.child(staffKey).hasChild("currentJob") == false) {
                    i = new Intent(StaffHome.this, StaffMarkRoom.class);
                } else {
                    i = new Intent(StaffHome.this, StaffCurrentJob.class);
                }

                i.putExtras(extras);
                startActivity(i);
                finish();
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

        //Reference to staff details in database
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

        //Reference to jobs in database
        mJobRef = FirebaseDatabase.getInstance().getReference(hotelID).child("jobs");
        mJobRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                jobs = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public void todaysTeamsButtonClick(View v) {
        Intent i = new Intent(StaffHome.this, TodaysTeams.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }

    public void getTeams() {
        //Reference to teams value in database
        mTeamRef = FirebaseDatabase.getInstance().getReference(hotelID).child("teams");
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Only populate one TableLayout for mobile, two for tablet
                teams = dataSnapshot;
                setQueue();
                if (tb1 != null) {
                    setTeams();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setTeams() {
        TextView template = (TextView) findViewById(com.app.efficiclean.R.id.tvTeamsRow1);

        //Remove all TableRows except heading
        tb1.removeViews(1, tb1.getChildCount() - 1);
        for(DataSnapshot ds : teams.getChildren()) {
            //Get team values and create new table row
            Team team = ds.getValue(Team.class);

            //Make sure team has members
            if (ds.hasChild("members")) {
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
                    tb1.addView(tr);
                }
            }
        }
    }

    public void setQueue() {
        TextView template = (TextView) findViewById(R.id.tvQueueRow1);

        //Remove all TableRows except heading
        tb2.removeViews(1, tb2.getChildCount() - 1);
        for(DataSnapshot ds : teams.getChildren()) {
            //Get team values and create new table row
            Team team = ds.getValue(Team.class);

            //Make sure team has members and is available
            if (team.getStatus().equals("Waiting") && ds.hasChild("members")) {
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
                    tb2.addView(tr);
                }
            }
        }
    }
}
