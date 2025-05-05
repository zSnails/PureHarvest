package cr.ac.itcr.zsnails.pureharvest.ui.client;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.model.Product;

public class ViewProductActivity extends AppCompatActivity {

    private ImageView imageMain;
    private LinearLayout miniImagesContainer;
    private TextView productName, productDescription, productType, ratingCount, productPrice;
    private RatingBar productRating;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Details");
        }

        productId = getIntent().getStringExtra("product_id");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        initViews();
        imageMain.setOnClickListener(v -> showZoomDialog());
        loadProductFromFirestore();
    }

    private void initViews() {
        imageMain = findViewById(R.id.imageMain);
        miniImagesContainer = findViewById(R.id.miniImagesContainer);
        productName = findViewById(R.id.productName);
        productDescription = findViewById(R.id.productDescription);
        productType = findViewById(R.id.productType);
        productRating = findViewById(R.id.productRating);
        ratingCount = findViewById(R.id.ratingCount);
        productPrice = findViewById(R.id.productPrice);
    }

    private void loadProductFromFirestore() {
        firestore.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            updateUI(product);
                        }
                    } else {
                        Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUI(Product product) {
        productName.setText(product.name);
        productDescription.setText(product.description);
        productType.setText(product.type);
        productRating.setRating((float) product.rating);
        ratingCount.setText(String.format("(%.1f)", product.rating));
        productPrice.setText("₡" + product.price);

        // Main image
        if (product.imageUrls != null && !product.imageUrls.isEmpty()) {
            Glide.with(this).load(product.imageUrls.get(0)).into(imageMain);
        }

        // Mini images
        miniImagesContainer.removeAllViews();
        if (product.imageUrls != null) {
            for (String url : product.imageUrls) {
                ImageView mini = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
                params.setMargins(8, 0, 8, 0);
                mini.setLayoutParams(params);
                mini.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(this).load(url).into(mini);

                mini.setOnClickListener(v -> Glide.with(this).load(url).into(imageMain));

                miniImagesContainer.addView(mini);
            }
        }

    }

    private void showZoomDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_image_zoom);

        ImageView zoomedImage = dialog.findViewById(R.id.zoomedImage);
        zoomedImage.setImageDrawable(imageMain.getDrawable());

        zoomedImage.setOnClickListener(v -> dialog.dismiss()); //close when you touches

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}