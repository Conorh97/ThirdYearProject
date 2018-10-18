package com.app.efficiclean.activities;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import com.app.efficiclean.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

public class StaffLoginInstrumentedTest1 {

    @Rule
    public ActivityTestRule<StaffLogin> mActivity = new ActivityTestRule<StaffLogin>(StaffLogin.class);

    private StaffLogin sLogin;
    
    @Before
    public void setUp() throws Exception {
        sLogin = mActivity.getActivity();
    }

    @Test
    public void test() {
        assertNotNull(sLogin.hotelID);
        assertNotNull(sLogin.loginBtn);
        assertNotNull(sLogin.mAuth);
        assertNotNull(sLogin.mAuthListener);
        assertNotNull(sLogin.mRootRef);
        assertNotNull(sLogin.password);
        assertNotNull(sLogin.spinner);
        assertNotNull(sLogin.username);
    }

    @Test
    public void login_1() {
        onView(ViewMatchers.withId(R.id.etStaffHotelID)).perform(typeText("0582"));
        closeSoftKeyboard();
        onView(ViewMatchers.withId(R.id.etUsername)).perform(typeText("hanlonc5"));
        closeSoftKeyboard();
        onView(ViewMatchers.withId(R.id.etPassword)).perform(typeText("switch"));
        closeSoftKeyboard();

        onView(withId(R.id.btStaffLogin)).perform(click());
    }

    @Test
    public void login_2() {
        onView(withId(R.id.btStaffLogin)).perform(click());
    }

    @Test
    public void back() {
        onView(withContentDescription("Navigate up")).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        sLogin = null;
    }

}