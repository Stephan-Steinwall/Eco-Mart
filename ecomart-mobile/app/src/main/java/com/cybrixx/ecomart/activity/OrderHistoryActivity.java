package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.adapter.OrderAdapter;
import com.cybrixx.ecomart.model.OrderResponseDTO;
import com.cybrixx.ecomart.network.OrderApi;
import com.cybrixx.ecomart.network.RetrofitClient;
import com.cybrixx.ecomart.util.ErrorStateController;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private ErrorStateController errorState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history); // We made this XML in the last step

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        errorState = new ErrorStateController(findViewById(R.id.errorState));

        // Pull down to reload, e.g. to see an order's new status
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchMyOrders);

        setupBottomNavigation();
        fetchMyOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        errorState.release();
    }

    private void fetchMyOrders() {
        errorState.hide();
        swipeRefresh.setRefreshing(true);
        OrderApi orderApi = RetrofitClient.getInstance(this).create(OrderApi.class);

        orderApi.getMyOrders().enqueue(new Callback<List<OrderResponseDTO>>() {
            @Override
            public void onResponse(Call<List<OrderResponseDTO>> call, Response<List<OrderResponseDTO>> response) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderResponseDTO> orders = response.body();
                    orderAdapter = new OrderAdapter(OrderHistoryActivity.this, orders);
                    recyclerViewOrders.setAdapter(orderAdapter);
                } else {
                    errorState.showHttpError(response.code(), OrderHistoryActivity.this::fetchMyOrders);
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponseDTO>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                errorState.showFailure(t, OrderHistoryActivity.this::fetchMyOrders);
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
