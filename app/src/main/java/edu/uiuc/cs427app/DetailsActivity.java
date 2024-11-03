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

        // Setting up UI elements
        TextView welcomeMessage = findViewById(R.id.welcomeText);
        TextView cityInfoMessage = findViewById(R.id.cityInfo);
        Button buttonMap = findViewById(R.id.mapButton);

        // Display city information
        welcomeMessage.setText("Welcome to the " + cityName);
        cityInfoMessage.setText("Detailed information about the weather of " + cityName);
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
}
