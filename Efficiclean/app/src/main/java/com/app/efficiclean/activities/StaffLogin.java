package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Housekeeper;
import com.app.efficiclean.classes.Staff;
import com.app.efficiclean.classes.Supervisor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.onesignal.OneSignal;

public class StaffLogin extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;
    public DatabaseReference mRootRef;
    public EditText hotelID;
    public EditText username;
    public EditText password;
    public Button loginBtn;
    public ProgressBar spinner;
    public Staff employee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.efficiclean.R.layout.activity_staff_login);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Set screen orientation based on layout
        if(getResources().getBoolean(R.bool.landscape_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //Map EditTexts to their xml elements
        hotelID = (EditText) findViewById(com.app.efficiclean.R.id.etStaffHotelID);
        username = (EditText) findViewById(com.app.efficiclean.R.id.etUsername);
        password = (EditText) findViewById(com.app.efficiclean.R.id.etPassword);

        //Set progress spinner
        spinner = (ProgressBar) findViewById(com.app.efficiclean.R.id.staffLoginProgress);
        spinner.setVisibility(View.GONE);

        //Map to xml button and set listener
        loginBtn = (Button) findViewById(R.id.btStaffLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Display loading spinner
                spinner.bringToFront();
                spinner.invalidate();
                spinner.setVisibility(View.VISIBLE);
                staffLoginButtonClick();
            }
        });

        //Get instance of Firebase database
        mRootRef = FirebaseDatabase.getInstance().getReference();

        //Create Firebase authenticator
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

        //Create Firebase authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth fbAuth) {
                FirebaseUser user = fbAuth.getCurrentUser();
                if (user != null) {
                    OneSignal.sendTag("uid", user.getUid());

                    spinner.setVisibility(View.GONE);
                    Bundle bundle = new Bundle();
                    bundle.putString("hotelID", hotelID.getText().toString().trim());
                    bundle.putString("staffKey", mAuth.getUid());
                    Intent staffPage = new Intent();

                    if (employee.getJobTitle().equals("supervisor")) {          //Choose which page to display
                        staffPage = new Intent(StaffLogin.this, SupervisorHome.class);
                    } else if (employee.getJobTitle().equals("housekeeper")) {
                        staffPage = new Intent(StaffLogin.this, StaffHome.class);
                    }
                    staffPage.putExtras(bundle);
                    startActivity(staffPage);
                    onStop();
                }
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
        finish();
        Intent i = new Intent(StaffLogin.this, GuestLogin.class);
        startActivity(i);
    }

    private void staffLoginButtonClick() {
        //Get input values from EditText boxes
        String hNumber = hotelID.getText().toString().trim();
        String uString = username.getText().toString().trim();
        String pString = password.getText().toString().trim();

        if (!hNumber.equals("") && !uString.equals("") && !pString.equals("")) {      //Check for no null values before we search database
            setValidationValues(hNumber, uString, pString);
        } else {                                                                      //Display error message if incorrect user input
            spinner.setVisibility(View.GONE);
            Toast.makeText(StaffLogin.this, "Please complete all fields and try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setValidationValues(final String hNumber, final String uString, final String pString) {
        //Create DatabaseReference to staff
        DatabaseReference mStaffRef = mRootRef.child(hNumber).child("staff");

        //Create ValueEventListener to read data from reference
        mStaffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Create boolean to check if the correct details were found in the database
                Boolean correctDetails = false;

                //Iterate through the staff members
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("username").getValue(String.class).toLowerCase().equals(uString.toLowerCase())                     //Check if user input matches staff member details
                            && ds.child("password").getValue(String.class).equals(pString)) {
                        correctDetails = true;                                                              //Change value of correctDetails
                        if (!ds.child("teamID").getValue(String.class).equals("Absent")) {
                            Housekeeper housekeeper = ds.getValue(Housekeeper.class);                       //Create Housekeeper from Firebase Data
                            validateValues(housekeeper);
                        } else {
                            spinner.setVisibility(View.GONE);
                            Toast.makeText(StaffLogin.this, "This staff member is marked as absent for today.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
                if (!correctDetails) {
                    setSupervisorValues(hNumber, uString, pString);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setSupervisorValues(String hNumber, final String uString, final String pString) {
        DatabaseReference mSupervisorRef = mRootRef.child(hNumber).child("supervisor");

        mSupervisorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Create boolean to check if the correct details were found in the database
                Boolean correctDetails = false;

                //Iterate through the staff members
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("username").getValue(String.class).toLowerCase().equals(uString.toLowerCase())                     //Check if user input matches staff member details
                            && ds.child("password").getValue(String.class).equals(pString)) {
                        correctDetails = true;                                                          //Change value of correctDetails
                        Supervisor supervisor = ds.getValue(Supervisor.class);                       //Create Housekeeper from Firebase Data
                        validateValues(supervisor);
                    }
                }
                if (!correctDetails) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(StaffLogin.this, "Your details seem to be incorrect. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void validateValues(Staff worker) {
        employee = worker;
        if (worker != null) {
            //Create user email and password for authentication
            String eString = worker.getUsername() + "@efficiclean.com";
            String pString = worker.getPassword();

            //Authorise user with Firebase
            mAuth.signInWithEmailAndPassword(eString, pString)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                spinner.setVisibility(View.GONE);
                                Toast.makeText(StaffLogin.this, "Authentication failed.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
            });
        } else {
            spinner.setVisibility(View.GONE);
            Toast.makeText(StaffLogin.this, "Error authenticating with the server. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
