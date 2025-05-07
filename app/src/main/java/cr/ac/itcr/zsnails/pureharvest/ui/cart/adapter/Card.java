package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.bumptech.glide.Glide;

import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.Stateful;
import cr.ac.itcr.zsnails.pureharvest.databinding.ShoppingCartExpandableElementBinding;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;


public final class Card extends ViewHolder implements OnClickListener, Stateful<CardState> {
    @NonNull
    public final ShoppingCartExpandableElementBinding binding;
    private final Context ctx;
    private CardState state;
    private AmountTapListener cb;

    private Card(final ShoppingCartExpandableElementBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.ctx = binding.getRoot().getContext();
        this.setState(new CardClosedState(this));
        this.binding.getRoot().setOnClickListener(this);
    }

    public static Card from(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ShoppingCartExpandableElementBinding binding = ShoppingCartExpandableElementBinding
                .inflate(inflater, parent, false);
        return new Card(binding);
    }

    public void bind(@NonNull final Item item) {
        this.binding.productName.setText(item.getName());
        this.binding.cartItemAmount.setText(
                String.format(Locale.getDefault(), "%d", item.getAmount()));
        this.binding.cartItemAmount.setOnClickListener(
                (View view) -> cb.onAmountTap(item, getAdapterPosition()));
        this.binding.productTypeDetail.setText(item.getType());
        this.binding.productPriceDetail.setText(ctx.getString(R.string.colones, item.getPrice()));
        Glide.with(ctx).load(item.getImage()).into(this.binding.productImageBig);
        Glide.with(ctx).load(item.getImage()).into(this.binding.productImageSmall);
    }

    public void setAmountTapListener(AmountTapListener cb) {
        this.cb = cb;
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

    public interface AmountTapListener {
        void onAmountTap(Item item, int position);
    }
}
