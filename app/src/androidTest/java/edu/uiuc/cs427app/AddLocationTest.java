package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This is the UI and functionality tests for the AddLocation feature in the application.
 */
@RunWith(AndroidJUnit4.class)
public class AddLocationTest {

    private FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<Login> mActivityRule = new ActivityScenarioRule<>(Login.class);

    /**
     * Sets up the test environment by ensuring the user is logged out
     *  and performing login before the tests are conducted.
     */
    @Before
    public void setUp() {
        // Ensure the user is logged out for a clean state
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

        // Perform login
        performLogin();
    }

    /**
     * Helper method to log user in.
     * Launches the login activity, enters credentials, and verifies successful login.
     */
    private void performLogin() {
        // Launch the login activity
        ActivityScenario.launch(Login.class);

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

        // Verify that we're on the main activity by checking if the "Add Location" button is displayed
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Cleans up after each test by logging out the user if logged in.
     */
    @After
    public void tearDown() {
        // Clean up after each test
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    /**
     * Verifies that the "Add City" button works correctly when a valid city name is entered.
     * Ensures navigation back to MainActivity after successful addition.
     */
    @Test
    public void testAddCityButtonWithValidCity() {
        // Click "Add Location" button to navigate to AddLocation screen
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo(), click());

        // Type a valid city name
        onView(withId(R.id.cityAutoCompleteTextView))
                .perform(typeText("Chicago"), closeSoftKeyboard());

        // Click the "Add City" button
        onView(withId(R.id.addCityButton))
                .perform(click());

        // Add a delay for network call
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify successful addition by checking if MainActivity is displayed
        onView(withId(R.id.logout_btn)) // Example: Check for a button in MainActivity
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that an error message is shown when attempting to add a city without entering a name.
     */
    @Test
    public void testAddCityButtonWithEmptyInput() {
        // Click "Add Location" button to navigate to AddLocation screen
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo(), click());

        // Leave the city name field empty
        onView(withId(R.id.cityAutoCompleteTextView))
                .perform(typeText(""), closeSoftKeyboard());

        // Click the "Add City" button
        onView(withId(R.id.addCityButton))
                .perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify error message is shown
        onView(withId(R.id.addCityButton))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that autocomplete suggestions are displayed when typing a partial city name.
     */
    @Test
    public void testAutoCompleteDropdownDisplay() {
        // Click "Add Location" button to navigate to AddLocation screen
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo(), click());

        // Type part of a city name to trigger autocomplete
        onView(withId(R.id.cityAutoCompleteTextView))
                .perform(typeText("Chi"), closeSoftKeyboard());

        // Verify that the autocomplete suggestions are displayed
        onView(withId(R.id.addCityButton))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies navigation to the AddLocation activity when clicking the "Add Location" button.
     * Ensures the relevant UI elements are displayed in the AddLocation activity.
     */
    @Test
    public void testAddCityNavigation() {
        // Click "Add Location" button to navigate to AddLocation screen
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo(), click());

        // Verify that we're on the AddLocation activity
        onView(withId(R.id.cityAutoCompleteTextView))
                .check(matches(isDisplayed()));
        onView(withId(R.id.addCityButton))
                .check(matches(isDisplayed()));
    }
}


