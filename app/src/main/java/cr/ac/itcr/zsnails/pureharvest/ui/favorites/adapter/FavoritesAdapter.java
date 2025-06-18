package cr.ac.itcr.zsnails.pureharvest.ui.favorites.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoriteProductViewHolder> {
    private List<Product> products = new ArrayList<>();

    @NonNull
    @Override
    public FavoriteProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FavoriteProductViewHolder.from(parent);
    }

    private FavoriteProductViewHolder.OnAddToCartListener listener;

    public FavoritesAdapter(FavoriteProductViewHolder.OnAddToCartListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<Product> items) {
        this.products = items;
        Log.d("recycler:view", String.format("Data size: %d", this.products.size()));
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteProductViewHolder holder, int position) {
        holder.bind(this.products.get(position));
        holder.setOnAddToCartListener(listener);
    }

    @Override
    public int getItemCount() {
        return this.products.size();
    }
}
