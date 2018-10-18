package com.app.efficiclean.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.app.efficiclean.R;
import com.app.efficiclean.classes.Guest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class GuestHomeInstrumentedTest {

    @Rule
    public ActivityTestRule<GuestHome> mActivity = new ActivityTestRule<GuestHome>(GuestHome.class);

    private GuestHome gHome;

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
        gHome = mActivity.launchActivity(i);
        gHome.guestKey = "2sCyGG15ahTb0drTiIOoxQFRwma2";
    }

    @Test
    public void test() {
        assertNotNull(gHome.extras);
        assertNotNull(gHome.mAuth);
        assertNotNull(gHome.mAuthListener);
        assertNotNull(gHome.mRootRef);
        assertNotNull(gHome.guest);
        assertNotNull(gHome.guestKey);
        assertNotNull(gHome.hotelID);
    }

    @Test
    public void pleaseService() {
        onView(withId(R.id.btPleaseService)).perform(click());
    }

    @Test
    public void doNotDisturb() {
        onView(withId(R.id.btDoNotDisturb)).perform(click());
    }

    @Test
    public void checkingOut() {
        onView(withId(R.id.btCheckingOut)).perform(click());
    }

    @Test
    public void back_1() {
        onView(withContentDescription("Navigate up")).perform(click());
        onView(withText("Yes")).perform(click());
    }

    @Test
    public void back_2() {
        onView(withContentDescription("Navigate up")).perform(click());
        onView(withText("No")).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        gHome = null;
    }
}
