package edu.uiuc.cs427app;

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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {
    TextInputEditText editUsername, editPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView registerNow;

    /**
     * Checks if the user is already signed in when the activity starts.
     * If the user is signed in, redirects them to the main activity.
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
     * Initializes the login activity, sets up UI elements, theme switching, and
     * button actions.
     * Configures Firebase Authentication instance and handles user interactions for
     * login.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved
     *                           state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        buttonLogin = findViewById(R.id.btn_login);
        editUsername = findViewById(R.id.username);
        editPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        registerNow = findViewById(R.id.registerNow);

        registerNow.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for "Register Now" text view.
             * Redirects the user to the registration activity.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for the "Login" button.
             * Validates email and password inputs, then attempts to log in the user via
             * Firebase Authentication.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String username, password;
                username = editUsername.getText().toString();
                password = editPassword.getText().toString();

                String email = username + "@illinois.edu";
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            /**
                             * Callback triggered when Firebase Authentication completes login attempt.
                             * If successful, redirects to the main activity; otherwise, shows an error
                             * message.
                             *
                             * @param task The result of the Firebase authentication attempt.
                             */
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Fetch the user's theme preference
                                    fetchUserThemePreference(email);

                                } else {
                                    // sign in fails
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    /**
     * Fetches and applies the user's theme preference from the backend server.
     *
     * This method:
     * - Makes an HTTP request to fetch theme preference
     * - Saves the preference locally
     * - Applies the theme setting
     * - Navigates to MainActivity on completion
     *
     * If the theme fetch fails, the method still proceeds to MainActivity
     * with default theme settings.
     *
     * @param userEmail The email address of the authenticated user
     */
    private void fetchUserThemePreference(String userEmail) {
        new Thread(() -> {
            try {
                URL url = new URL(Config.API_URL + "user/theme?userEmail=" + userEmail);
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

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean isNightMode = jsonResponse.getBoolean("theme");

                    runOnUiThread(() -> {
                        // Save theme preference locally
                        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("night_mode", isNightMode);
                        editor.apply();

                        // Apply theme and redirect to main activity
                        AppCompatDelegate.setDefaultNightMode(
                                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                        Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(Login.this, "Failed to load theme preference.", Toast.LENGTH_SHORT).show();
                        // Proceed to main activity even if theme fetch fails
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Proceed to main activity even if there's an error
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        }).start();
    }
}