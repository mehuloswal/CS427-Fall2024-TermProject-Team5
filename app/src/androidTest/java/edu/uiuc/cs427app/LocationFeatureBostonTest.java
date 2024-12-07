package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test to verify the map feature for "Boston".
 */
@RunWith(AndroidJUnit4.class)
public class LocationFeatureBostonTest {

    private FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<Login> mActivityRule = new ActivityScenarioRule<>(Login.class);

    @Before
    public void setUp() {
        // Ensure the user is logged out for a clean state
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
        // Perform login
        performLogin();
        // Ensure "Boston" is in the city list
        ensureCityExists("Boston");
    }

    /**
     * Helper method to perform user login before testing.
     * Performs actions and checks assertions related to login.
     */
    private void performLogin() {
        // Launch login activity
        ActivityScenario.launch(Login.class);

        // Enter username and password
        onView(withId(R.id.username))
                .perform(typeText("ruipeng2"), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.btn_login))
                .perform(click());

        // Wait for the login process
        try {
            Thread.sleep(1000); // Pause to allow login to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify main activity is displayed
        onView(withId(R.id.buttonAddLocation))
                .check(matches(isDisplayed()));
    }

    /**
     * Ensures that a city is in the user's city list. If it already exists, does
     * not add it again.
     * Includes actions and assertions to verify city presence.
     *
     * @param cityName The name of the city to ensure exists.
     */
    private void ensureCityExists(String cityName) {
        // Check if the city is already displayed
        try {
            onView(withText(cityName))
                    .check(matches(isDisplayed()));
        } catch (Exception e) {
            // City is not displayed, add it
            addCity(cityName);
        }
    }

    /**
     * Helper method to add a city to the user's city list.
     * Includes actions and assertions for adding a city.
     *
     * @param cityName The name of the city to add.
     */
    private void addCity(String cityName) {
        // Click "Add Location" button
        onView(withId(R.id.buttonAddLocation))
                .perform(click());

        // Verify that we're on the AddLocation activity
        onView(withId(R.id.addCityButton))
                .check(matches(isDisplayed()));

        // Type the city name
        onView(withId(R.id.cityAutoCompleteTextView))
                .perform(typeText(cityName), closeSoftKeyboard());

        // Click "Add City" button
        onView(withId(R.id.addCityButton))
                .perform(click());

        // Pause to allow city to be added
        try {
            Thread.sleep(2000); // Wait for the city to be added
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify the city is displayed in the main activity
        onView(withText(cityName))
                .check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        // Sign out after the test
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    /**
     * Test the map feature for "Boston".
     * Includes actions and assertions for testing the map functionality.
     */
    @Test
    public void testMapFeatureBoston() {
        // Verify "Boston" is displayed
        onView(withText("Boston"))
                .check(matches(isDisplayed()));

        // Click the "MAP" button for "Boston"
        String mapButtonTag = "map_button_Boston";
        onView(withTagValue(Matchers.<Object>equalTo(mapButtonTag)))
                .perform(click());

        // Wait for the map activity to load
        try {
            Thread.sleep(2000); // Pause to allow map to load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that the map activity is displayed by checking for city name
        onView(withId(R.id.cityNameTextView))
                .check(matches(withText("Boston")));

        // Verify that the map view is displayed
        onView(withId(R.id.mapView))
                .check(matches(isDisplayed()));
    }
}