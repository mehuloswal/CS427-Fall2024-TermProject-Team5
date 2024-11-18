package edu.uiuc.cs427app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Import the Google Generative AI library.
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

public class WeatherInsightsActivity extends AppCompatActivity {

    private static final String TAG = "WeatherInsightsActivity";
    private String weatherData;
    private String cityName;
    private String apiKey = Config.GEMINI_KEY;

    private ProgressBar progressBar;
    private LinearLayout questionContainer;
    private TextView responseText;
    private TextView responseTitle;
    private GenerativeModelFutures model;

    /**
     * Initializes the activity, sets up UI elements, and fetches weather data
     * passed from DetailsActivity.
     *
     * @param savedInstanceState if the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_insights);

        // Initialize UI elements
        questionContainer = findViewById(R.id.questionContainer);
        responseText = findViewById(R.id.responseText);
        responseTitle = findViewById(R.id.responseTitle);
        progressBar = findViewById(R.id.progressBar);

        responseTitle.setVisibility(View.GONE);

        // Fetch weather data passed from DetailsActivity
        weatherData = getIntent().getStringExtra("weatherData");

        cityName = getIntent().getStringExtra("city");

        Log.d(TAG, "weatherData: " + weatherData);
        Log.d(TAG, "city: " + cityName);

        // Initialize the Gemini model
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        model = GenerativeModelFutures.from(gm);

        if (weatherData != null) {
            generateQuestionsFromWeatherData(weatherData);
        } else {
            Toast.makeText(this, "No weather data available (Check Details Activity)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Calls the Gemini API to generate context-specific questions based on the
     * provided weather data.
     *
     * @param weatherData A string containing current weather details.
     */
    private void generateQuestionsFromWeatherData(String weatherData) {
        progressBar.setVisibility(View.VISIBLE);

        // Prepare the prompt for question generation
        Content content = new Content.Builder()
                .addText("Given the current weather in " + cityName + ": " + weatherData +
                        ". Generate exactly three unique, context-specific questions that a person in " + cityName +
                        " might ask to help make decisions about their day. Consider activities, clothing, transportation, and events. "
                        +
                        "Avoid generic questions and ensure the questions are varied and specific to the current weather conditions. "
                        +
                        "List each question on a new line without any numbering or additional text.")
                .build();

        // Listenable future to represent asynchronous computation
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // Callbacks for success or failure events
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    /**
                     * Called when the Gemini API request completes successfully.
                     * Parses the response to extract generated questions and updates the UI to display them.
                     *
                     * @param result The result of the API call, containing the generated content.
                     */
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        // Parse response (the generated questions)
                        List<String> questions = parseQuestions(result.getText());

                        // Display the questions
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            displayQuestions(questions);
                        });
                    }

                    /**
                     * Called when the Gemini API request fails.
                     * Logs the error and updates the UI to notify the user of the failure.
                     *
                     * @param t The exception thrown during the API call.
                     */
                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("WeatherInsightsActivity", "Error generating questions", t);
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(WeatherInsightsActivity.this,
                                    "Failed to generate questions from Gemini service", Toast.LENGTH_SHORT).show();
                        });
                    }
                },
                executor);
    }

    /**
     * Parses the response text from Gemini API and returns a list of generated
     * questions.
     * Assumption: response contains exactly three lines, each is one question.
     * 
     * @param responseText A string containing generated questions.
     * @return A list of questions.
     */
    private List<String> parseQuestions(String responseText) {
        if (responseText == null || responseText.isEmpty()) {
            Toast.makeText(WeatherInsightsActivity.this, "Response is successful but empty", Toast.LENGTH_SHORT).show();
            return new ArrayList<>();
        }

        List<String> questions = new ArrayList<>();
        String[] lines = responseText.split("\\r?\\n");
        for (String line : lines) {
            String question = line.trim();
            if (!question.isEmpty()) {
                questions.add(question);
            }
        }
        return questions;
    }

    /**
     * Dynamically creates buttons for each question and adds them to the
     * questionContainer layout.
     * Each button will trigger another call to LLM service to answer the
     * corresponding question.
     *
     * @param questions A list of context-specific questions generated from weather
     *                  data.
     */
    private void displayQuestions(List<String> questions) {
        questionContainer.removeAllViews();

        for (String question : questions) {
            Button questionButton = new Button(this);
            questionButton.setText(question);
            // Set up the button's layout parameters and click listener
            questionButton.setOnClickListener(new View.OnClickListener() {
                /**
                 * Called when the user clicks on a question button.
                 * Triggers the generation of an answer for the selected question using the Gemini API.
                 *
                 * @param v The view that was clicked (the question button).
                 */
                @Override
                public void onClick(View v) {
                    generateAnswerForQuestion(question);
                }
            });
            questionContainer.addView(questionButton);
        }
    }

    /**
     * Calls the Gemini API to generate an answer to the user's selected question
     * based on the weather data.
     *
     * @param question The user's selected question.
     */
    private void generateAnswerForQuestion(String question) {
        // Show progress bar first
        progressBar.setVisibility(View.VISIBLE);
        // Prepare the prompt for answer generation
        Content content = new Content.Builder()
                .addText("Here is the weather data: " + weatherData + ". " +
                        "Answer the question briefly with explanations: " + question)
                .build();

        // Use a single-threaded executor
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // Callbacks for success or failure events
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    /**
                     * Called when the Gemini API request completes successfully.
                     * Parses the response to extract generated questions and updates the UI to display them.
                     *
                     * @param result The result of the API call, containing the generated content.
                     */
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String answer = result.getText();
                        runOnUiThread(() -> {
                            // Hides the progress bar and show the title
                            progressBar.setVisibility(View.GONE);
                            responseTitle.setVisibility(View.VISIBLE);
                            responseText.setText("Q: " + question + "\n\nA: " + answer);
                        });
                    }

                    /**
                     * Called when the Gemini API request fails.
                     * Logs the error and updates the UI to notify the user of the failure.
                     *
                     * @param t The exception thrown during the API call.
                     */
                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("WeatherInsightsActivity", "Error generating answer", t);
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(WeatherInsightsActivity.this, "Failed to get answer from Gemini service",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                },
                executor);
    }
}