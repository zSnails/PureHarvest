package cr.ac.itcr.zsnails.pureharvest.ui.orders.client.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.ItemClientOrderBinding;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

public class OrderViewHolder extends RecyclerView.ViewHolder {
    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
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

    private String getStatusString(Integer status) {
        if (status == null) {
            return ctx.getString(R.string.status_not_available);
        }
        switch (status) {
            case 0:
                return ctx.getString(R.string.status_in_warehouse);
            case 1:
                return ctx.getString(R.string.status_on_the_way);
            case 2:
                return ctx.getString(R.string.status_delivered);
            default:
                return ctx.getString(R.string.status_not_available);
        }
    }

    @SuppressLint("SetTextI18n")
    public void bind(@NonNull final Order order) {
        this.binding.orderDate.setText(dateFormat.format(order.getDate().toDate()));
        this.binding.orderName.setText(ctx.getString(R.string.order_id_template, order.getDocumentId()));
        // TODO: poner el status correcto en base al tipo de orden
        this.binding.orderStatus.setText(getStatusString(order.getStatus()));
        // TODO: darle funcionalidad al view details button
        this.binding.viewDetailsButton.setOnClickListener(v -> {
            // TODO: add navigation mesh
            Bundle b = new Bundle();
            b.putSerializable("order", order);
            Navigation.findNavController(v).navigate(R.id.action_clientOrdersFragment2_to_clientOrderDetailsFragment, b);
        });
    }
}
