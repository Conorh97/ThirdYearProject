package com.app.efficiclean.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Guest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

public class GuestDoNotDisturbInstrumentedTest {

    @Rule
    public ActivityTestRule<GuestDoNotDisturb> mActivity = new ActivityTestRule<GuestDoNotDisturb>(GuestDoNotDisturb.class);

    private GuestDoNotDisturb gDoNotDisturb;

    @Before
    public void setUp() throws Exception {
        Guest guest = new Guest();
        guest.setForename("Conor");
        guest.setRoomNumber("101");
        guest.setSurname("Hanlon");

        Bundle bundle = new Bundle();
        bundle.putString("hotelID", "0582");
        bundle.putSerializable("thisGuest", guest);
        Intent i = new Intent();
        i.putExtras(bundle);
        gDoNotDisturb = mActivity.launchActivity(i);
    }

    @Test
    public void test() {
        assertNotNull(gDoNotDisturb.btHome);
        assertNotNull(gDoNotDisturb.extras);
        assertNotNull(gDoNotDisturb.guest);
        assertNotNull(gDoNotDisturb.hotelID);
        assertNotNull(gDoNotDisturb.mAuth);
        assertNotNull(gDoNotDisturb.mAuthListener);
    }

    @Test
    public void home() {
        onView(withId(R.id.btHome)).perform(click());
    }

    @Test
    public void back() {
        onView(withContentDescription("Navigate up")).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        gDoNotDisturb = null;
    }
}