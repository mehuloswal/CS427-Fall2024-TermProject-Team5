package edu.uiuc.cs427app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.ui.AppBarConfiguration;

import edu.uiuc.cs427app.databinding.ActivityMainBinding;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uiuc.cs427app.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private LinearLayout cityListContainer;
    // Maps Button ID to its Name
    private HashMap<Integer, String> cityButtonMap;

    private HashMap<Integer, CityData> mapButtonMap;

    private HashMap<Integer, CityData> weatherButtonMap;

    FirebaseAuth auth;
    FirebaseUser user;
    Button logoutBtn;

    Switch themeSwitch;

    /**
     * Initializes the main activity, including authentication, theme settings, and
     * UI setup.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down,
     *                           this Bundle contains the data it most recently
     *                           supplied in {@link #onSaveInstanceState}.
     *                           Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        logoutBtn = findViewById(R.id.logout_btn);
        themeSwitch = findViewById(R.id.themeSwitch);

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            loadCitiesFromServer(user.getEmail());
            String teamNumber = getString(R.string.app_name);
            getSupportActionBar().setTitle(teamNumber + " - " + user.getEmail().split("@")[0]);
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {

            /**
             * Logs out the user and redirects them to the login page.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Fetch user preference and set up the theme switch feature
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                // Switches theme
                SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                editor.putBoolean("night_mode", isChecked);
                editor.apply();
                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Initializing the UI components
        cityButtonMap = new HashMap<>();
        mapButtonMap = new HashMap<>();
        weatherButtonMap = new HashMap<>();

        // Find the container for the city list
        cityListContainer = findViewById(R.id.cityListContainer);

        Button buttonNew = findViewById(R.id.buttonAddLocation);
        buttonNew.setOnClickListener(this);
    }

    /**
     * Handles click events on buttons in the main activity.
     * Navigates to different activities for adding locations, showing city details,
     * or displaying maps.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
        if (id == R.id.buttonAddLocation) {
            intent = new Intent(MainActivity.this, AddLocation.class);
            startActivity(intent);
        } else if (cityButtonMap.containsKey(id)) {
            // Details button clicked
            CityData cityData = weatherButtonMap.get(id);
            intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("city", cityButtonMap.get(id));
            intent.putExtra("cityName", cityData.cityName);
            intent.putExtra("latitude", cityData.latitude);
            intent.putExtra("longitude", cityData.longitude);
            startActivity(intent);
        } else if (mapButtonMap.containsKey(id)) {
            // Map button clicked
            CityData cityData = mapButtonMap.get(id);
            intent = new Intent(this, MapActivity.class);
            intent.putExtra("cityName", cityData.cityName);
            intent.putExtra("latitude", cityData.latitude);
            intent.putExtra("longitude", cityData.longitude);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "Error: unknown id", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches the list of cities from the backend server using a REST API.
     *
     * @param userEmail The email address of the logged-in user.
     */
    private void loadCitiesFromServer(String userEmail) {
        new Thread(() -> {
            try {
                URL url = new URL(Config.API_URL + "getCity?userEmail=" + userEmail);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response (pseudo code - adapt based on actual response structure)
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // Update UI with cities (parse response to extract city names)
                    runOnUiThread(() -> {
                        // Assume response is a JSON array of city names
                        updateCityList(response.toString());
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to load cities.", Toast.LENGTH_SHORT)
                            .show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(
                        () -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Dynamically and programmatically updates the list of cities in the
     * MainActivity layout
     * This function parses jsonCityList (as an array of city names) to extract city
     * details, creates corresponding UI elements,
     * and sets up buttons for additional interactions (details and maps).
     * 
     * @param jsonCityList The list of cities in Json string format, expected to
     *                     contain city names,
     *                     latitudes, and longitudes
     */
    private void updateCityList(String jsonCityList) {
        try {
            cityListContainer.removeAllViews();
            // Clears all existing mappings from both the city and map button maps.
            cityButtonMap.clear();
            mapButtonMap.clear();
            weatherButtonMap.clear();

            JSONArray cityArray = new JSONArray(jsonCityList);
            for (int i = 0; i < cityArray.length(); i++) {
                JSONObject cityObject = cityArray.getJSONObject(i);
                String cityName = cityObject.getString("city_name");
                double latitude = cityObject.getDouble("latitude");
                double longitude = cityObject.getDouble("longitude");

                LinearLayout cityLayout = new LinearLayout(this);
                cityLayout.setOrientation(LinearLayout.HORIZONTAL);
                cityLayout.setPadding(8, 8, 8, 8);

                TextView cityTextView = new TextView(this);
                cityTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                cityTextView.setText(cityName);
                cityTextView.setTextSize(16);

                // Details Button
                Button detailsButton = new Button(new ContextThemeWrapper(this, R.style.Theme_MyFirstApp));
                detailsButton.setText("Weather");
                detailsButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                int detailsButtonId = View.generateViewId();
                detailsButton.setId(detailsButtonId);
                cityButtonMap.put(detailsButtonId, cityName);
                CityData cityData = new CityData(cityName, latitude, longitude);
                weatherButtonMap.put(detailsButtonId, cityData);
                detailsButton.setOnClickListener(this);
                detailsButton.setTag("weather_button_" + cityName);


                // Map Button
                Button mapButton = new Button(new ContextThemeWrapper(this, R.style.Theme_MyFirstApp));
                mapButton.setText("Map");
                mapButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                int mapButtonId = View.generateViewId();
                mapButton.setId(mapButtonId);
                cityData = new CityData(cityName, latitude, longitude);
                mapButtonMap.put(mapButtonId, cityData);
                mapButton.setOnClickListener(this);

                cityLayout.addView(cityTextView);
                cityLayout.addView(detailsButton);
                cityLayout.addView(mapButton);

                cityListContainer.addView(cityLayout);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper class to store city data including the name and geographical
     * coordinates.
     * This class encapsulates the properties of a city required for the
     * application's functionality,
     * such as displaying details in various UI components or calculating distances.
     */
    private class CityData {
        String cityName;
        double latitude;
        double longitude;

        /**
         * Constructs a new CityData object.
         *
         * @param cityName  The name of the city.
         * @param latitude  The latitude coordinate of the city.
         * @param longitude The longitude coordinate of the city.
         */
        CityData(String cityName, double latitude, double longitude) {
            this.cityName = cityName;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

}
