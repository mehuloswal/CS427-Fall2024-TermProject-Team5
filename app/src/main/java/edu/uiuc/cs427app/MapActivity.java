package edu.uiuc.cs427app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String cityName;
    private double latitude;
    private double longitude;
    private MapView mapView;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Retrieve data from Intent
        cityName = getIntent().getStringExtra("cityName");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        // Set city details
        TextView cityNameTextView = findViewById(R.id.cityNameTextView);
        TextView coordinatesTextView = findViewById(R.id.coordinatesTextView);
        cityNameTextView.setText(cityName);
        coordinatesTextView.setText("Latitude: " + latitude + ", Longitude: " + longitude);

        // Initialize the MapView
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);

        // Set the map ready callback
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Create a LatLng object for the city location
        LatLng cityLocation = new LatLng(latitude, longitude);

        // Add a marker at the city location
        googleMap.addMarker(new MarkerOptions()
                .position(cityLocation)
                .title(cityName));

        // Move and zoom the camera to the city location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 12));
    }

    // MapView lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // Save instance state to handle configuration changes
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
}