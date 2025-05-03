package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.entities.CartDisplayItem;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.ShoppingCartViewModel;


public final class ShoppingCartAdapter extends Adapter<Card> implements ShoppingCartViewModel.ItemOperationEventListener {
    private final Card.AmountTapListener amountTapListener;
    private List<CartDisplayItem> items = new ArrayList<>();

    public ShoppingCartAdapter(Card.AmountTapListener listener) {
        setHasStableIds(true);
        this.amountTapListener = listener;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<CartDisplayItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Card onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Card card = Card.from(parent);
        card.setAmountTapListener(amountTapListener);
        return card;
    }

    public void removeAt(Integer idx) {
        Item item = items.get(idx);
        items.remove(item);
        notifyItemRemoved(idx);
    }

    @Override
    public void onBindViewHolder(@NonNull Card holder, int position) {
        Item current = items.get(position);
        holder.bind(current);
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

    @Override
    public void onItemCreated(Item item) {
    }

    @Override
    public void onItemRemoved(Item item, int position) {
        notifyItemRemoved(position);
    }

    @Override
    public void onItemUpdated(Item item) {
        int idx = this.items.indexOf(item);
        notifyItemChanged(idx, item);
    }
}

