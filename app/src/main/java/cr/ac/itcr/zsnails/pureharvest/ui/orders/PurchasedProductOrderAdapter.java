package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;

public class PurchasedProductOrderAdapter extends RecyclerView.Adapter<PurchasedProductOrderAdapter.ProductOrderViewHolder> {

    private List<PurchasedProductOrder> productList;

    public PurchasedProductOrderAdapter(List<PurchasedProductOrder> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchased_product_order, parent, false);
        return new ProductOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductOrderViewHolder holder, int position) {
        PurchasedProductOrder product = productList.get(position);
        holder.productId.setText(holder.itemView.getContext().getString(R.string.product_id_prefix_display, product.getProductId()));
        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format(Locale.US, "₡%.2f", product.getPrice()));
        holder.productQuantity.setText(holder.itemView.getContext().getString(R.string.product_quantity_prefix_display, product.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductOrderViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productId, productName, productPrice, productQuantity;

        public ProductOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewProductOrder);
            productId = itemView.findViewById(R.id.textViewProductIdOrder);
            productName = itemView.findViewById(R.id.textViewProductNameOrder);
            productPrice = itemView.findViewById(R.id.textViewProductPriceOrder);
            productQuantity = itemView.findViewById(R.id.textViewProductQuantityOrder);
        }
    }

    public void updateData(List<PurchasedProductOrder> newProductList) {
        this.productList.clear();
        this.productList.addAll(newProductList);
        notifyDataSetChanged();
    }
}