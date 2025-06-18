package cr.ac.itcr.zsnails.pureharvest.ui.orders.client.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.domain.repository.ClientOrdersRepository;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.client.ClientOrdersViewModel;

public class ClientOrderItemsAdapter extends RecyclerView.Adapter<OrderItemViewHolder> {
    private List<ClientOrdersRepository.OrderDisplayItem> orderItems = new ArrayList<>();
    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return OrderItemViewHolder.from(parent);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<ClientOrdersRepository.OrderDisplayItem> items) {
        this.orderItems = items;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        holder.bind(orderItems.get(position));
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }
}
