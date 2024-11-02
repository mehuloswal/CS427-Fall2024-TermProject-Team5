package edu.uiuc.cs427app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailsActivity extends AppCompatActivity {

    private String cityName;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Fetching city name from intent and Firebase user information
        cityName = getIntent().getStringExtra("city");
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Setting up UI elements
        TextView welcomeMessage = findViewById(R.id.welcomeText);
        TextView cityInfoMessage = findViewById(R.id.cityInfo);
        Button buttonMap = findViewById(R.id.mapButton);

        // Display city information
        welcomeMessage.setText("Welcome to the " + cityName);
        cityInfoMessage.setText("Detailed information about the weather of " + cityName);

        // Map button listener
        // buttonMap.setOnClickListener(view -> {
        // // Navigate to Map Activity
        // Intent mapIntent = new Intent(DetailsActivity.this, MapActivity.class);
        // mapIntent.putExtra("city", cityName);
        // startActivity(mapIntent);
        // });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete City")
                .setMessage("Are you sure you want to delete this city?")
                .setPositiveButton("Yes", (dialog, which) -> deleteCity())
                .setNegativeButton("No", null)
                .show();
    }

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
}
