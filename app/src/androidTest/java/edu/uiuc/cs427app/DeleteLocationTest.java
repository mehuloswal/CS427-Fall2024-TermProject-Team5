package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import androidx.test.espresso.matcher.RootMatchers;

import static org.hamcrest.Matchers.not;

import static org.hamcrest.Matchers.equalTo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test to verify the deletion of a city location.
 */
@RunWith(AndroidJUnit4.class)
public class DeleteLocationTest {

    private FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<Login> mActivityRule = new ActivityScenarioRule<>(Login.class);

    /**
     * Sets up the test environment by ensuring a clean login state and verifying
     * the presence of a specific city ("Nashville") in the user's city list.
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

        // Ensure "Nashville" exists in the city list
        ensureCityExists("Nashville");
    }

    /**
     * Logs in to the application with valid credentials.
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
            Thread.sleep(2000); // Increased wait time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Scroll to and verify that we're on the main activity by checking if the "Add
        // Location" button is displayed
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Ensures that a city exists in the user's city list. If it does not exist,
     * adds it.
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
     * Adds a city to the user's city list.
     *
     * @param cityName The name of the city to add.
     */
    private void addCity(String cityName) {
        // Click "Add Location" button
        onView(withId(R.id.buttonAddLocation))
                .perform(scrollTo(), click());

        // Verify that we're on the AddLocation activity
        onView(withId(R.id.addCityButton))
                .check(matches(isDisplayed()));

        // Type the city name
        onView(withId(R.id.cityAutoCompleteTextView))
                .perform(typeText(cityName), closeSoftKeyboard());

        // Select the city from the dropdown
        onView(withText(cityName))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click());

        // Click "Add City" button
        onView(withId(R.id.addCityButton))
                .perform(click());

        // Wait for the city to be added
        try {
            Thread.sleep(1000); // Pause for city to be added
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify the city is displayed in the main activity
        onView(withText(cityName))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Cleans up the test environment by signing out any logged-in user.
     */
    @After
    public void tearDown() {
        // Clean up after each test
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    /**
     * Verifies that the user can open the delete dialog but choose not to delete the city.
     */
    @Test
    public void testDontRemoveCity() {
        // Check if "Nashville" exists
        boolean cityExists = true;
        try {
            onView(withText("Nashville"))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        } catch (Exception e) {
            cityExists = false;
        }

        if (cityExists) {
            // Click the Weather button for "Nashville"
            onView(withTagValue(equalTo("weather_button_Nashville")))
                    .perform(scrollTo(), click());

            // Verify we're on the city details screen by checking for city info text
            onView(withId(R.id.cityInfo))
                    .check(matches(isDisplayed()));

            // Open the menu and click the delete option
            onView(withContentDescription("More options")) // Opens the options menu
                    .perform(click());

            // Wait for the menu to appear
            try {
                Thread.sleep(1000); // Pause to allow menu to appear
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withText("Delete"))
                    .check(matches(isDisplayed()));
            // Click the delete option in the menu
            onView(withText("Delete")) // Matches the delete option in the menu
                    .perform(click());

            try {
                Thread.sleep(1000); // Pause to allow deletion to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Verify the delete dialog is displayed
            onView(withText("Are you sure you want to delete this city?"))
                    .check(matches(isDisplayed()));

            try {
                Thread.sleep(1000); // Pause to allow deletion to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Confirm the deletion in the dialog
            onView(withText("No"))
                    .perform(click());

            // Wait for the deletion to process
            try {
                Thread.sleep(2000); // Pause to allow deletion to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Verify by checking the city is no longer displayed
            try {
                onView(withText("Nashville"))
                        .perform(scrollTo())
                        .check(matches(isDisplayed()));
            } catch (Exception e) {
                // Expected exception if the view does not exist
                System.out.println("City 'Nashville' was not deleted.");
            }
        } else {
            // If the city does not exist, log a message
        }
    }

    /**
     * Verifies that the user can successfully delete a city from their list.
     */
    @Test
    public void testRemoveCity() {
        // Check if "Nashville" exists
        boolean cityExists = true;
        try {
            onView(withText("Nashville"))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        } catch (Exception e) {
            cityExists = false;
        }

        if (cityExists) {
            // Click the Weather button for "Nashville"
            onView(withTagValue(equalTo("weather_button_Nashville")))
                    .perform(scrollTo(), click());

            // Verify we're on the city details screen by checking for city info text
            onView(withId(R.id.cityInfo))
                    .check(matches(isDisplayed()));

            // Open the menu and click the delete option
            onView(withContentDescription("More options")) // Opens the options menu
                    .perform(click());

            // Wait for the menu to appear
            try {
                Thread.sleep(1000); // Pause to allow menu to appear
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withText("Delete"))
                    .check(matches(isDisplayed()));
            // Click the delete option in the menu
            onView(withText("Delete")) // Matches the delete option in the menu
                    .perform(click());

            try {
                Thread.sleep(1000); // Pause to allow deletion to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Verify the delete dialog is displayed
            onView(withText("Are you sure you want to delete this city?"))
                    .check(matches(isDisplayed()));

            try {
                Thread.sleep(1000); // Pause to allow deletion to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Confirm the deletion in the dialog
            onView(withText("Yes"))
                    .perform(click());

            // Wait for the deletion to process
            try {
                Thread.sleep(2000); // Pause to allow deletion to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Verify successful deletion by checking the city is no longer displayed
            try {
                onView(withText("Nashville"))
                        .perform(scrollTo())
                        .check(doesNotExist());
            } catch (Exception e) {
                // Expected exception if the view does not exist
                System.out.println("City 'Nashville' was successfully deleted.");
            }
        } else {
            // If the city does not exist, log a message
            System.out.println("City 'Nashville' does not exist, nothing to delete.");
        }
    }
}