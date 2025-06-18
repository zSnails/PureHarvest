package cr.ac.itcr.zsnails.pureharvest.ui.account.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.entities.FavoriteDisplayProduct;

public class FavoriteItemsAdapter extends RecyclerView.Adapter<Card> {

    private Card.AddToCartListener listener;
    private Card.ItemClickListener itemClickListener;
    private List<FavoriteDisplayProduct> items = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<FavoriteDisplayProduct> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public FavoriteItemsAdapter(Card.AddToCartListener listener, Card.ItemClickListener itemClickListener) {
        setHasStableIds(false);
        this.listener = listener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public Card onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Card card = Card.from(parent);
        card.setAddToCartListener(listener);
        card.setItemClickListener(itemClickListener);
        return card;
    }

    @Override
    public void onBindViewHolder(@NonNull Card holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
