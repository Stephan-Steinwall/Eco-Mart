package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.adapter.CartAdapter;
import com.cybrixx.ecomart.model.CartItem;
import com.cybrixx.ecomart.util.DatabaseHelper;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartUpdateListener {

    private RecyclerView recyclerViewCart;
    private TextView tvCartTotal;
    private Button btnCheckout;
    private DatabaseHelper databaseHelper;
    private List<CartItem> cartItemList;
    private CartAdapter cartAdapter;

    private static final int PAYHERE_REQUEST = 11001;
    private double finalTotalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        databaseHelper = new DatabaseHelper(this);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        loadCartData();
        setupBottomNavigation();

        // --- The Checkout Button (PayHere) ---
        btnCheckout.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Just open the new Map/Checkout screen!
            startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
        });
    }

    private void loadCartData() {
        cartItemList = databaseHelper.getCartItems();
        cartAdapter = new CartAdapter(this, cartItemList, this);
        recyclerViewCart.setAdapter(cartAdapter);
        calculateTotal();
    }

    private void calculateTotal() {
        finalTotalAmount = 0.0;
        for (CartItem item : cartItemList) {
            finalTotalAmount += (item.getPrice() * item.getQuantity());
        }
        tvCartTotal.setText(String.format("Rs. %.2f", finalTotalAmount));
    }

    @Override
    public void onCartUpdated() {
        calculateTotal();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_cart) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(getApplicationContext(), OrderHistoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        }
            return false;
        });
    }
}