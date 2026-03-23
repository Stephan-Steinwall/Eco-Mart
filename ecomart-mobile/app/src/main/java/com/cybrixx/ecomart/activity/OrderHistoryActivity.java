package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.adapter.OrderAdapter;
import com.cybrixx.ecomart.model.OrderResponseDTO;
import com.cybrixx.ecomart.network.OrderApi;
import com.cybrixx.ecomart.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history); // We made this XML in the last step

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        setupBottomNavigation();
        fetchMyOrders();
    }

    private void fetchMyOrders() {
        OrderApi orderApi = RetrofitClient.getInstance(this).create(OrderApi.class);

        orderApi.getMyOrders().enqueue(new Callback<List<OrderResponseDTO>>() {
            @Override
            public void onResponse(Call<List<OrderResponseDTO>> call, Response<List<OrderResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderResponseDTO> orders = response.body();
                    orderAdapter = new OrderAdapter(OrderHistoryActivity.this, orders);
                    recyclerViewOrders.setAdapter(orderAdapter);
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponseDTO>> call, Throwable t) {
                Toast.makeText(OrderHistoryActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_orders); // Highlight "Orders"

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish(); // Close this activity
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0);
                finish(); // Close this activity
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true; // Already here
            } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        }
            return false;
        });
    }
}
