package cr.ac.itcr.zsnails.pureharvest.ui.account.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import cr.ac.itcr.zsnails.pureharvest.databinding.FavoriteListCardItemBinding;
import cr.ac.itcr.zsnails.pureharvest.entities.FavoriteDisplayProduct;

public class Card extends RecyclerView.ViewHolder {
    private final FavoriteListCardItemBinding binding;
    private AddToCartListener listener;
    private ItemClickListener itemClickListener;

    private Card(@NonNull final FavoriteListCardItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public static Card from(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        FavoriteListCardItemBinding binding = FavoriteListCardItemBinding.inflate(inflater, parent, false);
        return new Card(binding);
    }

    public void bind(@NonNull final FavoriteDisplayProduct product) {
        this.binding.favoriteListCardItemName.setText(product.name);
        this.binding.favoriteListCardItemAddToShoppingCart
                .setOnClickListener(v -> listener.onAddToCart(product, getAdapterPosition()));
        this.binding.getRoot()
                .setOnClickListener(
                        v -> itemClickListener.onItemClick(product, getAdapterPosition()));
        Glide.with(binding.getRoot().getContext())
                .load(product.imageUrl)
                .fitCenter()
                .into(this.binding.favoriteCardItemImage);
    }

    public void setAddToCartListener(AddToCartListener listener) {
        this.listener = listener;
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface AddToCartListener {
        void onAddToCart(FavoriteDisplayProduct product, int position);
    }

    public interface ItemClickListener {
        void onItemClick(FavoriteDisplayProduct product, int position);
    }
}
