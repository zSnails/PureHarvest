package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProductBinding;

public class EditProductFragment extends Fragment {

    private FragmentEditProductBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private String productId;
    private Uri imageUri;
    private String currentImageUrl; // To store the existing image URL

    private static final String TAG = "EditProductFragment";
    private static final String ARG_PRODUCT_ID = "productId";
    private static final int IMAGE_PICK_CODE = 1000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
        } else {
            Log.e(TAG, "Product ID not provided in arguments.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (productId != null && !productId.isEmpty()) {
            loadProductData();
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: No se pudo obtener el ID del producto.", Toast.LENGTH_LONG).show();
            }
        }

        binding.buttonChangeImage.setOnClickListener(v -> handleChangeImage());
        binding.buttonSave.setOnClickListener(v -> handleSaveChanges());
        binding.buttonCancel.setOnClickListener(v -> handleCancel());
    }

    private void loadProductData() {
        showLoading(true);
        firestore.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists() && getContext() != null) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            binding.editProductName.setText(getStringValue(data, "name"));
                            binding.editProductType.setText(getStringValue(data, "type")); // Assuming 'type' exists now
                            binding.editProductDescription.setText(getStringValue(data, "description"));
                            binding.editProductIngredients.setText(getStringValue(data, "ingredients"));
                            binding.editProductPreparation.setText(getStringValue(data, "preparation"));
                            binding.editProductAcidity.setText(getStringValue(data, "acidity"));
                            binding.editProductBody.setText(getStringValue(data, "body"));
                            binding.editProductAftertaste.setText(getStringValue(data, "aftertaste"));

                            Object priceObj = data.get("price");
                            if (priceObj instanceof Number) {
                                binding.editProductPrice.setText(String.format("%.2f", ((Number) priceObj).doubleValue())); // Format price
                            } else {
                                binding.editProductPrice.setText("");
                            }

                            currentImageUrl = getStringValue(data, "imageUrls");
                            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(currentImageUrl)
                                        //.placeholder(R.drawable.image_product) // Use your placeholder
                                        //.error(R.drawable.error_image) // Use your error image
                                        .into(binding.imageProduct);
                            } else {
                                //binding.imageProduct.setImageResource(R.drawable.placeholder_image); // Default if no URL
                            }

                        } else {
                            Log.e(TAG, "Product data is null for ID: " + productId);
                            Toast.makeText(getContext(), "No se encontraron datos para este producto.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Product document does not exist for ID: " + productId);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "El producto no existe.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error fetching product data for ID: " + productId, e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return "";
        return String.valueOf(value); // Convert any non-null object to String
    }


    private void handleChangeImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void handleSaveChanges() {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(getContext(), "Error: ID de producto inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String name = binding.editProductName.getText().toString().trim();
        String type = binding.editProductType.getText().toString().trim();
        String description = binding.editProductDescription.getText().toString().trim();
        String ingredients = binding.editProductIngredients.getText().toString().trim();
        String preparation = binding.editProductPreparation.getText().toString().trim();
        String priceStr = binding.editProductPrice.getText().toString().trim();
        String acidity = binding.editProductAcidity.getText().toString().trim();
        String body = binding.editProductBody.getText().toString().trim();
        String aftertaste = binding.editProductAftertaste.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Nombre y Precio son requeridos.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Precio inválido.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        Map<String, Object> productUpdates = new HashMap<>();
        productUpdates.put("name", name);
        productUpdates.put("type", type); // Assuming type field exists
        productUpdates.put("description", description);
        productUpdates.put("ingredients", ingredients);
        productUpdates.put("preparation", preparation);
        productUpdates.put("price", price);
        productUpdates.put("acidity", acidity);
        productUpdates.put("body", body);
        productUpdates.put("aftertaste", aftertaste);
        // Add other fields like certifications, flavors if needed

        if (imageUri != null) {
            uploadImageAndUpdateProduct(productUpdates);
        } else {
            updateProductFirestore(productUpdates);
        }
    }

    private void uploadImageAndUpdateProduct(Map<String, Object> productUpdates) {
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("product_images/" + productId + "/" + UUID.randomUUID().toString());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            productUpdates.put("imageUrls", uri.toString());
                            updateProductFirestore(productUpdates);
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Log.e(TAG, "Error getting download URL", e);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Error al obtener URL de imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error uploading image", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateProductFirestore(Map<String, Object> productUpdates) {
        firestore.collection("products").document(productId)
                .update(productUpdates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Producto actualizado con éxito.", Toast.LENGTH_SHORT).show();
                    }
                    // Optionally navigate back
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error updating product", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al actualizar producto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void handleCancel() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Operación Cancelada", Toast.LENGTH_SHORT).show();
        }
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void showLoading(boolean isLoading) {
        if (binding != null) { // Check if binding is still valid
            // Implement your loading indicator logic here
            // e.g., binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Disable/Enable buttons to prevent multi-clicks during load
            binding.buttonSave.setEnabled(!isLoading);
            binding.buttonCancel.setEnabled(!isLoading);
            binding.buttonChangeImage.setEnabled(!isLoading);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null && getContext() != null) {
            imageUri = data.getData();
            Glide.with(requireContext()).load(imageUri).into(binding.imageProduct);
        }
    }

}