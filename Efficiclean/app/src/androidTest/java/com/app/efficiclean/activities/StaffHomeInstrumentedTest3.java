package com.app.efficiclean.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.app.efficiclean.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class StaffHomeInstrumentedTest3 {

    @Rule
    public ActivityTestRule<StaffHome> mActivity = new ActivityTestRule<StaffHome>(StaffHome.class, true, false);

    private StaffHome sHome;

    @Before
    public void setUp() throws Exception {
        Bundle bundle = new Bundle();
        bundle.putString("hotelID", "0582");
        bundle.putString("staffKey", "EhLu55QL1MZ3U4tpWAEHtlQrKKE2");
        Intent i = new Intent();
        i.putExtras(bundle);

        mActivity.launchActivity(i);
        closeSoftKeyboard();
        sHome = mActivity.getActivity();
    }

    @Test
    public void test() {
        assertNotNull(sHome.btCurrentJob);
        assertNotNull(sHome.btRequestBreak);
        assertNotNull(sHome.btViewMap);
        assertNotNull(sHome.extras);
        assertNotNull(sHome.hotelID);
        assertNotNull(sHome.mAuth);
        assertNotNull(sHome.mAuthListener);
        assertNotNull(sHome.mJobRef);
        assertNotNull(sHome.mStaffRef);
        assertNotNull(sHome.staffKey);
    }

    @Test
    public void currentJob() {
        onView(withId(R.id.btCurrentJob)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        sHome = null;
    }

}
