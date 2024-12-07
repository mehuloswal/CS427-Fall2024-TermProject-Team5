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

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for user registration functionality in the application.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserRegisterTest {

    @Rule
    public ActivityScenarioRule<Login> activityRule = new ActivityScenarioRule<>(Login.class);

    private FirebaseAuth mAuth;
    private View decorView;

    /**
     * Sets up the test environment by signing out any logged-in user,
     * navigating to the register page, and initializing the decor view.
     */
    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
        ActivityScenario<Login> scenario = ActivityScenario.launch(Login.class);
        scenario.onActivity(activity -> {
            decorView = activity.getWindow().getDecorView();
        });

        // Navigate to Register page
        onView(withId(R.id.registerNow)).perform(click());
    }

    /**
     * Cleans up after each test by signing out the user and introducing a delay for stability.
     */
    @After
    public void tearDown() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

        // Add delay after each test
        try {
            Thread.sleep(1000); // Sleep for 1 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifies that all elements on the registration screen are displayed.
     */
    @Test
    public void testAllElementsDisplayed() {
        // Check title text
        onView(withText("Register"))
                .check(matches(isDisplayed()));

        // Check input fields
        onView(withId(R.id.username))
                .check(matches(isDisplayed()));
        onView(withId(R.id.password))
                .check(matches(isDisplayed()));
        onView(withId(R.id.confirmPassword))
                .check(matches(isDisplayed()));

        // Check progress bar exists (though it should be hidden initially)
        onView(withId(R.id.progressBar))
                .check(matches(Matchers.not(isDisplayed())));

        // Check register button
        onView(withId(R.id.btn_register))
                .check(matches(isDisplayed()));

        // Check login link
        onView(withId(R.id.loginNow))
                .check(matches(isDisplayed()));

        // Check theme switch
        onView(withId(R.id.themeSwitch))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that attempting to register with empty fields shows an appropriate error message.
     */
    @Test
    public void testEmptyFields() {
        // Click register without entering any data
        onView(withId(R.id.btn_register))
                .perform(click());

        // Verify error message
        onView(withText("All fields are required"))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that attempting to register with mismatched passwords shows an appropriate error message.
     */
    @Test
    public void testPasswordMismatch() {
        // Enter username
        onView(withId(R.id.username))
                .perform(typeText("testuser"), closeSoftKeyboard());

        // Enter different passwords
        onView(withId(R.id.password))
                .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPassword))
                .perform(typeText("password456"), closeSoftKeyboard());

        // Click register
        onView(withId(R.id.btn_register))
                .perform(click());

        // Verify error message
        onView(withText("Passwords do not match"))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that attempting to register with an email-like username shows an appropriate error message.
     */
    @Test
    public void testEmailPatternValidation() {
        // Enter email-like username
        onView(withId(R.id.username))
                .perform(typeText("test@example.com"), closeSoftKeyboard());

        // Enter matching passwords
        onView(withId(R.id.password))
                .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Click register
        onView(withId(R.id.btn_register))
                .perform(click());

        // Verify error message
        onView(withText("Username should not contain '@domain'. It will be appended automatically as '@illinois.edu'."))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that clicking the login link navigates back to the login screen.
     */
    @Test
    public void testNavigateBackToLogin() {
        // Click on login link
        onView(withId(R.id.loginNow))
                .perform(click());

        // Verify we're back on login screen
        onView(withId(R.id.btn_login))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies the functionality of the theme switch toggle on the registration screen.
     */
    @Test
    public void testThemeSwitchFunctionality() {
        // Check initial state
        onView(withId(R.id.themeSwitch))
                .check(matches(isDisplayed()));

        // Toggle theme
        onView(withId(R.id.themeSwitch))
                .perform(click());

        // Toggle back
        onView(withId(R.id.themeSwitch))
                .perform(click());
    }

    /**
     * Verifies successful registration by checking redirection to the main activity and success message.
     */
    @Test
    public void testSuccessfulRegistration() {
        // Randomly generate a user name:
        String randomUsername = "espresso" + System.currentTimeMillis();

        // Enter username
        onView(withId(R.id.username))
                .perform(typeText(randomUsername), closeSoftKeyboard());

        // Enter password
        onView(withId(R.id.password))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Enter confirm password
        onView(withId(R.id.confirmPassword))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.btn_register))
                .perform(click());

        // Wait for registration process and redirection
        try {
            Thread.sleep(1000); // Longer wait to allow for Firebase registration and backend calls
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify we're on main activity by checking for specific elements
        onView(withId(R.id.logout_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.buttonAddLocation))
                .check(matches(isDisplayed()));

        try {
            Thread.sleep(500); // Longer wait to allow for Firebase registration and backend calls
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify success toast
        onView(withText("User created successfully"))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that attempting to register with an already existing username shows an error message.
     */
    @Test
    public void testDuplicateRegistration() {
        // Add delay at start of test
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Enter existing username
        onView(withId(R.id.username))
                .perform(typeText("ruipeng2"), closeSoftKeyboard());

        // Enter password
        onView(withId(R.id.password))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Enter confirm password
        onView(withId(R.id.confirmPassword))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.btn_register))
                .perform(click());

        // Wait for registration attempt
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify error message
        onView(withText("The email address is already in use by another account."))
                .inRoot(withDecorView(Matchers.not(decorView)))
                .check(matches(isDisplayed()));

        // Verify we're still on the register page
        onView(withId(R.id.btn_register))
                .check(matches(isDisplayed()));
    }
}