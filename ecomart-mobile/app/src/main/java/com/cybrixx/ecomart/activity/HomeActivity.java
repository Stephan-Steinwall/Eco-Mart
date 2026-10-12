package com.cybrixx.ecomart.activity;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.adapter.ProductAdapter;
import com.cybrixx.ecomart.model.OfferDTO;
import com.cybrixx.ecomart.model.ProductDTO;
import com.cybrixx.ecomart.network.OfferApi;
import com.cybrixx.ecomart.network.ProductApi;
import com.cybrixx.ecomart.network.RetrofitClient;
import com.cybrixx.ecomart.util.ErrorStateController;
import com.cybrixx.ecomart.util.PushManager;
import com.cybrixx.ecomart.util.SharedPrefsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String PREFS_HINTS = "ecomart_hints";
    private static final String HINT_DOUBLE_TAP = "double_tap_tip_shown";

    private SharedPrefsManager sharedPrefsManager;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private ErrorStateController errorState;
    private EditText etSearch;
    private Spinner spinnerSort;
    private BottomNavigationView bottomNavigationView;

    // The master list that holds all data directly from the database
    private List<ProductDTO> allProducts = new ArrayList<>();

    // Timed-offer banner
    private View offerBanner;
    private TextView tvOfferTitle, tvOfferMessage, tvOfferCountdown, tvOfferDiscount;
    private long offerEndsAtElapsed; // SystemClock.elapsedRealtime() at which the offer ends
    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private final Runnable countdownTick = this::updateCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPrefsManager = new SharedPrefsManager(this);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        etSearch = findViewById(R.id.etSearch);
        spinnerSort = findViewById(R.id.spinnerSort);

        errorState = new ErrorStateController(findViewById(R.id.errorState));

        // Pull down on the product list to reload it
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::reload);

        offerBanner = findViewById(R.id.offerBanner);
        tvOfferTitle = findViewById(R.id.tvOfferTitle);
        tvOfferMessage = findViewById(R.id.tvOfferMessage);
        tvOfferCountdown = findViewById(R.id.tvOfferCountdown);
        tvOfferDiscount = findViewById(R.id.tvOfferDiscount);

        setupSearchAndSort();
        reload();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
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
            } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
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

        // Link this phone to the logged-in account for order updates, and subscribe to offers
        PushManager.register(this);
    }

    // Tapping an offer notification while Home is already open lands here instead of recreating it.
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        fetchActiveOffer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (offerBanner.getVisibility() == View.VISIBLE) {
            updateCountdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        countdownHandler.removeCallbacks(countdownTick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countdownHandler.removeCallbacks(countdownTick);
        errorState.release();
    }

    private void setupSearchAndSort() {
        // --- 1. SETUP SORTING SPINNER ---
        String[] sortOptions = {"Default", "Price: Low to High", "Price: High to Low"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortOptions);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilterAndSort();
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
                applyFilterAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Shows allProducts narrowed by the search box and ordered by the sort spinner. Also re-applied
    // after a refresh so pulling down doesn't silently throw away what the user typed or chose.
    private void applyFilterAndSort() {
        if (allProducts.isEmpty() || productAdapter == null) return;

        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<ProductDTO> shown = new ArrayList<>();
        for (ProductDTO product : allProducts) {
            // Check if the product name contains the search text (case insensitive)
            if (query.isEmpty() || product.getName().toLowerCase(Locale.ROOT).contains(query)) {
                shown.add(product);
            }
        }

        int sortPosition = spinnerSort.getSelectedItemPosition();
        if (sortPosition == 1) { // Low to High
            Collections.sort(shown, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        } else if (sortPosition == 2) { // High to Low
            Collections.sort(shown, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        }

        productAdapter.setFilteredList(shown);
    }

    // Everything the home screen shows from the server: used on start, pull-to-refresh and "Try again".
    private void reload() {
        fetchProducts();
        fetchActiveOffer();
    }

    private void fetchProducts() {
        errorState.hide();
        swipeRefresh.setRefreshing(true);
        ProductApi productApi = RetrofitClient.getInstance(this).create(ProductApi.class);

        productApi.getProducts().enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Save to master list
                    allProducts = response.body();

                    productAdapter = new ProductAdapter(HomeActivity.this, allProducts);
                    recyclerViewProducts.setAdapter(productAdapter);
                    applyFilterAndSort();
                    showGestureTipOnce();
                } else {
                    errorState.showHttpError(response.code(), HomeActivity.this::reload);
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                errorState.showFailure(t, HomeActivity.this::reload);
            }
        });
    }

    // --- Timed offers ---

    private void fetchActiveOffer() {
        OfferApi offerApi = RetrofitClient.getInstance(this).create(OfferApi.class);
        offerApi.getActiveOffers().enqueue(new Callback<List<OfferDTO>>() {
            @Override
            public void onResponse(Call<List<OfferDTO>> call, Response<List<OfferDTO>> response) {
                if (isFinishing() || isDestroyed()) return;
                OfferDTO newest = null;
                if (response.isSuccessful() && response.body() != null) {
                    for (OfferDTO offer : response.body()) {
                        if (offer.getSecondsRemaining() > 0 && (newest == null || offer.getId() > newest.getId())) {
                            newest = offer;
                        }
                    }
                }
                if (newest != null) {
                    showOffer(newest);
                } else {
                    hideOffer();
                }
            }

            @Override
            public void onFailure(Call<List<OfferDTO>> call, Throwable t) {
                // The banner is a bonus; a failure here must not interrupt shopping. The product
                // list shows the proper error page if the connection is really down.
            }
        });
    }

    private void showOffer(OfferDTO offer) {
        tvOfferTitle.setText(offer.getTitle());
        tvOfferMessage.setText(offer.getMessage());
        tvOfferDiscount.setText(offer.getDiscountPercent() + "%\nOFF");
        offerEndsAtElapsed = SystemClock.elapsedRealtime() + offer.getSecondsRemaining() * 1000;
        offerBanner.setVisibility(View.VISIBLE);
        countdownHandler.removeCallbacks(countdownTick);
        updateCountdown();
    }

    private void hideOffer() {
        countdownHandler.removeCallbacks(countdownTick);
        offerBanner.setVisibility(View.GONE);
    }

    private void updateCountdown() {
        long remaining = (offerEndsAtElapsed - SystemClock.elapsedRealtime()) / 1000;
        if (remaining <= 0) {
            hideOffer(); // the offer just ended
            return;
        }
        long days = remaining / 86400;
        long hours = (remaining % 86400) / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;
        String text = days > 0
                ? String.format(Locale.US, "Ends in %dd %02dh %02dm", days, hours, minutes)
                : String.format(Locale.US, "Ends in %02d:%02d:%02d", hours, minutes, seconds);
        tvOfferCountdown.setText(text);
        countdownHandler.postDelayed(countdownTick, 1000);
    }

    // Gestures aren't discoverable on their own, so mention the double-tap once, ever.
    private void showGestureTipOnce() {
        SharedPreferences hints = getSharedPreferences(PREFS_HINTS, MODE_PRIVATE);
        if (allProducts.isEmpty() || hints.getBoolean(HINT_DOUBLE_TAP, false)) return;
        hints.edit().putBoolean(HINT_DOUBLE_TAP, true).apply();

        Snackbar.make(recyclerViewProducts, "Tip: double-tap a product to add it to your cart", 6000)
                .setAnchorView(bottomNavigationView)
                .setAction("Got it", v -> { })
                .show();
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
            PushManager.unregister(this); // must run before the session (and its token) is cleared
            sharedPrefsManager.clearSession();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
