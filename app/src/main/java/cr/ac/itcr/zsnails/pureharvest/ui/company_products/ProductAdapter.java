package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import cr.ac.itcr.zsnails.pureharvest.R;
import java.util.List;

import androidx.navigation.Navigation;
import android.widget.Button;
import android.os.Bundle;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;

    public ProductAdapter(List<Product> products) {
        this.products = products;
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.enterprise_item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private TextView productName;
        private TextView productPrice;

        private Button manageButton;


        public ProductViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);

            manageButton = itemView.findViewById(R.id.manageButton);


        }

        public void bind(Product product) {
            productName.setText(product.getName());
            productPrice.setText("₡" + (int) product.getPrice());
            Glide.with(productImage.getContext()).load(product.getFirstImageUrl()).into(productImage);

            manageButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("productId", product.getId());

                Navigation.findNavController(v).navigate(
                        R.id.action_companyProductsListFragment_to_editProductFragment, bundle
                );
            });
        }

    }
}