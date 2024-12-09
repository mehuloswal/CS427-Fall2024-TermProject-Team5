package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import androidx.test.espresso.matcher.RootMatchers;

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
 * Instrumented test to verify the weather feature for "Boston".
 * This test ensures that the user can view weather-related information
 * for the city of Boston and that all expected UI components are displayed.
 */
@RunWith(AndroidJUnit4.class)
public class WeatherFeatureBostonTest {

        // Firebase authentication instance for managing login/logout
        private FirebaseAuth mAuth;

        // Rule to launch the Login activity for the test
        @Rule
        public ActivityScenarioRule<Login> mActivityRule = new ActivityScenarioRule<>(Login.class);

        /**
         * Set up the test environment.
         * Ensures the user is logged out initially, logs in with valid credentials,
         * and ensures that "Boston" is present in the list of cities.
         */
        @Before
        public void setUp() {
                // Initialize Firebase authentication
                mAuth = FirebaseAuth.getInstance();

                // Ensure the user is logged out before starting the test
                if (mAuth.getCurrentUser() != null) {
                        mAuth.signOut();
                }

                // Perform user login
                performLogin();

                // Ensure the city "Boston" exists in the city list
                ensureCityExists("Boston");
        }

        /**
         * Logs the user in by interacting with the login screen.
         */
        private void performLogin() {
                // Launch the Login activity
                ActivityScenario.launch(Login.class);

                // Input username and close the keyboard
                onView(withId(R.id.username))
                                .perform(typeText("ruipeng2"), closeSoftKeyboard());
                sleep(500); // Pause to simulate real-time interaction

                // Input password and close the keyboard
                onView(withId(R.id.password))
                                .perform(typeText("123456"), closeSoftKeyboard());
                sleep(500);

                // Click the login button
                onView(withId(R.id.btn_login))
                                .perform(click());
                sleep(2000);

                // Scroll to the "Add Location" button and verify it's displayed
                onView(withId(R.id.buttonAddLocation))
                                .perform(scrollTo())
                                .check(matches(isDisplayed()));
        }

        /**
         * Ensures the specified city exists in the list of cities.
         * If the city is not found, it is added to the list.
         *
         * @param cityName Name of the city to ensure in the list
         */
        private void ensureCityExists(String cityName) {
                try {
                        // Check if the city is already displayed
                        onView(withText(cityName))
                                        .perform(scrollTo())
                                        .check(matches(isDisplayed()));
                } catch (Exception e) {
                        // If the city is not found, add it
                        addCity(cityName);
                }
        }

        /**
         * Adds a city to the list by interacting with the "Add Location" screen.
         *
         * @param cityName Name of the city to add
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
         * Cleans up after the test by signing out the user if logged in.
         */
        @After
        public void tearDown() {
                if (mAuth.getCurrentUser() != null) {
                        mAuth.signOut();
                }
        }

        /**
         * Test to verify the weather feature for "Boston".
         * Checks that all relevant UI components related to weather information are
         * displayed correctly.
         */
        @Test
        public void testWeatherFeatureBoston() {
                // Verify that "Boston" is displayed in the city list
                onView(withText("Boston"))
                                .perform(scrollTo())
                                .check(matches(isDisplayed()));

                // Click the weather button for "Boston"
                onView(withTagValue(equalTo("weather_button_Boston")))
                                .perform(scrollTo(), click());
                sleep(1000);

                // Verify all weather-related UI components are displayed
                onView(withId(R.id.welcomeText))
                                .check(matches(isDisplayed()));
                sleep(500);

                onView(withId(R.id.cityInfo))
                                .check(matches(isDisplayed()));
                sleep(500);

                onView(withId(R.id.temperature))
                                .check(matches(isDisplayed()));
                sleep(500);

                onView(withId(R.id.weather))
                                .check(matches(isDisplayed()));
                sleep(500);

                onView(withId(R.id.humidity))
                                .check(matches(isDisplayed()));
                sleep(500);

                onView(withId(R.id.wind))
                                .check(matches(isDisplayed()));
                sleep(500);

                onView(withId(R.id.weatherInsightsButton))
                                .check(matches(isDisplayed()));
        }

        /**
         * Helper method to safely pause test execution.
         *
         * @param milliseconds Time to pause in milliseconds
         */
        private void sleep(int milliseconds) {
                try {
                        Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
        }
}