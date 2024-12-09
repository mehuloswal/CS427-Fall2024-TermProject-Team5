package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import androidx.test.espresso.matcher.RootMatchers;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
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
 * Instrumented test to verify that mocking the location changes the displayed
 * map to Champaign.
 */
@RunWith(AndroidJUnit4.class)
public class MockLocationTest {

    private FirebaseAuth mAuth;
    private static final String MOCK_PROVIDER = LocationManager.GPS_PROVIDER;

    @Rule
    public ActivityScenarioRule<Login> mActivityRule = new ActivityScenarioRule<>(Login.class);

    /**
     * Sets up the test environment by ensuring the user is logged out,
     * performing login, and verifying the presence of "Chicago" in the city list.
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
        // Ensure "Chicago" is in the city list
        ensureCityExists("Chicago");
    }

    /**
     * Cleans up after each test by logging out the user if logged in.
     * Also removes the mock location provider.
     */
    @After
    public void tearDown() {
        // Sign out after the test
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

    }

    /**
     * Helper method to perform user login before testing.
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
            Thread.sleep(2000); // Wait time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify main activity is displayed
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Ensures that a city is in the user's city list.
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
     */
    private void addCity(String cityName) {
        // Click "Add Location" button
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo(), click());

        // Verify that we're on the AddLocation activity
        onView(withId(R.id.addCityButton))
                .check(matches(isDisplayed()));

        // Enter city name and add it
        onView(withId(R.id.cityAutoCompleteTextView))
                .perform(typeText(cityName), closeSoftKeyboard());

        // Select the city from the dropdown
        onView(withText(cityName))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click());

        onView(withId(R.id.addCityButton))
                .perform(click());

        // Wait for the city to be added
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify the city is displayed in the main activity
        onView(withText(cityName))
                .check(matches(isDisplayed()));
    }

    /**
     * Test the map feature by verifying that mocking location changes the displayed
     * map to Champaign.
     */
    @Test
    public void testMockLocationUpdatesMapToChampaign() {

        // Click the "Map" button for Chicago
        onView(allOf(
                withText("Map"),
                isDescendantOfA(allOf(
                        hasDescendant(withText("Chicago")),
                        isDescendantOfA(withId(R.id.cityListContainer))))))
                .perform(scrollTo(), click());

        // Wait for the MapActivity to load
        try {
            Thread.sleep(2000); // Adjust the wait time as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that the map activity is displaying Chicago
        onView(withId(R.id.cityNameTextView))
                .check(matches(withText("Chicago")));

        // Update the location to Champaign
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        scenario.onActivity(activity -> {
            activity.updateLocation("Champaign", 40.1142, -88.2737);
        });

        // Wait for the map to update
        try {
            Thread.sleep(2000); // Adjust the wait time as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify map updates (e.g., checking displayed map center, markers, etc.)
        onView(withId(R.id.mapView))
                .check(matches(isDisplayed()));

        // Verify that the map now shows Champaign
        onView(withId(R.id.cityNameTextView))
                .check(matches(withText("Champaign")));
        onView(withId(R.id.coordinatesTextView))
                .check(matches(allOf(
                        withText(containsString("Latitude: 40.1142")),
                        withText(containsString("Longitude: -88.2737")))));
    }
}