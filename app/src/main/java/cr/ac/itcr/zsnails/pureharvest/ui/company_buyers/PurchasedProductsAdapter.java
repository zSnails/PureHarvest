package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

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

public class PurchasedProductsAdapter extends RecyclerView.Adapter<PurchasedProductsAdapter.ProductViewHolder> {

    private List<PurchasedProduct> productList;

    public PurchasedProductsAdapter(List<PurchasedProduct> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchased_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        PurchasedProduct product = productList.get(position);
        holder.productId.setText(product.getProductId());
        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
        holder.orderDate.setText(product.getFormattedDate());

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground) // Reemplaza con tu placeholder
                .error(R.drawable.ic_launcher_background) // Reemplaza con tu imagen de error
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productId, productName, productPrice, orderDate;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewProduct);
            productId = itemView.findViewById(R.id.textViewProductId);
            productName = itemView.findViewById(R.id.textViewProductName);
            productPrice = itemView.findViewById(R.id.textViewProductPrice);
            orderDate = itemView.findViewById(R.id.textViewOrderDate);
        }
    }

    public void updateData(List<PurchasedProduct> newProductList) {
        this.productList.clear();
        this.productList.addAll(newProductList);
        notifyDataSetChanged();
    }
}