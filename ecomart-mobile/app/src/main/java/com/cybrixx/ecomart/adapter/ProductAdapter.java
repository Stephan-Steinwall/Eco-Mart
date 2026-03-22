package com.cybrixx.ecomart.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.CartItem;
import com.cybrixx.ecomart.model.ProductDTO;
import com.cybrixx.ecomart.util.DatabaseHelper;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductDTO> productList;
    private DatabaseHelper databaseHelper; // Add the helper

    public ProductAdapter(Context context, List<ProductDTO> productList) {
        this.context = context;
        this.productList = productList;
        this.databaseHelper = new DatabaseHelper(context); // Initialize it
    }

    public void setFilteredList(List<ProductDTO> filteredList) {
        this.productList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDTO product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText("Rs. " + product.getPrice());

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivProductImage);

        // --- NEW: Click the Card to View Details ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.cybrixx.ecomart.activity.ProductDetailActivity.class);
            // Pass the data to the next screen
            intent.putExtra("id", product.getId());
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            // If description is null in DB, pass a default string
            intent.putExtra("description", product.getDescription() != null ? product.getDescription() : "No description available.");
            context.startActivity(intent);
        });

        // --- Handle Add to Cart Click (from the Home Screen) ---
        holder.btnAddToCart.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(product.getId(), product.getName(), product.getPrice(), 1, product.getImageUrl());
            boolean isAdded = databaseHelper.addToCart(cartItem);
            if (isAdded) {
                Toast.makeText(context, product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice;
        Button btnAddToCart; // Declare the button

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart); // Initialize it
        }
    }
}