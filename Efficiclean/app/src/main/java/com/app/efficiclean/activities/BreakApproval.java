package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Break;
import com.app.efficiclean.classes.Supervisor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class BreakApproval extends AppCompatActivity {

    private String supervisorKey;
    private String hotelID;
    private Bundle extras;
    private Supervisor supervisor;
    private DatabaseReference mSuperRef;
    private DatabaseReference mRootRef;
    private DataSnapshot staff;
    private DataSnapshot breaks;
    private DataSnapshot teams;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_supervisor_approve_break);

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

        //Create reference to Firebase database
        mRootRef = FirebaseDatabase.getInstance().getReference(hotelID);
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store datasnapshots of staff, team and break branches in Firebase
                staff = dataSnapshot.child("staff");
                teams = dataSnapshot.child("teams");
                breaks = dataSnapshot.child("breakRequests");
                setBreakRequests();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Create reference to supervisor in Firebase
        mSuperRef = mRootRef.child("supervisor").child(supervisorKey);
        mSuperRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                supervisor = dataSnapshot.getValue(Supervisor.class);
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

    public void setBreakRequests(){
        //Reference TableLayout and template for dynamic TextView
        TableLayout table = (TableLayout) findViewById(com.app.efficiclean.R.id.tbTeams);
        TextView template = (TextView) findViewById(R.id.tvTeamsTitle);

        //Remove all TableRows except heading
        table.removeViews(1, table.getChildCount() - 1);

        //Iterate through current breaks to be approved
        for(final DataSnapshot b : breaks.getChildren()) {

            //Check if break is already approved
            boolean accepted = b.child("accepted").getValue(boolean.class);
            if (!accepted) {
                //Get values from current break
                final String teamID = b.child("teamID").getValue(String.class);
                final int breakLength = b.child("breakLength").getValue(int.class);
                int hour = b.child("breakTime").child("hours").getValue(int.class);
                int minute = b.child("breakTime").child("minutes").getValue(int.class);


                //Ensure values are in correct format to be passed to Break class
                String hString;
                String mString;

                if (hour < 10) {
                    hString = "0" + hour;
                } else {
                    hString = "" + hour;
                }

                if (minute < 10) {
                    mString = "0" + minute;
                } else {
                    mString = "" + minute;
                }

                String time = hString + mString;

                //Create Break class and set values
                Break currentBreak = new Break();
                currentBreak.setTeamID(teamID);
                currentBreak.setBreakTime(time);
                currentBreak.setBreakLength(breakLength);

                try {
                    //Reference the correct team members
                    DataSnapshot members = teams.child(teamID).child("members");

                    //Ensure values are in correct format to be displayed
                    String hourString = Integer.toString(currentBreak.getBreakTime().getHours());
                    if (hourString.length() == 1) {
                        hourString = "0" + hourString;
                    }

                    String minuteString = Integer.toString(currentBreak.getBreakTime().getMinutes());
                    if (minuteString.length() == 1) {
                        minuteString = "0" + minuteString;
                    }

                    String membersText = "";
                    String timeText = hourString + ":" + minuteString;

                    //Iterate through members and set correct team title text
                    for (DataSnapshot ds : members.getChildren()) {
                        String staffKey = ds.getValue(String.class);
                        if (membersText.equals("")) {
                            membersText += staff.child(staffKey).child("username").getValue(String.class);
                        } else {
                            membersText += " & " + staff.child(staffKey).child("username").getValue(String.class);
                            timeText += "\n";
                        }
                    }

                    //Create new TableRow to be added
                    TableRow tr = new TableRow(this);
                    tr.setLayoutParams(new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    //Create TextView for team names based on template
                    TextView teamNames = new TextView(this);
                    teamNames.setText(membersText);
                    teamNames.setTextSize(template.getTextSize() / 2);
                    teamNames.setWidth(template.getWidth());
                    teamNames.setMinHeight(template.getHeight());
                    teamNames.setPadding(
                            template.getPaddingLeft(),
                            template.getPaddingTop() - 5,
                            template.getPaddingRight(),
                            template.getPaddingBottom());
                    teamNames.setBackground(template.getBackground());
                    teamNames.setGravity(template.getGravity());

                    //Create TextView for requested time based on template
                    TextView status = new TextView(this);
                    status.setText(timeText);
                    status.setTextSize(template.getTextSize() / 2);
                    status.setWidth(template.getWidth());
                    status.setMinHeight(teamNames.getHeight());
                    status.setPadding(
                            template.getPaddingLeft(),
                            template.getPaddingTop() - 5,
                            template.getPaddingRight(),
                            template.getPaddingBottom() - 2);
                    status.setBackground(template.getBackground());
                    status.setGravity(template.getGravity());

                    final String mText = membersText;
                    final String tText = timeText;

                    //Add click listener for table row
                    tr.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Create intent and add variables to bundle for next activity
                            Intent i = new Intent(BreakApproval.this, TeamBreakApproval.class);
                            extras.putString("teamID", teamID);
                            extras.putString("breakKey", b.getKey());
                            extras.putString("membersText", mText);
                            extras.putString("timeText", tText);
                            extras.putInt("breakLength", breakLength);
                            i.putExtras(extras);
                            startActivity(i);
                            finish();
                        }
                    });

                    //Add tables to TableLayout
                    tr.addView(teamNames);
                    tr.addView(status);
                    table.addView(tr);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(BreakApproval.this, SupervisorHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }
}
