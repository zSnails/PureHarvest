package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;

public class CompanyOrderAdapter extends RecyclerView.Adapter<CompanyOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private OnOrderClickListener listener;
    private FirebaseFirestore db;


    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onViewDetailsClick(Order order);
    }

    public CompanyOrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
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
            holder.orderDate.setText(context.getString(R.string.not_available_short));
        }

        String orderDocumentIdString = order.getDocumentId() != null ?
                context.getString(R.string.order_doc_id_prefix) + order.getDocumentId() :
                context.getString(R.string.order_doc_id_prefix) + context.getString(R.string.not_available_short);
        holder.orderName.setText(orderDocumentIdString);


        String userId = order.getUserId();
        if (userId != null && !userId.isEmpty()) {
            holder.orderUserName.setText(context.getString(R.string.order_user_prefix) + context.getString(R.string.loading_text_placeholder));
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        // Check if the holder is still valid for this position
                        if (holder.getAdapterPosition() != position && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                            return; // ViewHolder has been recycled or position is no longer valid
                        }
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                User user = document.toObject(User.class);
                                if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                                    holder.orderUserName.setText(context.getString(R.string.order_user_prefix) + user.getFullName());
                                } else {
                                    holder.orderUserName.setText(context.getString(R.string.order_user_prefix) + context.getString(R.string.not_available_short));
                                }
                            } else {
                                holder.orderUserName.setText(context.getString(R.string.order_user_prefix) + context.getString(R.string.user_not_found_short));
                            }
                        } else {
                            Log.e("CompanyOrderAdapter", "Error fetching user: ", task.getException());
                            holder.orderUserName.setText(context.getString(R.string.order_user_prefix) + context.getString(R.string.error_loading_user_short));
                        }
                    });
        } else {
            holder.orderUserName.setText(context.getString(R.string.order_user_prefix) + context.getString(R.string.not_available_short));
        }


        Integer status = order.getStatus();
        holder.orderStatus.setText(getStatusString(status));

        if (status != null) {
            switch (status) {
                case 1:
                    holder.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    break;
                case 2:
                    holder.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.leaf_green));
                    break;
                case 0:
                default:
                    holder.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_on_background));
                    break;
            }
        } else {
            holder.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_on_background));
        }

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

    private String getStatusString(Integer status) {
        if (status == null) {
            return context.getString(R.string.status_not_available);
        }
        switch (status) {
            case 0:
                return context.getString(R.string.status_in_warehouse);
            case 1:
                return context.getString(R.string.status_on_the_way);
            case 2:
                return context.getString(R.string.status_delivered);
            default:
                return context.getString(R.string.status_not_available);
        }
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDate, orderName, orderUserName, orderStatus;
        Button viewDetailsButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderName = itemView.findViewById(R.id.orderName);
            orderUserName = itemView.findViewById(R.id.orderUserName);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}