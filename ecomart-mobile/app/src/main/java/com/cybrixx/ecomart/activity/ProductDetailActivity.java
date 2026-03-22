package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.CartItem;
import com.cybrixx.ecomart.util.DatabaseHelper;

public class ProductDetailActivity extends AppCompatActivity {

    private int currentQuantity = 1; // Default quantity
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        databaseHelper = new DatabaseHelper(this);

        // 1. Initialize Views
        ImageView ivDetailImage = findViewById(R.id.ivDetailImage);
        TextView tvDetailName = findViewById(R.id.tvDetailName);
        TextView tvDetailPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDetailDescription = findViewById(R.id.tvDetailDescription);
        TextView tvQuantity = findViewById(R.id.tvQuantity);

        Button btnMinus = findViewById(R.id.btnMinus);
        Button btnPlus = findViewById(R.id.btnPlus);
        Button btnDetailAddToCart = findViewById(R.id.btnDetailAddToCart);
        Button btnBuyNow = findViewById(R.id.btnBuyNow);

        // 2. Get Data from Intent
        Intent intent = getIntent();
        Long productId = intent.getLongExtra("id", -1);
        String name = intent.getStringExtra("name");
        double price = intent.getDoubleExtra("price", 0.0);
        String imageUrl = intent.getStringExtra("imageUrl");
        String description = intent.getStringExtra("description");

        // 3. Set Data to UI
        tvDetailName.setText(name);
        tvDetailPrice.setText("Rs. " + price);
        tvDetailDescription.setText(description);
        Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_launcher_background).into(ivDetailImage);

        // 4. Quantity Logic
        btnPlus.setOnClickListener(v -> {
            currentQuantity++;
            tvQuantity.setText(String.valueOf(currentQuantity));
        });

        btnMinus.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                tvQuantity.setText(String.valueOf(currentQuantity));
            }
        });

        // 5. Add to Cart Logic
        btnDetailAddToCart.setOnClickListener(v -> {
            addToCart(productId, name, price, imageUrl);
            Toast.makeText(this, currentQuantity + "x " + name + " added to cart!", Toast.LENGTH_SHORT).show();
            finish(); // Go back to Home
        });

        // 6. Buy Now Logic (Add to cart AND redirect to Cart)
        btnBuyNow.setOnClickListener(v -> {
            addToCart(productId, name, price, imageUrl);
            Intent cartIntent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(cartIntent);
            finish();
        });
    }

    private void addToCart(Long id, String name, double price, String imageUrl) {
        CartItem cartItem = new CartItem(id, name, price, currentQuantity, imageUrl);
        // Note: We might need to update DatabaseHelper.addToCart to handle bulk quantity adds better later,
        // but for now, it inserts the correct quantity from this screen!
        databaseHelper.addToCart(cartItem);
    }
}