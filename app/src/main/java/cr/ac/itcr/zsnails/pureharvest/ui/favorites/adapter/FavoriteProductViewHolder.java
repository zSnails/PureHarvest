package cr.ac.itcr.zsnails.pureharvest.ui.favorites.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.databinding.ItemProductBinding;
import cr.ac.itcr.zsnails.pureharvest.ui.client.ViewProductActivity;

public class FavoriteProductViewHolder extends RecyclerView.ViewHolder {

    private final ItemProductBinding binding;
    private final Context ctx;

    private FavoriteProductViewHolder(final ItemProductBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.ctx = binding.getRoot().getContext();
    }

    public static FavoriteProductViewHolder from(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProductBinding binding = ItemProductBinding
                .inflate(inflater, parent, false);
        return new FavoriteProductViewHolder(binding);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    private OnAddToCartListener listener;
    public void setOnAddToCartListener(OnAddToCartListener listener) {
        this.listener = listener;
    }

    public void bind(@NonNull final Product product) {
        this.binding.productName.setText(product.getName());
        if (product.getSaleDiscount() > 0) {
            this.binding.productBadge.setVisibility(View.VISIBLE);
            this.binding.productBadge.setText(ctx.getString(R.string.discount_format, (int) (product.getSaleDiscount() * 100)));
            this.binding.productOriginalPrice.setText(String.valueOf(product.getPrice()));
            this.binding.productOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            this.binding.productBadge.setVisibility(View.INVISIBLE);
            this.binding.productOriginalPrice.setVisibility(View.INVISIBLE);
        }
        this.binding.productPrice.setText(String.valueOf(product.getPrice() * (1 - product.getSaleDiscount())));
        this.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(ctx, ViewProductActivity.class);
            intent.putExtra("product_id", product.getId());
            ctx.startActivity(intent);
        });
        this.binding.addToCartButton.setOnClickListener(v -> {
            this.listener.onAddToCart(product);
        });
        Glide.with(ctx).load(product.getFirstImageUrl()).into(this.binding.productImage);
    }
}
