package cr.ac.itcr.zsnails.pureharvest.ui.orders.client.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

public class ClientOrdersAdapter extends RecyclerView.Adapter<OrderViewHolder>{
    private List<Order> orders;

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return OrderViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
