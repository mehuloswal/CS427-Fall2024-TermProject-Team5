package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;


import static org.hamcrest.Matchers.not;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for user logout functionality in the application.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserLogoutTest {

    @Rule
    public ActivityScenarioRule<Login> mActivityRule = new ActivityScenarioRule<>(Login.class);

    private FirebaseAuth mAuth;
    private View decorView;

    /**
     * Sets up the test environment by signing out any logged-in user
     * and initializing the decor view for UI verification.
     */
    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        // Sign out before each test to ensure a clean state
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
        mActivityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<Login>() {
            @Override
            public void perform(Login activity) {
                decorView = activity.getWindow().getDecorView();
            }
        });
    }

    /**
     * Cleans up after each test by signing out the user if logged in.
     */
    @After
    public void tearDown() {
        // Clean up after each test
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    /**
     * Verifies that the user can successfully log out.
     * The test involves:
     * - Logging in with valid credentials.
     * - Navigating to the main screen.
     * - Logging out and returning to the login screen.
     */
    @Test
    public void testLogout() {
        // Log in with valid credentials first
        onView(withId(R.id.username))
                .perform(typeText("ruipeng2"), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(click());

        // Wait for the login process to complete and the main screen to load
        try {
            Thread.sleep(1000);  // Allow some time for the login process to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that the logout button is displayed, indicating successful login
        onView(withId(R.id.logout_btn))
                .check(matches(isDisplayed()));

        // Click the logout button
        onView(withId(R.id.logout_btn))
                .perform(click());

        // Wait for the logout process to complete
        try {
            Thread.sleep(1000);  // Allow some time for logout to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that we are back on the login screen by checking if the login button,
        // the username and password inputs are displayed
        onView(withId(R.id.btn_login))
                .check(matches(isDisplayed()));
        onView(withId(R.id.username))
                .check(matches(isDisplayed()));
        onView(withId(R.id.password))
                .check(matches(isDisplayed()));
    }
}
