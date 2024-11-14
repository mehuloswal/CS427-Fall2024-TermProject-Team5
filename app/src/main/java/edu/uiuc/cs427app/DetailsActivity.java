package edu.uiuc.cs427app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.URL;

public class DetailsActivity extends AppCompatActivity {

    private String cityName;
    private FirebaseUser user;
    private double latitude;
    private double longitude;
    private TextView temperatureView, weatherView, humidityView, windView;

    /**
     * Initializes the DetailsActivity, setting up UI elements to display city information,
     * and fetches the city name from the intent and current Firebase user information.
     *
     * @param savedInstanceState Bundle object containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Fetching city name from intent and Firebase user information
        cityName = getIntent().getStringExtra("city");
        user = FirebaseAuth.getInstance().getCurrentUser();
        // Retrieve data from Intent
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        // Initialize UI elements
        TextView welcomeMessage = findViewById(R.id.welcomeText);
        TextView cityInfoMessage = findViewById(R.id.cityInfo);
        temperatureView = findViewById(R.id.temperature);
        weatherView = findViewById(R.id.weather);
        humidityView = findViewById(R.id.humidity);
        windView = findViewById(R.id.wind);

        // Display city information
        welcomeMessage.setText("Welcome to " + cityName + "!");
        cityInfoMessage.setText("Detailed information about the weather of " + cityName);

        // TODO: FINISH THIS! assigned to @zexin and @wenqi
        // Load weather details
        // loadWeatherDetails();
        new FetchWeatherTask().execute();
        // Set up the "Weather Insights" button
        Button weatherInsightsButton = findViewById(R.id.weatherInsightsButton);
        weatherInsightsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, WeatherInsightsActivity.class);
                intent.putExtra("city", cityName);
                // TODO: We need to gather the weatherData from these views and pass it on to the insight page
                // TODO: @zexin and @wenqi
                // Zexin: Done this at line 71
                // intent.putExtra("weatherData", cityName);
                startActivity(intent);
            }
        });
    }

    /**
     * Creates the options menu for the activity.
     * Inflates the menu resource containing the delete option.
     *
     * @param menu The options menu in which items are placed
     * @return true for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    /**
     * Handles selection of menu items in the options menu.
     * Currently handles the delete action for removing a city.
     *
     * @param item The menu item that was selected
     * @return boolean Return false to allow normal menu processing to proceed,
     *         true to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays a confirmation dialog before deleting a city.
     * Shows an AlertDialog with yes/no options to confirm deletion.
     * If user confirms, proceeds with city deletion.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete City")
                .setMessage("Are you sure you want to delete this city?")
                .setPositiveButton("Yes", (dialog, which) -> deleteCity())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Handles the deletion of a city from the user's list.
     * Makes an HTTP DELETE request to the backend server to remove the city.
     * If successful, returns to the MainActivity.
     *
     * The method:
     * 1. Verifies user and city data are available
     * 2. Makes an HTTP request to remove the city
     * 3. Handles the response and shows appropriate feedback
     * 4. Returns to MainActivity on successful deletion
     *
     * @throws Exception If there's an error in the network communication
     *                   or JSON processing
     */
    private void deleteCity() {
        if (user != null && cityName != null) {
            String userEmail = user.getEmail();

            new Thread(() -> {
                try {
                    // URL for the delete city API
                    URL url = new URL(Config.API_URL + "removeCity");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("DELETE");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // Creating JSON object with city name and user email
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
                            Toast.makeText(DetailsActivity.this, "City deleted successfully!", Toast.LENGTH_SHORT)
                                    .show();
                            Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast
                                .makeText(DetailsActivity.this, "Failed to delete city.", Toast.LENGTH_SHORT).show());
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    runOnUiThread(() -> Toast
                            .makeText(DetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        } else {
            Toast.makeText(this, "User or city data missing.", Toast.LENGTH_SHORT).show();
        }
    }
    private class FetchWeatherTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                String urlString = Config.API_URL + "getWeather?lat=" + latitude + "&lon=" + longitude;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    return new JSONObject(response.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject weatherData) {
            if (weatherData != null) {
                try {
                    // Extract and display weather data
                    String weatherDescription = weatherData.getString("weather");
                    double temperature = weatherData.getDouble("temperature");
                    int humidity = weatherData.getInt("humidity");
                    double windSpeed = weatherData.getDouble("wind_speed");
                    // Update UI
                    weatherView.setText("Weather: " + weatherDescription);
                    temperatureView.setText("Temperature: " + temperature + "°C");
                    humidityView.setText("Humidity: " + humidity + "%");
                    windView.setText("Wind Speed: " + windSpeed + " m/s");
                } catch (Exception e) {
                    Toast.makeText(DetailsActivity.this, "Error parsing weather data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DetailsActivity.this, "Failed to fetch weather data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * TODO: THIS FUNCTION IS NOT TESTED NOR USED!  @zexing and @wenqi need to check this
     * Loads weather details for the selected city by making an API request.
     * Displays the fetched temperature, weather, humidity, and wind data in the respective TextViews.
     */
    private void loadWeatherDetails() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:5001/api/getDetails&city=" + cityName);
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
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String temperature = jsonResponse.optString("temperature", "--") + "°C";
                    String weather = jsonResponse.optString("weather", "--");
                    String humidity = jsonResponse.optString("humidity", "--") + "%";
                    String wind = jsonResponse.optString("wind", "--") + " km/h";

                    // Update UI on the main thread
                    runOnUiThread(() -> {
                        temperatureView.setText("Temperature: " + temperature);
                        weatherView.setText("Weather: " + weather);
                        humidityView.setText("Humidity: " + humidity);
                        windView.setText("Wind: " + wind);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(DetailsActivity.this, "Failed to load weather data.", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(DetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
