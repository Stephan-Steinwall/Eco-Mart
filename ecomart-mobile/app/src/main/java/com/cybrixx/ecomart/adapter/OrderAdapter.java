package com.cybrixx.ecomart.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.OrderResponseDTO;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderResponseDTO> orderList;

    public OrderAdapter(Context context, List<OrderResponseDTO> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponseDTO order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getId());
        holder.tvOrderTotal.setText(String.format("Total: Rs. %.2f", order.getTotalAmount()));
        holder.tvOrderStatus.setText(order.getStatus());

        // Basic substring to clean up the Spring Boot LocalDateTime string
        String rawDate = order.getOrderDate();
        if (rawDate != null && rawDate.length() >= 10) {
            holder.tvOrderDate.setText("Date: " + rawDate.substring(0, 10));
        } else {
            holder.tvOrderDate.setText("Date: Unknown");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.cybrixx.ecomart.activity.TrackingActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            intent.putExtra("STATUS", order.getStatus());
            intent.putExtra("LATITUDE", order.getLatitude());
            intent.putExtra("LONGITUDE", order.getLongitude());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}
