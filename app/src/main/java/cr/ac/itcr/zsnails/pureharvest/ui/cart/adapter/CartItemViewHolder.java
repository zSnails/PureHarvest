package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import cr.ac.itcr.zsnails.pureharvest.databinding.ShoppingCartExpandableElementBinding;

public final class CartItemViewHolder extends ViewHolder {
    public ShoppingCartExpandableElementBinding binding;

    private CartItemViewHolder(ShoppingCartExpandableElementBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public static CartItemViewHolder from(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ShoppingCartExpandableElementBinding binding = ShoppingCartExpandableElementBinding.inflate(inflater, parent, false);
        return new CartItemViewHolder(binding);
    }
}
