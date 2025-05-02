package cr.ac.itcr.zsnails.pureharvest.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import cr.ac.itcr.zsnails.pureharvest.R;

import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.databinding.ItemProductBinding;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;

    public ProductAdapter(List<Product> products) {
        this.products = products;
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
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private TextView productName;
        private TextView productPrice;
        private Context ctx;
        private ItemProductBinding itemProductBinding;

        public ProductViewHolder(View itemView) {
            super(itemView);
            this.ctx = itemView.getContext();
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            ImageView addToCart = itemView.findViewById(R.id.addToCartButton);

            addToCart.setOnClickListener(v -> {
                // TODO: Implement Add to Cart action
            });

            itemView.setOnClickListener(v -> {
                // TODO: Navigate to product detail activity
            });
        }

        public void bind(Product product) {
            productName.setText(product.getName());
            productPrice.setText(this.ctx.getString(R.string.colones, product.getPrice()));
            Glide.with(productImage.getContext()).load(product.getFirstImageUrl()).into(productImage);
        }
    }
}
