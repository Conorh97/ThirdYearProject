package com.app.efficiclean.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Guest;
import com.app.efficiclean.classes.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


public class GuestHome extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;
    public DatabaseReference mRootRef;
    public Guest guest;
    public String guestKey;
    public String hotelID;
    public Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create instance for current time and for 15:00
        Calendar currentTime = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 20);
        midnight.set(Calendar.MINUTE, 0);

        //Block user actions if current time is too late
        if(midnight.getTimeInMillis() - currentTime.getTimeInMillis() <= 0){
            setContentView(R.layout.activity_guest_cannot_mark);
        } else {
            setContentView(R.layout.activity_guest_home);
        }

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
            guest = (Guest) extras.getSerializable("thisGuest");
        }

        //Get instance of Firebase database
        mRootRef = FirebaseDatabase.getInstance().getReference();

        //Create Firebase authenticator
        mAuth = FirebaseAuth.getInstance();
        guestKey = mAuth.getUid();
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

    public void pleaseServiceButtonClick(View view) {
        //Create new job and set job variables
        Job newJob = new Job();
        newJob.setCreatedBy(guestKey);
        newJob.setRoomNumber(guest.getRoomNumber());
        newJob.setPriority(0);

        //Write new job to database
        mRootRef.child(hotelID).child("jobs").push().setValue(newJob);
        mRootRef.child(hotelID).child("rooms").child(guest.getRoomNumber()).child("status").setValue("To Be Cleaned");
        changePage("service");
    }

    public void doNotDisturbButtonClick(View view) {
        //Update value of room in database
        mRootRef.child(hotelID).child("rooms").child(guest.getRoomNumber()).child("status").setValue("Do Not Disturb");
        changePage("notDisturb");
    }

    public void checkingOutButtonClick(View view) {
        //Create new job and set job variables
        Job newJob = new Job();
        newJob.setRoomNumber(guest.getRoomNumber());
        newJob.setPriority(0);

        //Write new job to database
        mRootRef.child(hotelID).child("jobs").push().setValue(newJob);
        mRootRef.child(hotelID).child("rooms").child(guest.getRoomNumber()).child("status").setValue("To Be Cleaned");
        changePage("checkingOut");
    }

    public void changePage(String choice) {
        Intent guestChoicePage = null;

        //Select correct intent based on user action
        if (choice.equals("service")) {
            guestChoicePage = new Intent(GuestHome.this, GuestPleaseService.class);
        } else if (choice.equals("notDisturb")) {
            guestChoicePage = new Intent(GuestHome.this, GuestDoNotDisturb.class);
        } else if (choice.equals("checkingOut")) {
            guestChoicePage = new Intent(GuestHome.this, GuestCheckingOut.class);
        }

        if (guestChoicePage != null) {
            //Start new intent
            guestChoicePage.putExtras(extras);
            startActivity(guestChoicePage);
            finish();
        } else {
            Toast.makeText(GuestHome.this, "An error has occurred. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
