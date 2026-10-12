package com.cybrixx.ecomart.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.CartItem;
import com.cybrixx.ecomart.util.DatabaseHelper;
import com.cybrixx.ecomart.util.ImageLoader;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartList;
    private DatabaseHelper databaseHelper;
    private CartUpdateListener listener;

    // Interface to tell the Activity that the Total Price needs to be recalculated
    public interface CartUpdateListener {
        void onCartUpdated();

        // An item was removed (button or swipe) - lets the screen offer an Undo.
        void onItemRemoved(CartItem removedItem);
    }

    // Removes the item at this position from the list and the database (shared by the Remove
    // button and the swipe gesture).
    public void removeAt(int position) {
        if (position < 0 || position >= cartList.size()) return;
        CartItem removed = cartList.remove(position);
        databaseHelper.deleteItem(removed.getId());
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, cartList.size() - position);
        listener.onCartUpdated(); // Update Total Price
        listener.onItemRemoved(removed);
    }

    public CartAdapter(Context context, List<CartItem> cartList, CartUpdateListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.tvCartName.setText(item.getName());
        holder.tvCartPrice.setText("Rs. " + item.getPrice());
        holder.tvCartQuantity.setText(String.valueOf(item.getQuantity()));

        ImageLoader.load(holder.ivCartImage, item.getImageUrl());

        // --- Increase Quantity ---
        holder.btnCartPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty);
            databaseHelper.updateQuantity(item.getId(), newQty);
            holder.tvCartQuantity.setText(String.valueOf(newQty));
            listener.onCartUpdated(); // Update Total Price
        });

        // --- Decrease Quantity ---
        holder.btnCartMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQty = item.getQuantity() - 1;
                item.setQuantity(newQty);
                databaseHelper.updateQuantity(item.getId(), newQty);
                holder.tvCartQuantity.setText(String.valueOf(newQty));
                listener.onCartUpdated(); // Update Total Price
            }
        });

        // --- Remove Item ---
        holder.tvRemove.setOnClickListener(v -> removeAt(holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartImage;
        TextView tvCartName, tvCartPrice, tvCartQuantity, tvRemove;
        TextView btnCartMinus, btnCartPlus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartImage = itemView.findViewById(R.id.ivCartImage);
            tvCartName = itemView.findViewById(R.id.tvCartName);
            tvCartPrice = itemView.findViewById(R.id.tvCartPrice);
            tvCartQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnCartMinus = itemView.findViewById(R.id.btnCartMinus);
            btnCartPlus = itemView.findViewById(R.id.btnCartPlus);
            tvRemove = itemView.findViewById(R.id.tvRemove);
        }
    }
}
