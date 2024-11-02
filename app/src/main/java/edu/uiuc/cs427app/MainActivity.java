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

    FirebaseAuth auth;
    FirebaseUser user;
    TextView userDetails;

    Button logoutBtn;

    Switch themeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth= FirebaseAuth.getInstance();
        logoutBtn = findViewById(R.id.logout_btn);
        userDetails = findViewById(R.id.user_details);
        themeSwitch = findViewById(R.id.themeSwitch);

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Displays user email
            loadCitiesFromServer(user.getEmail());
            String teamNumber = getString(R.string.app_name);
            getSupportActionBar().setTitle(teamNumber + " - " + user.getEmail());
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
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
        AppCompatDelegate
                .setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Initializing the UI components
        // The list of locations should be customized per user (change the implementation so that
        // buttons are added to layout programmatically

        cityButtonMap = new HashMap<>();
        // Find the container for the city list
        cityListContainer = findViewById(R.id.cityListContainer);

        // Example JSON response
        // String jsonResponse = "[\"Champaign\", \"Chicago\", \"New York\"]";
        // updateCityList(jsonResponse);

        Button buttonNew = findViewById(R.id.buttonAddLocation);
        buttonNew.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
        switch (id) {
            case R.id.buttonAddLocation:
                // Implement this action to add a new location to the list of locations
                intent = new Intent(MainActivity.this, AddLocation.class);
                startActivity(intent);
                break;
            default:
                if (!cityButtonMap.containsKey(id)) {
                    Toast.makeText(MainActivity.this, "Error: unknown id", Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(this, DetailsActivity.class);
                    intent.putExtra("city", cityButtonMap.get(id));
                    startActivity(intent);
                }
                break;
        }
    }

    // TODO: Call this function to fetch list of cities from users & render them in onCreate()
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
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to load cities.", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateCityList(String jsonCityList) {
        // Parse jsonCityList (as an array of city names)
        // Update your UI elements to display the list of cities
        // For example, you could dynamically add TextViews to a LinearLayout
        try {
            cityListContainer.removeAllViews();
            cityButtonMap.clear(); // Clear previous mappings

            JSONArray cityArray = new JSONArray(jsonCityList);

            for (int i = 0; i < cityArray.length(); i++) {
                String cityName = cityArray.getString(i);

                LinearLayout cityLayout = new LinearLayout(this);
                cityLayout.setOrientation(LinearLayout.HORIZONTAL);
                cityLayout.setPadding(8, 8, 8, 8);

                TextView cityTextView = new TextView(this);
                cityTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                cityTextView.setText(cityName);
                cityTextView.setTextSize(16);

                Button detailsButton = new Button(new ContextThemeWrapper(this, R.style.Theme_MyFirstApp));
                detailsButton.setText("Show Details");
                detailsButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                int buttonId = View.generateViewId(); // Generate a unique ID
                detailsButton.setId(buttonId);

                // Add the button ID and city name to the HashMap
                cityButtonMap.put(buttonId, cityName);

                // Set OnClickListener for the button
                detailsButton.setOnClickListener(this);

                cityLayout.addView(cityTextView);
                cityLayout.addView(detailsButton);
                cityListContainer.addView(cityLayout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}

