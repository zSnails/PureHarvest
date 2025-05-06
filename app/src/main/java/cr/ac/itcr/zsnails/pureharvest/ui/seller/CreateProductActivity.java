package cr.ac.itcr.zsnails.pureharvest.ui.seller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import android.widget.LinearLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.firebase.ProductUploader;
import cr.ac.itcr.zsnails.pureharvest.model.Product;

public class CreateProductActivity extends AppCompatActivity {

    private EditText nameEditText, ratingEditText, priceEditText, descriptionEditText;
    private EditText certEditText, flavorsEditText, acidityEditText, bodyEditText,
            aftertasteEditText, ingredientsEditText, preparationEditText;
    private Spinner typeSpinner;
    private LinearLayout coffeeExtrasContainer, imagePreviewContainer;
    private Button btnSelectImages, btnCreateProduct;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private final int MAX_IMAGES = 5;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);

        // App bar back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Crear producto");
        }

        initViews();
        setupSpinner();
        setupImagePicker();

        btnSelectImages.setOnClickListener(v -> pickImages());

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = typeSpinner.getSelectedItem().toString();

                if (selectedType.equalsIgnoreCase("Café")) {
                    coffeeExtrasContainer.setVisibility(View.VISIBLE);
                } else {
                    coffeeExtrasContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnCreateProduct.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String type = typeSpinner.getSelectedItem().toString();
            String ratingStr = ratingEditText.getText().toString().trim();
            String priceStr = priceEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (type.equals("Seleccione un tipo de producto")) {
                Toast.makeText(this, "Debe seleccionar un tipo de producto", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.isEmpty() || ratingStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Nombre, puntuación y precio son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            double rating;
            double price;
            try {
                rating = Double.parseDouble(ratingStr);
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Formato inválido en puntuación o precio", Toast.LENGTH_SHORT).show();
                return;
            }

            String cert = "", flavors = "", acidity = "", body = "", aftertaste = "", ingredients = "", prep = "";
            if (type.equalsIgnoreCase("Café")) {
                cert = certEditText.getText().toString().trim();
                flavors = flavorsEditText.getText().toString().trim();
                acidity = acidityEditText.getText().toString().trim();
                body = bodyEditText.getText().toString().trim();
                aftertaste = aftertasteEditText.getText().toString().trim();
                ingredients = ingredientsEditText.getText().toString().trim();
                prep = preparationEditText.getText().toString().trim();
            }

            String sellerId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "unknown";

            Product product = new Product(
                    name, type, rating, price, description,
                    null,
                    sellerId,
                    cert, flavors, acidity, body, aftertaste, ingredients, prep
            );

            ProductUploader uploader = new ProductUploader();
            btnCreateProduct.setEnabled(false);
            btnCreateProduct.setText("Subiendo...");

            uploader.uploadProduct(this, product, selectedImageUris, new ProductUploader.UploadCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(CreateProductActivity.this, "Producto creado exitosamente", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(CreateProductActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnCreateProduct.setEnabled(true);
                    btnCreateProduct.setText("Crear producto");
                }
            });
        });
    }

    private void initViews() {
        nameEditText = findViewById(R.id.editTextProductName);
        ratingEditText = findViewById(R.id.editTextRating);
        priceEditText = findViewById(R.id.editTextPrice);
        descriptionEditText = findViewById(R.id.editTextDescription);
        typeSpinner = findViewById(R.id.spinnerProductType);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnCreateProduct = findViewById(R.id.btnCreateProduct);
        coffeeExtrasContainer = findViewById(R.id.coffeeExtrasContainer);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);

        certEditText = findViewById(R.id.editTextCertifications);
        flavorsEditText = findViewById(R.id.editTextFlavors);
        acidityEditText = findViewById(R.id.editTextAcidity);
        bodyEditText = findViewById(R.id.editTextBody);
        aftertasteEditText = findViewById(R.id.editTextAftertaste);
        ingredientsEditText = findViewById(R.id.editTextIngredients);
        preparationEditText = findViewById(R.id.editTextPreparation);
    }

    private void setupSpinner() {
        String[] productTypes = {
                "Seleccione un tipo de producto",
                "Café", "Miel", "Hortaliza", "Especialidad", "Gourmet", "Base/Normal", "Orgánico"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                productTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count && selectedImageUris.size() < MAX_IMAGES; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                selectedImageUris.add(imageUri);
                            }
                        } else if (result.getData().getData() != null) {
                            Uri imageUri = result.getData().getData();
                            if (selectedImageUris.size() < MAX_IMAGES) {
                                selectedImageUris.add(imageUri);
                            }
                        }
                        showImagePreviews();
                    }
                }
        );
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Selecciona imágenes"));
    }

    private void showImagePreviews() {
        imagePreviewContainer.removeAllViews();
        for (Uri uri : selectedImageUris) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
            params.setMargins(8, 0, 8, 0);
            imageView.setLayoutParams(params);
            Glide.with(this).load(uri).into(imageView);
            imagePreviewContainer.addView(imageView);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}