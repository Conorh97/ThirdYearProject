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
import com.app.efficiclean.classes.Approval;
import com.app.efficiclean.classes.Supervisor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class HazardApprovalsList extends AppCompatActivity {

    private String supervisorKey;
    private String hotelID;
    private Bundle extras;
    private Supervisor supervisor;
    private DatabaseReference mSuperRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_supervisor_list_hazards);

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

        //Reference to supervisor in Firebase database
        mSuperRef = FirebaseDatabase.getInstance().getReference(hotelID).child("supervisor").child(supervisorKey);
        mSuperRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store datasnapshot and call function to display approval requests
                supervisor = dataSnapshot.getValue(Supervisor.class);
                setRoomApprovals();
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

    public void setRoomApprovals(){
        //Reference TableLayout and template for dynamic TextView
        TableLayout table = (TableLayout) findViewById(com.app.efficiclean.R.id.tbHazardsToBeApproved);
        TextView template = (TextView) findViewById(R.id.tvRow1);

        //Remove all TableRows except heading
        table.removeViews(1, table.getChildCount() - 1);

        //Iterate through current breaks to be approved
        for(final String key : supervisor.approvals.keySet()) {
            final Approval approval = supervisor.approvals.get(key);

            //Filter approvals to only look at hazards
            if (approval.getPriorityCounter() == 2) {
                //Create new TableRow to be added
                TableRow tr = new TableRow(this);
                tr.setLayoutParams(new TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                //Create TextView for team names based on template
                TextView roomNumber = new TextView(this);
                roomNumber.setText(approval.getJob().getRoomNumber());
                roomNumber.setTextSize(template.getTextSize() / 2);
                roomNumber.setWidth(template.getWidth());
                roomNumber.setHeight(template.getHeight());
                roomNumber.setPadding(
                        template.getPaddingLeft(),
                        template.getPaddingTop() - 5,
                        template.getPaddingRight(),
                        template.getPaddingBottom());
                roomNumber.setBackground(template.getBackground());
                roomNumber.setGravity(template.getGravity());

                //Add click listener for table row
                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Create intent and add variables to bundle for next activity
                        Intent i = new Intent(HazardApprovalsList.this, HazardApprovalPage.class);
                        extras.putString("roomNumber", approval.getJob().getRoomNumber());
                        extras.putString("approvalKey", key);
                        i.putExtras(extras);
                        startActivity(i);
                        finish();
                    }
                });

                //Add tables to TableLayout
                tr.addView(roomNumber);
                table.addView(tr);
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
        Intent i = new Intent(HazardApprovalsList.this, SupervisorHome.class);
        i.putExtras(extras);
        startActivity(i);
        finish();
    }
}
