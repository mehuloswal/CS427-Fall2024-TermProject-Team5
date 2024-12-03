package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.RootMatchers.isSystemAlertWindow;

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
import org.hamcrest.Matchers;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginRegisterTest {

    @Rule
    public ActivityScenarioRule<Login> mActivityRule = new ActivityScenarioRule<>(Login.class);

    private FirebaseAuth mAuth;
    private View decorView;

    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        // Sign out before each test to ensure a clean state
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
//        ActivityScenario.launch(Login.class);
        mActivityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<Login>() {
            @Override
            public void perform(Login activity) {
                decorView = activity.getWindow().getDecorView();
            }
        });
    }

    @After
    public void tearDown() {
        // Clean up after each test
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    @Test
    public void testValidLogin() {
        // Enter username
        onView(withId(R.id.username))
                .perform(typeText("ruipeng2"), closeSoftKeyboard());

        // Enter password
        onView(withId(R.id.password))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.btn_login))
                .perform(click());

        // Add a small delay to allow for the login process
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that we're on the main activity by checking if the logout button is displayed
        onView(withId(R.id.logout_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEmptyUsername() {
        // Enter only password
        onView(withId(R.id.password))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.btn_login))
                .perform(click());

        // Wait for toast
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify error message
        onView(withText("Authentication failed."))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));

    }

    @Test
    public void testEmptyPassword() {
        // Enter only username
        onView(withId(R.id.username))
                .perform(typeText("ruipeng2"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.btn_login))
                .perform(click());

        // Wait for toast
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify error message
        onView(withText("Please enter password"))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidCredentials() {
        // Enter invalid username and password
        onView(withId(R.id.username))
                .perform(typeText("wronguser"), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText("wrongpass"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.btn_login))
                .perform(click());

        // Wait for authentication process
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify error message
        onView(withText("Authentication failed."))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));

        // Verify the login btn still exists
        onView(withId(R.id.btn_login))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToRegister() {
        // Click on register link
        onView(withId(R.id.registerNow))
                .perform(click());

        // Verify we're on register screen
        onView(withId(R.id.confirmPassword))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btn_register))
                .check(matches(isDisplayed()));
    }
}