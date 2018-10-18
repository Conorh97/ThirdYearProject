package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Housekeeper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;

public class ReportAbsences extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String hotelID;
    private Bundle extras;
    private String supervisorKey;
    private DatabaseReference mStaffRef;
    private DataSnapshot staff;
    private RadioGroup names;
    private HashMap<String, String> keys;
    private Button markAbsent;
    private String teamID;
    private DatabaseReference mTeamRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_report_absence);

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

        //Reference radio group of staff members
        names = (RadioGroup) findViewById(R.id.rgStaffList);

        //Add click listener to submit button
        markAbsent = (Button) findViewById(R.id.btReportAbsenceSubmit);
        markAbsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if supervisor selected a staff member
                if (names.getCheckedRadioButtonId() != 0) {
                    reportAbsence();
                } else {
                    Toast.makeText(ReportAbsences.this, "Please select a staff member to mark absent.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //Reference to staff branch in database
        mStaffRef = FirebaseDatabase.getInstance().getReference(hotelID).child("staff");
        mStaffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store datasnapshot and populate radio group
                staff = dataSnapshot;
                populateGroup();
                keys = populateHashMap();
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
        Intent i = new Intent(ReportAbsences.this, SupervisorHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }

    public void populateGroup(){
        //Remove placeholders
        names.removeAllViews();

        //Iterate through all staff members
        for (DataSnapshot ds : staff.getChildren()) {
            //Get relevant staff information
            Housekeeper hs = ds.getValue(Housekeeper.class);
            String username = hs.getUsername();

            //Ensure that staff member is not absent
            if (hs.getTeamID().equals("Absent") == false) {
                //Add new radio button displaying staff member's name
                RadioButton r1 = new RadioButton(this);
                r1.setText(username);
                r1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                r1.setBackgroundResource(R.drawable.cell_border);
                r1.setLayoutParams(names.getLayoutParams());
                names.addView(r1);
            }
        }
    }

    public HashMap<String, String> populateHashMap(){
        /*
            Used to create HashMap so that there is a clear link back to the database.
            The key in this HashMap is the username rather than the staff member's id.
            This is so we can display the username in the radio button, and extract the unique staff id
            by just getting the text of the radio button.
        */

        HashMap<String, String> map = new HashMap<String, String>();

        //Iterate through staff members
        for(DataSnapshot ds : staff.getChildren()){
            //Add current staff member's values to HashMap
            String key = ds.getKey();
            String username = ds.child("username").getValue(String.class);
            map.put(username, key);
        }
        return map;
    }

    public void reportAbsence() {
        //Reference radio button that is checked
        RadioButton rb = (RadioButton) findViewById(names.getCheckedRadioButtonId());

        //Check if a radio button is selected
        if (rb != null) {
            //Get housekeeper name and extract id from HashMap
            String uname = rb.getText().toString();
            final String key = keys.get(uname);

            //Add listener to relevant staff member
            mStaffRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Save team id and update housekeeper status
                    teamID = dataSnapshot.child("teamID").getValue(String.class);
                    mStaffRef.child(key).child("teamID").setValue("Absent");
                    removeFromTeam(key);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void removeFromTeam(final String key) {
        //Reference to members of the housekeeper's team in database
        mTeamRef = FirebaseDatabase.getInstance().getReference(hotelID).child("teams").child(teamID).child("members");
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Iterate through members
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Remove staff key if current value matches housekeeper
                    if (ds.getValue(String.class).equals(key)) {
                        mTeamRef.child(ds.getKey()).removeValue();
                        removeTeam(key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeTeam(final String key) {
        mTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Remove team if there is no members anymore
                if (dataSnapshot.exists() == false) {
                    FirebaseDatabase.getInstance().getReference(hotelID).child("teams").child(teamID).removeValue();
                }

                Intent i = new Intent(ReportAbsences.this, SupervisorHome.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

