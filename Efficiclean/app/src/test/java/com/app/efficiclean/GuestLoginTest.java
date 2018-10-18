package com.app.efficiclean;

import android.content.Intent;
import android.widget.ProgressBar;
import com.app.efficiclean.activities.GuestLogin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GuestLoginTest {

    @Mock private GuestLogin mockActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ProgressBar spinner = mock(ProgressBar.class);
        mockActivity.spinner = spinner;
    }

    @Test
    public void test_loginButtonClick_1() {
        doCallRealMethod().when(mockActivity).loginButtonClick(anyString(), anyString(), anyString(), anyString());
        mockActivity.loginButtonClick("0582", "101", "Conor", "Hanlon");
        verify(mockActivity, times(1)).setValidationValues("0582", "101", "Conor", "Hanlon");
    }

    @Test
    public void test_loginButtonClick_2() {
        doCallRealMethod().when(mockActivity).loginButtonClick(anyString(), anyString(), anyString(), anyString());
        mockActivity.loginButtonClick("", "", "staff1", "");

        verify(mockActivity, never()).setValidationValues("0582", "101", "Conor", "Hanlon");
        verify(mockActivity, times(1)).startActivity(any(Intent.class));
    }
}