package cr.ac.itcr.zsnails.pureharvest.ui.orders; // Correct package

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Changed from MaterialButton to Button for simplicity if not specifically needed
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
// import cr.ac.itcr.zsnails.pureharvest.models.Order; // Old import
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order; // Updated import for Order model

public class CompanyOrderAdapter extends RecyclerView.Adapter<CompanyOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    // Consider making dateFormat static or pass Locale if needed for different regions
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onViewDetailsClick(Order order);
    }

    public CompanyOrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (order.getDate() != null) {
            holder.orderDate.setText(dateFormat.format(order.getDate().toDate()));
        } else {
            holder.orderDate.setText(context.getString(R.string.not_available_short)); // Using string resource
        }

        // Using productId as the "orderName"
        holder.orderName.setText(order.getProductId() != null ?
                context.getString(R.string.order_name_prefix) + order.getProductId() :
                context.getString(R.string.order_name_prefix) + context.getString(R.string.not_available_short));

        holder.orderUserName.setText(order.getUserId() != null ?
                context.getString(R.string.order_user_prefix) + order.getUserId() :
                context.getString(R.string.order_user_prefix) + context.getString(R.string.not_available_short));

        holder.viewDetailsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailsClick(order);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        notifyDataSetChanged(); // For simplicity. Consider DiffUtil for larger lists.
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDate, orderName, orderUserName;
        Button viewDetailsButton; // Matched to item_order.xml (MaterialButton can be cast to Button)

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderName = itemView.findViewById(R.id.orderName);
            orderUserName = itemView.findViewById(R.id.orderUserName);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton); // Ensure this ID is in item_order.xml
        }
    }
}