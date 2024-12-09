package edu.uiuc.cs427app;

import edu.uiuc.cs427app.Config;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AddLocation extends AppCompatActivity {

    private AutoCompleteTextView cityAutoCompleteTextView;
    private Button addCityButton;
    private List<String> cityList = new ArrayList<>(); // List to hold city names

    /**
     * Initializes the activity, sets up the UI elements, loads available cities
     * from the server,
     * and configures the button click event to handle city addition.
     *
     * @param savedInstanceState Bundle object containing the activity's previously
     *                           saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        cityAutoCompleteTextView = findViewById(R.id.cityAutoCompleteTextView);
        addCityButton = findViewById(R.id.addCityButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String teamNumber = getString(R.string.app_name);
        getSupportActionBar().setTitle(teamNumber + " - " + user.getEmail().split("@")[0]);

        // Load cities from server for dropdown
        loadCitiesFromServer();

        addCityButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for the "Add City" button.
             * Retrieves the selected city name and user email, then calls addCityToServer
             * if valid.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                String cityName = cityAutoCompleteTextView.getText().toString().trim();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null && !cityName.isEmpty()) {
                    String userEmail = user.getEmail();
                    addCityToServer(cityName, userEmail);
                } else {
                    Toast.makeText(AddLocation.this, "Please enter or select a city name.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Fetches the list of available cities from the server and updates the
     * autocomplete dropdown.
     * Runs a network operation on a background thread to get city data.
     * On successful retrieval, updates the city list UI in the main thread.
     */
    private void loadCitiesFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL(Config.API_URL + "cities"); // Using 10.0.2.2 for local development
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        cityList.add(jsonArray.getString(i));
                    }

                    // Update UI on the main thread
                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddLocation.this,
                                android.R.layout.simple_dropdown_item_1line, cityList);
                        cityAutoCompleteTextView.setAdapter(adapter);
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast
                        .makeText(AddLocation.this, "Error loading cities: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show());
            }
        }).start();
    }

    /**
     * Sends a request to add a selected city for the logged-in user to the server.
     * On success, returns to the main activity and displays a success message.
     * If unsuccessful, displays an error message to the user.
     *
     * @param cityName  The name of the city to add.
     * @param userEmail The email of the user adding the city.
     */
    private void addCityToServer(String cityName, String userEmail) {
        new Thread(() -> {
            try {
                // First, get the user's existing cities
                URL getUrl = new URL(Config.API_URL + "getCity?userEmail=" + userEmail);
                HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
                getConn.setRequestMethod("GET");

                int getResponseCode = getConn.getResponseCode();
                if (getResponseCode == HttpURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
                    StringBuilder getResponse = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        getResponse.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JSONArray jsonArray = new JSONArray(getResponse.toString());
                    boolean cityExists = false;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject cityObject = jsonArray.getJSONObject(i);
                        String existingCityName = cityObject.getString("city_name");
                        if (existingCityName.equalsIgnoreCase(cityName)) {
                            cityExists = true;
                            break;
                        }
                    }

                    if (cityExists) {
                        // City already exists, show message
                        runOnUiThread(() -> {
                            Toast.makeText(AddLocation.this, "City already exists.", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // City does not exist, proceed to add
                        URL url = new URL(Config.API_URL + "addCity");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

                        // Creating JSON object to send in request
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("cityName", cityName);
                        jsonParam.put("userEmail", userEmail);

                        // Sending JSON data
                        try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                            writer.write(jsonParam.toString());
                            writer.flush();
                        }

                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            runOnUiThread(() -> {
                                Toast.makeText(AddLocation.this, "City added successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddLocation.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> Toast
                                    .makeText(AddLocation.this, "Failed to add city.", Toast.LENGTH_SHORT).show());
                        }
                        conn.disconnect();
                    }
                } else {
                    // Failed to get existing cities
                    runOnUiThread(() -> Toast
                            .makeText(AddLocation.this, "Failed to fetch existing cities.", Toast.LENGTH_SHORT).show());
                }
                getConn.disconnect();
            } catch (Exception e) {
                runOnUiThread(
                        () -> Toast.makeText(AddLocation.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
