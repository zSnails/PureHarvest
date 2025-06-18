package cr.ac.itcr.zsnails.pureharvest.ui.orders.client.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.ItemPurchasedProductOrderBinding;
import cr.ac.itcr.zsnails.pureharvest.domain.repository.ClientOrdersRepository;

public class OrderItemViewHolder extends RecyclerView.ViewHolder {

    private final Context ctx;
    private final ItemPurchasedProductOrderBinding binding;

    private OrderItemViewHolder(final ItemPurchasedProductOrderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.ctx = binding.getRoot().getContext();
    }

    public static OrderItemViewHolder from(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPurchasedProductOrderBinding binding = ItemPurchasedProductOrderBinding
                .inflate(inflater, parent, false);
        return new OrderItemViewHolder(binding);
    }

    public void bind(@NonNull final ClientOrdersRepository.OrderDisplayItem item) {
        this.binding.textViewProductIdOrder.setText(item.productId);
        this.binding.textViewProductNameOrder.setText(item.name);
        this.binding.textViewProductPriceOrder.setText(ctx.getString(R.string.colones, item.price));
        this.binding.textViewProductQuantityOrder.setText(String.valueOf(item.amount));
        Glide.with(ctx).load(item.imageUrl).into(this.binding.imageViewProductOrder);
    }

}