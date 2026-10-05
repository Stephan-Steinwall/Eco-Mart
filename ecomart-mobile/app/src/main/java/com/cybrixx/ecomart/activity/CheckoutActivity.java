package com.cybrixx.ecomart.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.CartItem;
import com.cybrixx.ecomart.util.DatabaseHelper;
import com.cybrixx.ecomart.util.SharedPrefsManager;

// PayHere Imports
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText etAddress, etPhone;
    private Button btnPayNow;

    private DatabaseHelper databaseHelper;
    private List<CartItem> cartItemList;
    private double finalTotalAmount = 0.0;
    private static final int PAYHERE_REQUEST = 11001;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // We will save these to send to Spring Boot later
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        btnPayNow = findViewById(R.id.btnPayNow);

        SharedPrefsManager prefs = new SharedPrefsManager(this);

        String savedAddress = prefs.getAddress();
        if (savedAddress != null && !savedAddress.isEmpty()) {
            etAddress.setText(savedAddress);
        }

        String savedPhone = prefs.getPhone();
        if (savedPhone != null && !savedPhone.isEmpty()) {
            etPhone.setText(savedPhone);
        }

        databaseHelper = new DatabaseHelper(this);
        cartItemList = databaseHelper.getCartItems();
        calculateTotal();

        btnPayNow.setText(String.format("Pay Rs. %.2f", finalTotalAmount));

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnPayNow.setOnClickListener(v -> initiatePayment());
    }

    private void calculateTotal() {
        for (CartItem item : cartItemList) {
            finalTotalAmount += (item.getPrice() * item.getQuantity());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize the location client if you haven't already
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 1. Check for GPS Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableRealLocation();
        } else {
            // Ask the user for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 2. Handle Map Clicks (Keep your existing logic here)
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Delivery Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;

            Geocoder geocoder = new Geocoder(CheckoutActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressText = addresses.get(0).getAddressLine(0);
                    etAddress.setText(addressText);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void enableRealLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); // Shows the blue dot

            // Zoom the camera to their exact current location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));

                    // Optional: You could even auto-drop the pin here!
                }
            });
        }
    }

    // Catch the user's answer when the permission popup appears
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableRealLocation();
            } else {
                Toast.makeText(this, "Location permission is required to auto-find you.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initiatePayment() {
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please provide address and contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPrefsManager prefs = new SharedPrefsManager(this);
        InitRequest req = new InitRequest();
        req.setMerchantId("1226921"); // YOUR SANDBOX ID HERE
        req.setCurrency("LKR");
        req.setAmount(finalTotalAmount);
        req.setOrderId("ECO-" + System.currentTimeMillis());
        req.setItemsDescription("EcoMart Order");
        req.setCustom1("");
        req.setCustom2("");

        req.getCustomer().setFirstName(prefs.getFirstName());
        req.getCustomer().setLastName(prefs.getLastName());
        req.getCustomer().setEmail(prefs.getEmail());
        req.getCustomer().setPhone(phone); // Use the typed phone number!
        req.getCustomer().getAddress().setAddress(address); // Use the map address!
        req.getCustomer().getAddress().setCity("Sri Lanka");
        req.getCustomer().getAddress().setCountry("Sri Lanka");
        req.getCustomer().getDeliveryAddress().setAddress(address);
        req.getCustomer().getDeliveryAddress().setCity("Sri Lanka");
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == RESULT_OK && response != null && response.isSuccess()) {
                sendOrderToBackend();
            } else {
                Toast.makeText(this, "Payment Failed/Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendOrderToBackend() {
        btnPayNow.setEnabled(false);
        btnPayNow.setText("Finalizing...");

        java.util.List<com.cybrixx.ecomart.model.OrderItemRequestDTO> orderItems = new java.util.ArrayList<>();
        for (CartItem item : cartItemList) {
            orderItems.add(new com.cybrixx.ecomart.model.OrderItemRequestDTO(item.getProductId(), item.getQuantity()));
        }

        // We pass the new dynamically generated address!
        String finalAddress = etAddress.getText().toString().trim() + " | Phone: " + etPhone.getText().toString().trim();
        com.cybrixx.ecomart.model.OrderRequestDTO orderRequest = new com.cybrixx.ecomart.model.OrderRequestDTO(finalAddress, selectedLat, selectedLng,orderItems);

        com.cybrixx.ecomart.network.OrderApi orderApi = com.cybrixx.ecomart.network.RetrofitClient.getInstance(this).create(com.cybrixx.ecomart.network.OrderApi.class);

        orderApi.placeOrder(orderRequest).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    databaseHelper.clearCart();
                    Toast.makeText(CheckoutActivity.this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(CheckoutActivity.this, OrderHistoryActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Order Failed: Please try again", Toast.LENGTH_LONG).show();
                    btnPayNow.setEnabled(true);
                    btnPayNow.setText(String.format("Pay Rs. %.2f", finalTotalAmount));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                btnPayNow.setEnabled(true);
            }
        });
    }
}
