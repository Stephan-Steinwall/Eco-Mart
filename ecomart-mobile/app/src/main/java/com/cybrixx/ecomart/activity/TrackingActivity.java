package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.cybrixx.ecomart.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private double destLat = 0.0;
    private double destLng = 0.0;
    private String currentStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        TextView tvTrackingTitle = findViewById(R.id.tvTrackingTitle);

        // 1. Get data from Intent
        Intent intent = getIntent();
        Long orderId = intent.getLongExtra("ORDER_ID", -1);
        currentStatus = intent.getStringExtra("STATUS");
        destLat = intent.getDoubleExtra("LATITUDE", 0.0);
        destLng = intent.getDoubleExtra("LONGITUDE", 0.0);

        tvTrackingTitle.setText("Tracking Order #" + orderId);

        // 2. Update the visual timeline
        updateStatusUI(currentStatus);

        // 3. Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trackingMapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupBottomNavigation();
    }

    private void updateStatusUI(String status) {
        TextView stepPending = findViewById(R.id.stepPending);
        TextView stepPreparing = findViewById(R.id.stepPreparing);
        TextView stepOnTheWay = findViewById(R.id.stepOnTheWay);
        TextView stepDelivered = findViewById(R.id.stepDelivered);

        // Reset all to gray first (handled in XML, but safe to enforce)
        int activeColor = Color.parseColor("#4CAF50"); // EcoMart Green
        int inactiveColor = Color.parseColor("#BDBDBD"); // Gray

        // Highlight based on current status
        if (status != null) {
            switch (status.toUpperCase()) {
                case "PENDING":
                    setActiveStep(stepPending, activeColor);
                    break;
                case "PREPARING":
                    setActiveStep(stepPending, activeColor);
                    setActiveStep(stepPreparing, activeColor);
                    break;
                case "ON_THE_WAY":
                    setActiveStep(stepPending, activeColor);
                    setActiveStep(stepPreparing, activeColor);
                    setActiveStep(stepOnTheWay, activeColor);
                    break;
                case "DELIVERED":
                    setActiveStep(stepPending, activeColor);
                    setActiveStep(stepPreparing, activeColor);
                    setActiveStep(stepOnTheWay, activeColor);
                    setActiveStep(stepDelivered, activeColor);
                    break;
                case "CANCELLED":
                    stepPending.setText("Order Cancelled");
                    stepPending.setTextColor(Color.RED);
                    break;
            }
        }
    }

    private void setActiveStep(TextView tv, int color) {
        tv.setTextColor(color);
        tv.setTypeface(null, Typeface.BOLD);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Plot the delivery location!
        if (destLat != 0.0 && destLng != 0.0) {
            LatLng deliveryLocation = new LatLng(destLat, destLng);
            googleMap.addMarker(new MarkerOptions()
                    .position(deliveryLocation)
                    .title("Delivery Destination"));

            // Zoom in on the location
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(deliveryLocation, 15f));
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_orders); // Highlight Orders since we are in the order flow

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finishAffinity(); // Clear stack
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0);
                finishAffinity();
                return true;
            } else if (itemId == R.id.nav_orders) {
                // Just go back one screen to the main Order History list
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finishAffinity();
                return true;
            }
            return false;
        });
    }
}