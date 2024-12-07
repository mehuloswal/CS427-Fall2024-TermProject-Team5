package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

import static org.hamcrest.Matchers.equalTo;

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
 * Instrumented test to verify the map feature for "Chicago".
 */
@RunWith(AndroidJUnit4.class)
public class LocationFeatureChicagoTest {

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
        // Ensure "Chicago" is in the city list
        ensureCityExists("Chicago");
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
            Thread.sleep(2000); // Increased wait time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Scroll to and verify main activity is displayed
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo())
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
                    .perform(scrollTo())
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
                .perform(scrollTo(), click());

        // Verify that we're on the AddLocation activity
        onView(withId(R.id.addCityButton))
                .perform(scrollTo())
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
                .perform(scrollTo())
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
     * Test the map feature for "Chicago".
     * Includes actions and assertions for testing the map functionality.
     */
    @Test
    public void testMapFeatureChicago() {
        // Verify "Chicago" is displayed
        onView(withText("Chicago"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        // Locate the parent layout containing the city name and its buttons
        onView(allOf(
                hasDescendant(withText("Chicago")),
                isDescendantOfA(withId(R.id.cityListContainer))))
                .check(matches(isDisplayed()));

        // Click the "MAP" button within the same layout as "Chicago"
        onView(allOf(
                withText("Map"), // Text of the "Map" button
                isDescendantOfA(allOf(
                        hasDescendant(withText("Chicago")), // Ensure it's in the layout containing "Chicago"
                        isDescendantOfA(withId(R.id.cityListContainer))))))
                .perform(scrollTo(), click());

        // Wait for the map activity to load
        try {
            Thread.sleep(2000); // Pause to allow map to load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that the map activity is displayed by checking for city name
        onView(withId(R.id.cityNameTextView))
                .check(matches(withText("Chicago")));

        // Verify that the map view is displayed
        onView(withId(R.id.mapView))
                .check(matches(isDisplayed()));

        // Dynamically verify the coordinates are displayed in the expected format
        onView(withId(R.id.coordinatesTextView))
                .check(matches(withText(Matchers.startsWith("Latitude: "))))
                .check(matches(withText(Matchers.containsString("Longitude: "))));
    }
}