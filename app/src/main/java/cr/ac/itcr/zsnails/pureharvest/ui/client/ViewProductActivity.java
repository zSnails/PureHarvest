package cr.ac.itcr.zsnails.pureharvest.ui.client;

import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.ShoppingCartViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import cr.ac.itcr.zsnails.pureharvest.MainActivity;
import cr.ac.itcr.zsnails.pureharvest.ui.client.ImageSliderAdapter;

@AndroidEntryPoint
public class ViewProductActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ImageView imageMain;
    private LinearLayout miniImagesContainer;
    private TextView productName, productDescription, productType, ratingCount, productPrice;
    private RatingBar productRating;
    private String productId;
    private Button btnIncrease, btnDecrease;
    private TextView tvQuantity;
    private int quantity = 1;
    private double unitPrice = 0;
    private Button btnAddToCart;
    private ImageButton btnFavorite;
    private boolean isFavorite = false;
    private LinearLayout optionalFieldsContainer;
    private ShoppingCartViewModel shoppingCartViewModel;
    private ViewProductViewModel viewProductViewModel;
    private Button btnViewProfile;
    private Product currentProduct;
    private ViewPager2 imageSlider;
    private ImageButton btnPrev, btnNext;
    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);
        shoppingCartViewModel = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        viewProductViewModel = new ViewModelProvider(this).get(ViewProductViewModel.class);

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
        viewProductViewModel.productId = productId;
        viewProductViewModel.favorite.observe(this, (fav) -> {
            btnFavorite.setImageResource(fav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            btnFavorite.setColorFilter(
                    ContextCompat.getColor(this, fav ? R.color.red : android.R.color.darker_gray)
            );
        });
        try {
            viewProductViewModel.loadFavoriteStatus();
        } catch (UserNotAuthenticatedException e) {
            throw new RuntimeException(e);
        }

        initViews();
        loadProductFromFirestore();
    }

    private void initViews() {
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
        optionalFieldsContainer = findViewById(R.id.optionalFieldsContainer);
        imageSlider = findViewById(R.id.imageSlider);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        btnViewProfile = findViewById(R.id.btnViewProfile);
        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProductActivity.this, MainActivity.class);
            intent.putExtra("navigate_to", "company_contact");
            intent.putExtra("company_id", currentProduct.getSellerId()); // usa sellerId como companyId
            startActivity(intent);
        });
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
        productName.setText(product.getName());
        productDescription.setText(product.getDescription());
        productType.setText(product.getType());
        productRating.setRating((float) product.getRating());
        ratingCount.setText(String.format("(%.1f)", product.getRating()));
        productPrice.setText("₡" + product.getPrice());
        this.currentProduct = product;

        unitPrice = product.getPrice();
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

        btnAddToCart.setOnClickListener(v -> {
            CartItem item = new CartItem();
            item.productId = product.getId();
            item.amount = quantity;

            shoppingCartViewModel.insertItem(item);
            Toast.makeText(this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
        });

        // Main image
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(this, product.getImageUrls(), this::showZoomDialog);
            imageSlider.setAdapter(adapter);
            this.imageUrls = product.getImageUrls();

            imageSlider.setOnClickListener(v -> {
                int currentIndex = imageSlider.getCurrentItem();
                if (currentIndex >= 0 && imageUrls != null && currentIndex < imageUrls.size()) {
                    showZoomDialog(imageUrls.get(currentIndex));
                }
            });


            btnPrev.setOnClickListener(v -> {
                int current = imageSlider.getCurrentItem();
                if (current > 0) {
                    imageSlider.setCurrentItem(current - 1, true);
                }
            });

            btnNext.setOnClickListener(v -> {
                int current = imageSlider.getCurrentItem();
                if (current < adapter.getItemCount() - 1) {
                    imageSlider.setCurrentItem(current + 1, true);
                }
            });
        }

        // Mini images
        miniImagesContainer.removeAllViews();
        if (product.getImageUrls() != null) {
            for (int i = 0; i < product.getImageUrls().size(); i++) {
                String url = product.getImageUrls().get(i);
                int finalI = i;

                ImageView mini = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
                params.setMargins(8, 0, 8, 0);
                mini.setLayoutParams(params);
                mini.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(this).load(url).into(mini);

                mini.setOnClickListener(v -> imageSlider.setCurrentItem(finalI, true));

                miniImagesContainer.addView(mini);
            }
        }
        optionalFieldsContainer.removeAllViews();
        addOptionalField("Certifications", product.getCertifications());
        addOptionalField("Flavors", product.getFlavors());
        addOptionalField("Acidity", product.getAcidity());
        addOptionalField("Body", product.getBody());
        addOptionalField("Aftertaste", product.getAftertaste());
        addOptionalField("Ingredients", product.getIngredients());
        addOptionalField("Preparation", product.getPreparation());
    }

    private void showZoomDialog(String imageUrl) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_image_zoom);

        ImageView zoomedImage = dialog.findViewById(R.id.zoomedImage);
        Glide.with(this).load(imageUrl).into(zoomedImage);

        zoomedImage.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addOptionalField(String label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            TextView labelView = new TextView(this);
            labelView.setText(label);
            labelView.setTextAppearance(android.R.style.TextAppearance_Medium);
            labelView.setTypeface(null, android.graphics.Typeface.BOLD);
            labelView.setPadding(0, 12, 0, 0);

            TextView valueView = new TextView(this);
            valueView.setText(value);
            valueView.setTextAppearance(android.R.style.TextAppearance_Small);

            optionalFieldsContainer.addView(labelView);
            optionalFieldsContainer.addView(valueView);
        }
    }

    private void updateQuantityAndPrice() {
        tvQuantity.setText(String.valueOf(quantity));
        double totalPrice = unitPrice * quantity;
        productPrice.setText(String.format("₡%.2f", totalPrice));
        btnAddToCart.setText("Add " + quantity + " to Cart");
    }

    private void toggleFavorite() {
        this.isFavorite = !isFavorite; // esto es un hack terrible, esto no debería estar aquí, isFavorite mínimo debería ser el mismo que en el view model
                                       // (esto lo hizo fabs originalmente)
        viewProductViewModel.favorite.setValue(this.isFavorite);
        try {
            viewProductViewModel.toggleFavorite();
        } catch (UserNotAuthenticatedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
