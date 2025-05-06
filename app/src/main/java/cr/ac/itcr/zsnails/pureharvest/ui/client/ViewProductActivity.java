package cr.ac.itcr.zsnails.pureharvest.ui.client;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import androidx.core.content.ContextCompat;

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
    private Button btnIncrease, btnDecrease;
    private TextView tvQuantity;
    private int quantity = 1;
    private double unitPrice = 0;
    private Button btnAddToCart;
    private ImageButton btnFavorite;
    private boolean isFavorite = false;

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
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnFavorite.setOnClickListener(v -> toggleFavorite());
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

        unitPrice = product.price;
        updateQuantityAndPrice();

        tvQuantity.setText(String.valueOf(quantity));

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            updateQuantityAndPrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityAndPrice();
            }
        });
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
    private void updateQuantityAndPrice() {
        tvQuantity.setText(String.valueOf(quantity));
        double totalPrice = unitPrice * quantity;
        productPrice.setText(String.format("₡%.2f", totalPrice));
        btnAddToCart.setText("Add " + quantity + " to Cart");
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        btnFavorite.setColorFilter(
                ContextCompat.getColor(this, isFavorite ? R.color.red : android.R.color.darker_gray)
        );
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}