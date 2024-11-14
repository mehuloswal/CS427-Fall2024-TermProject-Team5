package edu.uiuc.cs427app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherActivity extends AppCompatActivity {

    private String cityName;
    private double latitude;
    private double longitude;
    private TextView cityNameTextView, weatherTextView, temperatureTextView, humidityTextView, windSpeedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Retrieve data from Intent
        cityName = getIntent().getStringExtra("cityName");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        // Initialize TextViews
        cityNameTextView = findViewById(R.id.cityNameTextView);
        weatherTextView = findViewById(R.id.weatherTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);

        // Set city name
        cityNameTextView.setText(cityName);

        // Fetch weather data
        new FetchWeatherTask().execute();
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
                    weatherTextView.setText("Weather: " + weatherDescription);
                    temperatureTextView.setText("Temperature: " + temperature + "°C");
                    humidityTextView.setText("Humidity: " + humidity + "%");
                    windSpeedTextView.setText("Wind Speed: " + windSpeed + " m/s");
                } catch (Exception e) {
                    Toast.makeText(WeatherActivity.this, "Error parsing weather data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(WeatherActivity.this, "Failed to fetch weather data.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
