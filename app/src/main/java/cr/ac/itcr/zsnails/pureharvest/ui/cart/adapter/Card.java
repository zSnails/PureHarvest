package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.Stateful;
import cr.ac.itcr.zsnails.pureharvest.databinding.ShoppingCartExpandableElementBinding;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;


public final class Card extends ViewHolder implements OnClickListener, Stateful<CardState> {
    @NonNull
    public final ShoppingCartExpandableElementBinding binding;
    private final Context ctx;
    private CardState state;

    private Card(final ShoppingCartExpandableElementBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.ctx = binding.getRoot().getContext();
        this.setState(new CardClosedState(this));
        this.binding.getRoot().setOnClickListener(this);
    }

    public static Card from(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ShoppingCartExpandableElementBinding binding = ShoppingCartExpandableElementBinding.inflate(inflater, parent, false);
        return new Card(binding);
    }

    // TODO: bind the data once I get an actual database model
    public void bind(@NonNull final Item item) {
        this.binding.productName.setText(item.getName());
        this.binding.productTypeDetail.setText(item.getType());
        this.binding.productPriceDetail.setText(ctx.getString(R.string.colones, item.getPrice()));
    }

    public CardState getState() {
        return this.state;
    }

    public void setState(final CardState state) {
        this.state = state;
    }

    @Override
    public void onClick(final View view) {
        this.state.onClick();
    }
}
