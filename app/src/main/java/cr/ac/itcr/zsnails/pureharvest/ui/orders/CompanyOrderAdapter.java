package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
// Order model import is correct

public class CompanyOrderAdapter extends RecyclerView.Adapter<CompanyOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
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
            holder.orderDate.setText(context.getString(R.string.not_available_short));
        }

        String orderDocumentIdString = order.getDocumentId() != null ?
                context.getString(R.string.order_doc_id_prefix) + order.getDocumentId() : // Usar el ID del documento
                context.getString(R.string.order_doc_id_prefix) + context.getString(R.string.not_available_short);
        holder.orderName.setText(orderDocumentIdString);


        String userIdString = order.getUserId() != null ?
                context.getString(R.string.order_user_prefix) + order.getUserId() :
                context.getString(R.string.order_user_prefix) + context.getString(R.string.not_available_short);
        holder.orderUserName.setText(userIdString);

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
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDate, orderName, orderUserName;
        Button viewDetailsButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderName = itemView.findViewById(R.id.orderName); // Mostrará ID del Documento de la Orden
            orderUserName = itemView.findViewById(R.id.orderUserName); // Mostrará ID del Usuario
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}