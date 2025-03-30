package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;


public final class ShoppingCartAdapter extends Adapter<Card> {
    private List<CartItem> items = new ArrayList<>();

    public ShoppingCartAdapter() {
        // NOTE: future self, I can use the item ids for some operations, I'll take a look at this
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<CartItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Card onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Card card = Card.from(parent);
        return card;
    }

    @Override
    public void onBindViewHolder(@NonNull Card holder, int position) {
        // TODO: show shopping cart data, the user does not yet have shopping cart information here
        holder.binding.productName.setText(items.get(position).productId);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewRecycled(@NonNull Card holder) {
        super.onViewRecycled(holder);
        holder.setState(new CardClosedState(holder));
    }
}

