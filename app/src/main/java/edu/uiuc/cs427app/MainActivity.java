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

    FirebaseAuth auth;
    FirebaseUser user;
    Button logoutBtn;

    Switch themeSwitch;

    /**
     * States initialization in the MainActivity
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     *
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
            // fetch user preference
//            applyThemeFromPreferences();
            loadCitiesFromServer(user.getEmail());
            String teamNumber = getString(R.string.app_name);
            getSupportActionBar().setTitle(teamNumber + " - " + user.getEmail().split("@")[0]);
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Logs user out and redirects user to Login page
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

        // Set up the theme switch feature
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                // Switches theme
                SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                editor.putBoolean("night_mode", isChecked);
                editor.apply();
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Initializing the UI components
        cityButtonMap = new HashMap<>();
        mapButtonMap = new HashMap<>();
        // Find the container for the city list
        cityListContainer = findViewById(R.id.cityListContainer);

        Button buttonNew = findViewById(R.id.buttonAddLocation);
        buttonNew.setOnClickListener(this);
    }

    /**
     * The logistic control for the Details button for each added city on the layout
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
            intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("city", cityButtonMap.get(id));
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
     * Fetch the list of cities from the backend server via RESTful APIs
     *
     * @param userEmail The logged in user's email
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
     * This function parses jsonCityList (as an array of city names)
     *
     * @param jsonCityList The list of cities in Json string format
     */
    private void updateCityList(String jsonCityList) {
        try {
            cityListContainer.removeAllViews();
            cityButtonMap.clear();
            mapButtonMap.clear();

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
                detailsButton.setText("Show Details");
                detailsButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                int detailsButtonId = View.generateViewId();
                detailsButton.setId(detailsButtonId);
                cityButtonMap.put(detailsButtonId, cityName);
                detailsButton.setOnClickListener(this);

                // Map Button
                Button mapButton = new Button(new ContextThemeWrapper(this, R.style.Theme_MyFirstApp));
                mapButton.setText("Show Map");
                mapButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                int mapButtonId = View.generateViewId();
                mapButton.setId(mapButtonId);
                CityData cityData = new CityData(cityName, latitude, longitude);
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

    // Helper class to store city data
    private class CityData {
        String cityName;
        double latitude;
        double longitude;

        CityData(String cityName, double latitude, double longitude) {
            this.cityName = cityName;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Applies the theme mode based on the saved user preference.
     * Retrieves the theme mode preference from SharedPreferences,
     * and sets the app's night mode accordingly.
     * If "night_mode" is set to true in SharedPreferences, the app
     * will switch to dark mode. Otherwise, it will remain in light mode.
     */
    private void applyThemeFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        AppCompatDelegate
                .setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

}
