package cr.ac.itcr.zsnails.pureharvest.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.databinding.ItemProductBinding;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private AddToCartListener listener;
    private OnProductClickListener productClickListener;
    private boolean showRating;
    private boolean showDiscount;

    public ProductAdapter(List<Product> products, AddToCartListener listener, OnProductClickListener productClickListener, boolean showRating, boolean showDiscount) {
        this.products = products;
        this.listener = listener;
        this.productClickListener = productClickListener;
        this.showRating = showRating;
        this.showDiscount = showDiscount;
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.setAddToCartListener(listener);
        holder.bind(products.get(position), showRating);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public interface AddToCartListener {
        void onClick(Product product);
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView productBadge;
        private final Context ctx;
        private Product me;
        private ItemProductBinding itemProductBinding;
        private AddToCartListener cb;

        public ProductViewHolder(View itemView) {
            super(itemView);
            this.ctx = itemView.getContext();
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productBadge = itemView.findViewById(R.id.productBadge);
            ImageView addToCart = itemView.findViewById(R.id.addToCartButton);

            addToCart.setOnClickListener(v -> cb.onClick(me));

            itemView.setOnClickListener(v -> {
                if (productClickListener != null && me != null) {
                    productClickListener.onProductClick(me);
                }
            });
        }


        public void setAddToCartListener(AddToCartListener cb) {
            this.cb = cb;
        }

        public void bind(Product product, boolean showRating) {
            this.me = product;
            productName.setText(product.getName());
            productPrice.setText(this.ctx.getString(R.string.colones, product.getPrice()));
            if (showRating) {
                productBadge.setVisibility(View.VISIBLE);
                productBadge.setText(this.ctx.getString(R.string.rating_format, product.getRating()));
            } else if (showDiscount && product.getSaleDiscount() != null) {
                productBadge.setVisibility(View.VISIBLE);
                int discountPercentage = (int) (product.getSaleDiscount() * 100);
                productBadge.setText(this.ctx.getString(R.string.discount_format, discountPercentage));
            }
            else {
                productBadge.setVisibility(View.GONE);
            }
            Glide.with(productImage.getContext()).load(product.getFirstImageUrl()).into(productImage);
        }
    }
}
