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

    public void removeAt(Integer idx) {
        CartItem item = items.get(idx);
        items.remove(item);
        notifyItemRemoved(idx);
    }

    @Override
    public void onBindViewHolder(@NonNull Card holder, int position) {
        // TODO: show shopping cart data, the user does not yet have shopping cart information here
        // here I won't be using this, but the actual bind method I defined in the Card view holder
        CartItem current = items.get(position);
        holder.binding.productName.setText(current.productId);
        if (current.amount != null) {
            holder.binding.cartItemAmount.setText(current.amount.toString());
        }
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

