package com.cybrixx.ecomart.activity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.adapter.ProductAdapter;
import com.cybrixx.ecomart.model.ProductDTO;
import com.cybrixx.ecomart.network.ProductApi;
import com.cybrixx.ecomart.network.RetrofitClient;
import com.cybrixx.ecomart.util.SharedPrefsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private SharedPrefsManager sharedPrefsManager;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;

    // The master list that holds all data directly from the database
    private List<ProductDTO> allProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPrefsManager = new SharedPrefsManager(this);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));

        setupSearchAndSort();
        fetchProducts();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Highlight "Home"

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(getApplicationContext(), OrderHistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void setupSearchAndSort() {
        EditText etSearch = findViewById(R.id.etSearch);
        Spinner spinnerSort = findViewById(R.id.spinnerSort);

        // --- 1. SETUP SORTING SPINNER ---
        String[] sortOptions = {"Default", "Price: Low to High", "Price: High to Low"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortOptions);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (allProducts.isEmpty() || productAdapter == null) return;

                // Create a copy of the list to sort
                List<ProductDTO> sortedList = new ArrayList<>(allProducts);

                if (position == 1) { // Low to High
                    Collections.sort(sortedList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
                } else if (position == 2) { // High to Low
                    Collections.sort(sortedList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                }

                productAdapter.setFilteredList(sortedList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- 2. SETUP SEARCH BAR ---
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList(String text) {
        if (allProducts.isEmpty() || productAdapter == null) return;

        List<ProductDTO> filteredList = new ArrayList<>();
        for (ProductDTO product : allProducts) {
            // Check if the product name contains the search text (case insensitive)
            if (product.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.setFilteredList(filteredList);
    }

    private void fetchProducts() {
        ProductApi productApi = RetrofitClient.getInstance(this).create(ProductApi.class);

        productApi.getProducts().enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save to master list
                    allProducts = response.body();

                    productAdapter = new ProductAdapter(HomeActivity.this, allProducts);
                    recyclerViewProducts.setAdapter(productAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Options Menu (Logout) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Logout");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            sharedPrefsManager.clearSession();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}