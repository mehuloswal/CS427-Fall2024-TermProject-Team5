package edu.uiuc.cs427app;

import edu.uiuc.cs427app.Config;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    TextInputEditText editUsername, editPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView loginNow;
    Switch themeSwitch;

    /**
     * Called when the activity is starting.
     * Checks if the user is already signed in and redirects to the main activity if so.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and redirect to main activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Initializes the activity, sets up the UI components, and configures the theme switch and
     * registration functionality.
     *
     * @param savedInstanceState Bundle object containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize the views variables, add whatever necessary
        mAuth = FirebaseAuth.getInstance();
        buttonReg = findViewById(R.id.btn_register);
        editUsername = findViewById(R.id.username);
        editPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        loginNow = findViewById(R.id.loginNow);
        themeSwitch = findViewById(R.id.themeSwitch);

        // set up the theme switch feature
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        AppCompatDelegate
                .setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        loginNow.setOnClickListener(new View.OnClickListener() {
            /**
             * Redirects the user to the login activity if they already have an account.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for the "Register" button.
             * Validates the email and password inputs, then attempts to register the user
             * with Firebase Authentication.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String username, password;
                username = editUsername.getText().toString();
                password = editPassword.getText().toString();

                // Regular expression to detect email-like patterns
                Pattern emailPattern = Pattern.compile("@.*\\..*");
                Matcher matcher = emailPattern.matcher(username);

                // Check if username contains an email-like pattern
                if (matcher.find()) {
                    Toast.makeText(Register.this, "Username should not contain '@domain'. It will be appended automatically as '@illinois.edu'.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                String email = username + "@illinois.edu";
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Please enter user name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                Task<AuthResult> createUserTask = mAuth.createUserWithEmailAndPassword(email, password);

                // If failed, log the error to user
                createUserTask.addOnFailureListener(exception -> {
                    Toast.makeText(Register.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                });
                createUserTask.addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        boolean isDarkMode = themeSwitch.isChecked();
                        sendUserToBackend(email, isDarkMode);
                    }
                });

            }
        });

    }

    /**
     * Sends user information to the backend server upon successful registration.
     * Constructs a JSON object with user data and sends it as a POST request.
     * Displays success or failure message based on the server response.
     *
     * @param email      The email address of the registered user.
     * @param isDarkMode The user's theme preference (dark mode enabled or disabled).
     */
    private void sendUserToBackend(String email, boolean isDarkMode) {
        new Thread(() -> {
            try {
                // Prepare JSON data
                String username = email.split("@")[0];
                JSONObject jsonData = new JSONObject();
                jsonData.put("username", username);
                jsonData.put("email", email);
                jsonData.put("theme", isDarkMode);

                // Set up the connection
                URL url = new URL(Config.API_URL + "users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Send JSON data
                OutputStream os = conn.getOutputStream();
                os.write(jsonData.toString().getBytes("UTF-8"));
                os.close();

                // Check the response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    runOnUiThread(() -> Toast.makeText(Register.this, "User created successfully", Toast.LENGTH_SHORT)
                            .show());
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    runOnUiThread(
                            () -> Toast.makeText(Register.this, "Failed to create user", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(
                        () -> Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}