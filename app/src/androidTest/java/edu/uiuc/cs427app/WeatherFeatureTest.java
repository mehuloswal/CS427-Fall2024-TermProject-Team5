package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class WeatherFeatureTest {
    private FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<DetailsActivity> mActivityRule =
            new ActivityScenarioRule<>(DetailsActivity.class);

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

    @After
    public void tearDown() {
        // Clean up after each test
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    @Test
    public void testDetailsActivityForDetroit() {
        // Locate and click the first weather button using its tag
        onView(withTagValue(equalTo("weather_button_Detroit"))).perform(click());

        // Verify welcome text display
        onView(withId(R.id.welcomeText))
                .check(matches(isDisplayed()));

        // Verify city info display
        onView(withId(R.id.cityInfo))
                .check(matches(isDisplayed()));

        // Verify temperature display
        onView(withId(R.id.temperature))
                .check(matches(isDisplayed()));

        // Verify weather condition
        onView(withId(R.id.weather))
                .check(matches(isDisplayed()));

        // Verify humidity information
        onView(withId(R.id.humidity))
                .check(matches(isDisplayed()));

        // Verify wind information
        onView(withId(R.id.wind))
                .check(matches(isDisplayed()));

        // Verify weather insights button is present
        onView(withId(R.id.weatherInsightsButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDetailsActivityForBoston() {
        // Locate and click the second weather button using its tag
        onView(withTagValue(equalTo("weather_button_Boston"))).perform(click());

        // Verify welcome text display
        onView(withId(R.id.welcomeText))
                .check(matches(isDisplayed()));

        // Verify city info display
        onView(withId(R.id.cityInfo))
                .check(matches(isDisplayed()));

        // Verify temperature display
        onView(withId(R.id.temperature))
                .check(matches(isDisplayed()));

        // Verify weather condition
        onView(withId(R.id.weather))
                .check(matches(isDisplayed()));

        // Verify humidity information
        onView(withId(R.id.humidity))
                .check(matches(isDisplayed()));

        // Verify wind information
        onView(withId(R.id.wind))
                .check(matches(isDisplayed()));

        // Verify weather insights button is present
        onView(withId(R.id.weatherInsightsButton))
                .check(matches(isDisplayed()));
    }
}