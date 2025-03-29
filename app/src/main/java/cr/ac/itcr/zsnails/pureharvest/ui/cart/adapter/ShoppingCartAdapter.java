package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;


public final class ShoppingCartAdapter extends Adapter<Card> {
    public ShoppingCartAdapter() {
        // NOTE: future self, I can use the item ids for some operations, I'll take a look at this
        // setHasStableIds(true);
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
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    @Override
    public void onViewRecycled(@NonNull Card holder) {
        super.onViewRecycled(holder);
        holder.setState(new CardClosedState(holder));
    }
}

