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

public class AddLocation extends AppCompatActivity {

    private AutoCompleteTextView cityAutoCompleteTextView;
    private Button addCityButton;
    private List<String> cityList = new ArrayList<>(); // List to hold city names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        cityAutoCompleteTextView = findViewById(R.id.cityAutoCompleteTextView);
        addCityButton = findViewById(R.id.addCityButton);

        // Load cities from server for dropdown
        loadCitiesFromServer();

        addCityButton.setOnClickListener(new View.OnClickListener() {
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

    private void addCityToServer(String cityName, String userEmail) {
        new Thread(() -> {
            try {
                URL url = new URL(Config.API_URL + "addCity"); // Using 10.0.2.2 for local development
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
                    runOnUiThread(
                            () -> Toast.makeText(AddLocation.this, "Failed to add city.", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(
                        () -> Toast.makeText(AddLocation.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
