package com.app.efficiclean.activities;

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
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class GuestLoginInstrumentedTest2 {

    @Rule
    public ActivityTestRule<GuestLogin> mActivity = new ActivityTestRule<GuestLogin>(GuestLogin.class);

    private GuestLogin gLogin;

    @Before
    public void setUp() throws Exception {
        gLogin = mActivity.getActivity();
    }

    @Test
    public void testStart() {
        assertNotNull(gLogin.hotelID);
        assertNotNull(gLogin.roomNumber);
        assertNotNull(gLogin.forename);
        assertNotNull(gLogin.surname);
        assertNotNull(gLogin.spinner);
        assertNotNull(gLogin.loginBtn);
        assertNotNull(gLogin.mRootRef);
        assertNotNull(gLogin.mAuth);
        assertNotNull(gLogin.mAuthListener);
    }

    @Test
    public void login() {
        onView(withId(R.id.etForename)).perform(typeText("staff1"));
        closeSoftKeyboard();

        onView(withId(R.id.btLogin)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        gLogin = null;
    }

}
