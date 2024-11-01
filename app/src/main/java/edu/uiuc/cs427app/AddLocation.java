package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddLocation extends AppCompatActivity {

    private EditText cityEditText;
    private Button addCityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        cityEditText = findViewById(R.id.cityEditText);
        addCityButton = findViewById(R.id.addCityButton);

        addCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityEditText.getText().toString().trim();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null && !cityName.isEmpty()) {
                    String userEmail = user.getEmail();
                    addCityToServer(cityName, userEmail);
                } else {
                    Toast.makeText(AddLocation.this, "Please enter a city name.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addCityToServer(String cityName, String userEmail) {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:5000/addCity");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = "{\"cityName\": \"" + cityName + "\", \"userEmail\": \"" + userEmail + "\"}";

                try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                    writer.write(jsonInputString);
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
                    runOnUiThread(() -> Toast.makeText(AddLocation.this, "Failed to add city.", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(AddLocation.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}