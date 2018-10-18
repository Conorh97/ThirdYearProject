package com.app.efficiclean.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Guest;
import com.app.efficiclean.services.*;
import com.firebase.jobdispatcher.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.onesignal.OneSignal;

import java.util.Calendar;

public class GuestLogin extends AppCompatActivity {
    
    public String hid = "";
    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;
    public DatabaseReference mRootRef;
    public EditText hotelID;
    public EditText roomNumber;
    public EditText forename;
    public EditText surname;
    public Button loginBtn;
    public ProgressBar spinner;
    public Guest guest;
    private FirebaseJobDispatcher jobDispatcher;
    private Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_login);

        //Set screen orientation based on layout
        if(getResources().getBoolean(R.bool.landscape_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //Initiate OneSignal messageing application
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        //Map EditTexts to their xml elements
        hotelID = (EditText) findViewById(R.id.etHotelID);
        roomNumber = (EditText) findViewById(R.id.etRoomNumber);
        forename = (EditText) findViewById(R.id.etForename);
        surname = (EditText) findViewById(R.id.etSurname);

        //Set progress spinner
        spinner = (ProgressBar) findViewById(R.id.guestLoginProgress);
        spinner.setVisibility(View.GONE);

        //Map to xml button and set listener
        loginBtn = (Button) findViewById(R.id.btLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get input values from EditText boxes
                String hNumber = hotelID.getText().toString().trim();
                String rNumber = roomNumber.getText().toString().trim();
                String fString = forename.getText().toString().trim();
                String sString = surname.getText().toString().trim();

                //Display loading spinner
                spinner.bringToFront();
                spinner.invalidate();
                spinner.setVisibility(View.VISIBLE);
                loginButtonClick(hNumber, rNumber, fString, sString);
            }
        });

        //Create job dispatcher to run our services
        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

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
                //Get current user
                FirebaseUser user = fbAuth.getCurrentUser();

                //Login user if not null
                if (user != null) {
                    //Run our services
                    scheduleReset();
                    allocateTeams();
                    updateJobPriorities();
                    updateTeamPriorities();
                    updateBreakStatus();
                    allocateBreaks();
                    initiateQueue();
                    allocateMarkingTask();

                    //Send unique user id to OneSignal platform
                    OneSignal.sendTag("uid", user.getUid());

                    //Create Bundle to pass information to next activity
                    bundle.putString("hotelID", hotelID.getText().toString().trim());
                    bundle.putSerializable("thisGuest", guest);

                    //Listen to value at the guest's room number in the database
                    mRootRef.child(hotelID.getText().toString()).child("rooms").child(guest.getRoomNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String status = dataSnapshot.child("status").getValue(String.class);
                            Intent guestHomePage;
                            if (status.equals("Completed")) {
                                //Intent if guest's room has been serviced today
                                guestHomePage = new Intent(GuestLogin.this, GuestCompleted.class);
                            } else if (status.equals("Idle") == false && status.equals("Do Not Disturb") == false) {
                                //Intent if guest's room currently has a job on the queue
                                guestHomePage = new Intent(GuestLogin.this, GuestPleaseService.class);
                            } else {
                                //Intent to bring guest's to their home page
                                guestHomePage = new Intent(GuestLogin.this, GuestHome.class);
                            }
                            //Hide spinner and change activity
                            spinner.setVisibility(View.GONE);
                            guestHomePage.putExtras(bundle);
                            startActivity(guestHomePage);
                            onStop();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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

    public void loginButtonClick(String hNumber, String rNumber, String fString, String sString) {
        hid = hNumber;

        if (!fString.equals("") && fString.toLowerCase().equals("staff1")) {
            //Condition to pass to staff login page
            spinner.setVisibility(View.GONE);
            Intent staffPage = new Intent(getApplicationContext(), StaffLogin.class);
            startActivity(staffPage);
        } else if (!hNumber.equals("") && !rNumber.equals("") && !fString.equals("") && !sString.equals("")) {      //Check for no null values before we search database
            setValidationValues(hNumber, rNumber, fString, sString);
        } else {
            //Display error message if incorrect user input
            spinner.setVisibility(View.GONE);
            Toast.makeText(GuestLogin.this, "Please complete all fields and try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void setValidationValues(final String hNumber, final String rNumber, final String fString, final String sString) {
        //Create DatabaseReference to specified hotel room
        DatabaseReference mRoomRef = mRootRef.child(hNumber).child("rooms").child(rNumber).child("currentGuest");

        //Create ValueEventListener to read data from reference
        mRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get guest unique id key
                String guestKey = dataSnapshot.getValue(String.class);

                if (guestKey != null) {
                    //Create DatabaseReference to specified guest
                    DatabaseReference mGuestRef = mRootRef.child(hNumber).child("guest").child(guestKey);

                    //Create ValueEventListener to read data from reference
                    mGuestRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Get guests values from Firebase
                            guest = dataSnapshot.getValue(Guest.class);

                            //Start validation process
                            validateValues(rNumber, fString, sString);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(GuestLogin.this, "Your details seem to be incorrect. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void validateValues(String rNumber, String fString, String sString) {
        if (guest != null
                && guest.getForename().toLowerCase().equals(fString.toLowerCase())
                && guest.getSurname().toLowerCase().equals(sString.toLowerCase())){        //Validates that input data matches values from database
            //Create user email and password for authentication
            String pString = fString.toLowerCase() + sString.toLowerCase() + rNumber;
            final String eString = pString + "@efficiclean.com";

            //Authorise user with Firebase
            mAuth.signInWithEmailAndPassword(eString, pString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                spinner.setVisibility(View.GONE);
                                Toast.makeText(GuestLogin.this, "Authentication failed.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                OneSignal.syncHashedEmail(eString);
                            }
                        }
                    });
        } else {
            spinner.setVisibility(View.GONE);
            Toast.makeText(GuestLogin.this, "Your details seem to be incorrect. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public int[] getTimes(int hour, int minute) {
        //Create instance with inputted hour and time and instance of current time
        Calendar currentTime = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, hour);
        midnight.set(Calendar.MINUTE, minute);

        //Get amount of seconds until inputted time
        long difference = midnight.getTimeInMillis() - currentTime.getTimeInMillis();

        int startSeconds = (int) (difference / 1000);
        int endSeconds = startSeconds + 86400;

        //Create array with start and end times
        int[] times = {startSeconds, endSeconds};
        return times;
    }

    public void scheduleReset() {
        //Get start time of service and recurring time
        int[] times = getTimes(23, 45);

        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        //Create job for ResetSystemStatus class
        Job job = jobDispatcher.newJobBuilder()
                .setService(ResetSystemStatus.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag(hid + " RESET SERVICE")
                .setTrigger(Trigger.executionWindow(times[0], times[1]))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .build();

        jobDispatcher.mustSchedule(job);
        Log.v(hid + " RESET SERVICE", "Reset system data for next day");
    }

    public void allocateTeams() {
        //Get start time of service and recurring time
        int[] times = getTimes(23, 55);

        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        //Create job for TeamAllocator class
        Job job = jobDispatcher.newJobBuilder()
                .setService(TeamAllocator.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag(hid + " TEAM SERVICE")
                .setTrigger(Trigger.executionWindow(times[0], times[1]))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .build();

        jobDispatcher.mustSchedule(job);
        Log.v(hid + " TEAM SERVICE", "Allocate housekeeper teams for next day");
    }

    public void updateJobPriorities() {
        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        //Create job for UpdateJobPriorities class
        Job job = jobDispatcher.newJobBuilder()
                .setService(UpdateJobPriorities.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag(hid + " JOB SERVICE")
                .setTrigger(Trigger.executionWindow(300, 300))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .build();

        jobDispatcher.mustSchedule(job);
        Log.v(hid + " JOB SERVICE", "Update priority of jobs on the queue");
    }

    public void updateTeamPriorities() {
        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        //Create job for UpdateTeamPriorities class
        Job job = jobDispatcher.newJobBuilder()
                .setService(UpdateTeamPriorities.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag(hid + " TEAM PRIORITY SERVICE")
                .setTrigger(Trigger.executionWindow(600, 600))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .build();

        jobDispatcher.mustSchedule(job);
        Log.v(hid + " TEAM PRIORITY SERVICE", "Update priority of teams on the queue");
    }

    public void updateBreakStatus() {
        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        //Create job for UpdateBreakStatus class
        Job job = jobDispatcher.newJobBuilder()
                .setService(UpdateBreakStatus.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag(hid + " BREAK SERVICE")
                .setTrigger(Trigger.executionWindow(60, 60))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .build();

        jobDispatcher.mustSchedule(job);
        Log.v(hid + " BREAK SERVICE", "Remove teams on break from the queue");
    }

    public void allocateBreaks() {
        int[] times = getTimes(12, 0);

        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        if (times[0] >= 0 && times[1] >= 0) {
            //Create job for BreakAllocator class
            Job job = jobDispatcher.newJobBuilder()
                    .setService(BreakAllocator.class)
                    .setLifetime(Lifetime.FOREVER)
                    .setRecurring(true)
                    .setTag(hid + " BREAK ALLOCATION SERVICE")
                    .setTrigger(Trigger.executionWindow(times[0], times[1]))
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .setReplaceCurrent(false)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .setExtras(extras)
                    .build();

            jobDispatcher.mustSchedule(job);
            Log.v(hid + " BREAK SERVICE", "Allocate team breaks with remaining break time");
        }
    }

    public void initiateQueue() {
        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        //Create job for QueueHandlerService class
        Job job = jobDispatcher.newJobBuilder()
                .setService(QueueHandlerService.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag(hid + " QUEUE SERVICE")
                .setTrigger(Trigger.executionWindow(60, 86400))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .build();

        jobDispatcher.mustSchedule(job);
        Log.v(hid + " QUEUE SERVICE", "Initiate queue service for job allocation");
    }

    public void allocateMarkingTask() {
        //Create bundle to pass variables to service
        Bundle extras = new Bundle();
        extras.putString("hid", hid);

        //Create job for CheckRooms class
        Job job = jobDispatcher.newJobBuilder()
                .setService(CheckRooms.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag(hid + " MARKING SERVICE")
                .setTrigger(Trigger.executionWindow(60, 1500))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .build();

        jobDispatcher.mustSchedule(job);
        Log.v(hid + " MARKING SERVICE", "Allocate job to a team to mark the status of rooms");
    }
}
