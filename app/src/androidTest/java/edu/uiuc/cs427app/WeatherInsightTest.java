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
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static org.hamcrest.Matchers.equalTo;

import static org.hamcrest.Matchers.startsWith;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;


/**
 * UI tests for the Weather Insights feature in the application.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WeatherInsightTest {
    private FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<WeatherInsightsActivity> mActivityRule =
            new ActivityScenarioRule<>(WeatherInsightsActivity.class);

    /**
     * Sets up the test environment by ensuring a clean login state.
     * If a user is logged in, it signs out and then performs login.
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
     * Helper method to log in to the application before tests.
     * Ensures the user is logged in by providing valid credentials.
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
     * Verifies that dynamically generated weather-related questions
     * are displayed as buttons in the Weather Insights page.
     */
    @Test
    public void testGeneratedQuestionsDisplayedAsButtons() {
        // Step 1: Locate and click the first weather button using its tag
        onView(withTagValue(equalTo("weather_button_Detroit"))).perform(click());

        // Step 2: Locate and click the "Weather Insights" button in the Details page
        onView(withId(R.id.weatherInsightsButton)).perform(click());

        // Simulate that questions are dynamically added
        onView(withId(R.id.questionContainer)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that clicking a dynamically generated question button
     * triggers the generation of an appropriate response.
     */
    @Test
    public void testClickQuestionButtonTriggersAnswerGeneration() {
        // Step 1: Navigate to the Weather Insights page
        onView(withTagValue(equalTo("weather_button_Detroit"))).perform(click());
        onView(withId(R.id.weatherInsightsButton)).perform(click());

        // Step 2: Wait for the questions to load
        try {
            Thread.sleep(2000); // Replace with IdlingResource if possible
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Step 3: Click the dynamically generated question button
        onView(allOf(
                isDescendantOfA(withId(R.id.questionContainer)),
                withText(startsWith("Should I wear"))
        )).perform(click());

        try {
            Thread.sleep(3000); // Replace with IdlingResource if possible
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Step 4: Verify the response text is displayed
        onView(withId(R.id.responseText)).check(matches(isDisplayed()));
    }
}
