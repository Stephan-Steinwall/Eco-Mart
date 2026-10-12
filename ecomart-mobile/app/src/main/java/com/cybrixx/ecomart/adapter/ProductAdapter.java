package com.cybrixx.ecomart.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.CartItem;
import com.cybrixx.ecomart.model.ProductDTO;
import com.cybrixx.ecomart.util.DatabaseHelper;
import com.cybrixx.ecomart.util.ImageLoader;

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
        ProductViewHolder holder = new ProductViewHolder(view);
        setUpCardGestures(holder);
        return holder;
    }

    // --- Card gestures: tap opens the product, double-tap adds it to the cart ---
    // The touch listener only observes events and returns false, so the view's own click handling
    // (ripple, accessibility) still runs; lint's "call performClick" advice doesn't apply.
    @SuppressLint("ClickableViewAccessibility")
    private void setUpCardGestures(ProductViewHolder holder) {
        GestureDetector detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true; // we want the rest of the gesture
            }

            // Fires only once we know it wasn't the first half of a double-tap.
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                openDetail(holder);
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                quickAddToCart(holder);
                return true;
            }
        });

        // Observe touches without consuming them, so the card's pressed ripple and
        // the RecyclerView's scrolling keep working.
        holder.itemView.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false;
        });
        holder.itemView.setClickable(true);

        // TalkBack's "activate" action must still open the product (there is no click listener now).
        ViewCompat.setAccessibilityDelegate(holder.itemView, new AccessibilityDelegateCompat() {
            @Override
            public boolean performAccessibilityAction(@NonNull View host, int action, @Nullable Bundle args) {
                if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
                    openDetail(holder);
                    return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }
        });
    }

    private ProductDTO productAt(ProductViewHolder holder) {
        int position = holder.getBindingAdapterPosition();
        return position == RecyclerView.NO_POSITION ? null : productList.get(position);
    }

    private void openDetail(ProductViewHolder holder) {
        ProductDTO product = productAt(holder);
        if (product == null) return;

        Intent intent = new Intent(context, com.cybrixx.ecomart.activity.ProductDetailActivity.class);
        // Pass the data to the next screen
        intent.putExtra("id", product.getId());
        intent.putExtra("name", product.getName());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("imageUrl", product.getImageUrl());
        // If description is null in DB, pass a default string
        intent.putExtra("description", product.getDescription() != null ? product.getDescription() : "No description available.");
        context.startActivity(intent);
    }

    private void quickAddToCart(ProductViewHolder holder) {
        ProductDTO product = productAt(holder);
        if (product == null) return;

        CartItem cartItem = new CartItem(product.getId(), product.getName(), product.getPrice(), 1, product.getImageUrl());
        if (databaseHelper.addToCart(cartItem)) {
            holder.itemView.performHapticFeedback(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.VIRTUAL_KEY);
            playAddedBurst(holder.ivAddedBurst);
        }
    }

    private void playAddedBurst(View burst) {
        burst.animate().cancel();
        burst.setVisibility(View.VISIBLE);
        burst.setAlpha(0f);
        burst.setScaleX(0.4f);
        burst.setScaleY(0.4f);
        burst.animate().alpha(1f).scaleX(1.1f).scaleY(1.1f).setStartDelay(0).setDuration(180)
                .withEndAction(() -> burst.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        .withEndAction(() -> burst.animate().alpha(0f).setStartDelay(350).setDuration(250)
                                .withEndAction(() -> burst.setVisibility(View.GONE)).start())
                        .start())
                .start();
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDTO product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText("Rs. " + product.getPrice());

        ImageLoader.load(holder.ivProductImage, product.getImageUrl());

        // A recycled card must not show a leftover "added" burst from its previous product.
        holder.ivAddedBurst.animate().cancel();
        holder.ivAddedBurst.setVisibility(View.GONE);

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
        ImageView ivProductImage, ivAddedBurst;
        TextView tvProductName, tvProductPrice;
        Button btnAddToCart; // Declare the button

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivAddedBurst = itemView.findViewById(R.id.ivAddedBurst);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart); // Initialize it
        }
    }
}
