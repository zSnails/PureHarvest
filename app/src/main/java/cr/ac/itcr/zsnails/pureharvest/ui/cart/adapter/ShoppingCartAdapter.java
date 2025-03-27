package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import cr.ac.itcr.zsnails.pureharvest.R;

public final class ShoppingCartAdapter extends Adapter<CartItemViewHolder> {

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return CartItemViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 10;
    }
}
