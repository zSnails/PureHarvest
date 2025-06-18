package cr.ac.itcr.zsnails.pureharvest.ui.orders.client.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cr.ac.itcr.zsnails.pureharvest.databinding.ItemClientOrderBinding;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

public class OrderViewHolder extends RecyclerView.ViewHolder {
    @NonNull
    public final ItemClientOrderBinding binding;

    private final Context ctx;

    private OrderViewHolder(final ItemClientOrderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.ctx = binding.getRoot().getContext();
    }

    public static OrderViewHolder from(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemClientOrderBinding binding = ItemClientOrderBinding
                .inflate(inflater, parent, false);
        return new OrderViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    public void bind(@NonNull final Order order) {
        this.binding.orderDate.setText(order.getDate().toString());
        this.binding.orderName.setText(order.getDocumentId());
        // TODO: poner el status correcto en base al tipo de orden
        this.binding.orderStatus.setText(order.getStatus().toString());
        // TODO: darle funcionalidad al view details button
        // this.binding.viewDetailsButton
    }
}
