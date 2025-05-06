package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Import ImageButton if finding manually, not needed if using binding directly
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController; // Import NavController
import androidx.navigation.Navigation; // Import Navigation

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProductBinding;

public class EditProductFragment extends Fragment {

    private FragmentEditProductBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private String productId;
    private Uri imageUri; // Store selected image URI

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
            Log.d(TAG, "Received Product ID: " + productId);
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


        binding.backButtonEditProduct.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });


        if (productId != null && !productId.isEmpty()) {
            loadProductData();
        } else {
            Log.e(TAG, "Cannot load data: Product ID is null or empty.");
            showErrorState("Error: ID de producto inválido.");
        }


        binding.buttonChangeImage.setOnClickListener(v -> handleChangeImage());
        binding.buttonSave.setOnClickListener(v -> handleSaveChanges());
        binding.buttonCancel.setOnClickListener(v -> handleCancel()); // Consider navigateUp() here too
    }

    private void loadProductData() {
        if (productId == null || productId.isEmpty()) return; // Guard clause

        showLoading(true);
        firestore.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) {
                        Log.w(TAG, "Fragment not attached or binding null after Firestore success.");
                        // No need to call showLoading(false) if binding is null
                        return;
                    }
                    showLoading(false);

                    if (documentSnapshot.exists()) {
                        populateFields(documentSnapshot);
                    } else {
                        Log.e(TAG, "Product document does not exist for ID: " + productId);
                        showErrorState("El producto no existe.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) {
                        Log.w(TAG, "Fragment not attached or binding null during Firestore failure.");
                        return;
                    }
                    showLoading(false);
                    Log.e(TAG, "Error fetching product data for ID: " + productId, e);
                    showErrorState("Error al cargar datos: " + e.getMessage());
                    // Set error image if load fails
                    binding.imageProduct.setImageResource(R.drawable.ic_error_image);
                });
    }


    private void populateFields(DocumentSnapshot doc) {
        if (binding == null) return; // Extra check

        binding.editProductName.setText(doc.getString("name"));
        binding.editProductType.setText(doc.getString("type"));
        binding.editProductDescription.setText(doc.getString("description"));
        binding.editProductIngredients.setText(doc.getString("ingredients"));
        binding.editProductPreparation.setText(doc.getString("preparation"));
        binding.editProductAcidity.setText(doc.getString("acidity"));
        binding.editProductBody.setText(doc.getString("body"));
        binding.editProductAftertaste.setText(doc.getString("aftertaste"));


        Double price = doc.getDouble("price");
        if (price != null) {
            binding.editProductPrice.setText(String.format("%.2f", price)); // Format for display
        } else {
            binding.editProductPrice.setText(""); // Or "0.00" or handle error
            Log.w(TAG, "Price field is missing or not a number for ID: " + productId);
        }


        Object imageUrlsObj = doc.get("imageUrls");
        String imageUrlToLoad = null;

        if (imageUrlsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> urlList = (List<Object>) imageUrlsObj;
            if (!urlList.isEmpty() && urlList.get(0) instanceof String && !TextUtils.isEmpty((String) urlList.get(0))) {
                imageUrlToLoad = (String) urlList.get(0);
                Log.d(TAG, "Image URL found (List, first element): " + imageUrlToLoad);
            } else {
                Log.w(TAG, "imageUrls field is an empty List or first element is not a valid String.");
            }
        } else if (imageUrlsObj instanceof String && !TextUtils.isEmpty((String) imageUrlsObj)) {
            imageUrlToLoad = (String) imageUrlsObj;
            Log.d(TAG, "Image URL found (String): " + imageUrlToLoad);
        } else {
            Log.w(TAG, "imageUrls field is null, missing, or not a List/String.");
        }


        if (imageUrlToLoad != null && getContext() != null) {
            Glide.with(requireContext())
                    .load(imageUrlToLoad)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(binding.imageProduct);
        } else {
            Log.w(TAG, "No valid image URL to load. Setting placeholder.");
            binding.imageProduct.setImageResource(R.drawable.ic_placeholder_image);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null && binding != null && getContext() != null ) {
            imageUri = data.getData(); // Store the selected image URI
            Log.d(TAG, "Image selected: " + imageUri.toString());
            Glide.with(requireContext())
                    .load(imageUri)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(binding.imageProduct);
        } else {
            Log.d(TAG, "onActivityResult: No image selected or fragment/binding/context is null.");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void handleChangeImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void handleSaveChanges() {
        if (binding == null) {
            Log.e(TAG, "handleSaveChanges: Binding is null!");
            return;
        }
        if (productId == null || productId.isEmpty()) {
            showErrorState("Error: ID de producto inválido.");
            return;
        }


        String name = binding.editProductName.getText().toString().trim();
        String priceStr = binding.editProductPrice.getText().toString().trim();

        if (name.isEmpty()) {
            binding.layoutProductName.setError("Nombre es requerido");
            return;
        } else {
            binding.layoutProductName.setError(null); // Clear error
        }

        if (priceStr.isEmpty()) {
            binding.layoutProductPrice.setError("Precio es requerido");
            return;
        } else {
            binding.layoutProductPrice.setError(null);
        }

        double price;
        try {

            price = Double.parseDouble(priceStr.replace(',', '.'));
            if (price < 0) throw new NumberFormatException("Price cannot be negative");
            binding.layoutProductPrice.setError(null); // Clear error on success
        } catch (NumberFormatException e) {
            binding.layoutProductPrice.setError("Precio inválido");
            Log.e(TAG, "Invalid price format: " + priceStr, e);
            return;
        }


        showLoading(true);


        Map<String, Object> productUpdates = new HashMap<>();
        productUpdates.put("name", name);
        productUpdates.put("type", binding.editProductType.getText().toString().trim());
        productUpdates.put("description", binding.editProductDescription.getText().toString().trim());
        productUpdates.put("ingredients", binding.editProductIngredients.getText().toString().trim());
        productUpdates.put("preparation", binding.editProductPreparation.getText().toString().trim());
        productUpdates.put("price", price);
        productUpdates.put("acidity", binding.editProductAcidity.getText().toString().trim());
        productUpdates.put("body", binding.editProductBody.getText().toString().trim());
        productUpdates.put("aftertaste", binding.editProductAftertaste.getText().toString().trim());




        if (imageUri != null) {

            uploadImageAndUpdateProduct(productUpdates);
        } else {

            updateProductFirestore(productUpdates);
        }
    }

    private void uploadImageAndUpdateProduct(Map<String, Object> productUpdates) {
        if (imageUri == null) {
            Log.w(TAG, "uploadImageAndUpdateProduct called with null imageUri");
            updateProductFirestore(productUpdates); // Still update other fields
            return;
        }
        if (storage == null) {
            Log.e(TAG, "FirebaseStorage instance is null!");
            showErrorState("Error de almacenamiento.");
            showLoading(false);
            return;
        }

        showLoading(true);

        StorageReference storageRef = storage.getReference();
        // Create a unique path/filename for the image
        String imagePath = "product_images/" + productId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imagePath);

        Log.d(TAG, "Uploading image to: " + imagePath);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image upload SUCCESS.");
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Log.d(TAG, "Image download URL obtained: " + uri.toString());


                                List<String> imageUrlsList = new ArrayList<>();
                                imageUrlsList.add(uri.toString());
                                productUpdates.put("imageUrls", imageUrlsList);


                                updateProductFirestore(productUpdates);
                            })
                            .addOnFailureListener(e -> {

                                showLoading(false);
                                Log.e(TAG, "Error getting download URL", e);
                                showErrorState("Error al obtener URL de imagen: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    // Failed to upload the image file
                    showLoading(false);
                    Log.e(TAG, "Error uploading image", e);
                    showErrorState("Error al subir imagen: " + e.getMessage());
                })
                .addOnProgressListener(snapshot -> {

                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d(TAG, "Upload is " + progress + "% done");

                });
    }

    private void updateProductFirestore(Map<String, Object> productUpdates) {
        if (productId == null || productId.isEmpty() || firestore == null) {
            Log.e(TAG, "Cannot update Firestore, invalid state (productId/firestore null/empty).");
            showLoading(false);
            showErrorState("Error interno al guardar.");
            return;
        }


        firestore.collection("products").document(productId)
                .update(productUpdates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d(TAG, "Product updated successfully in Firestore for ID: " + productId);
                    showSuccessMessage("Producto actualizado con éxito.");
                    imageUri = null;


                    if (getView() != null) {
                        Navigation.findNavController(requireView()).navigateUp();
                    } else if (getActivity() != null) {

                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error updating product in Firestore for ID: " + productId, e);
                    showErrorState("Error al actualizar producto: " + e.getMessage());
                });
    }

    private void handleCancel() {

        imageUri = null;
        if (getView() != null) {
            Navigation.findNavController(requireView()).navigateUp();
        } else if (getActivity() != null) {

            getActivity().getSupportFragmentManager().popBackStack();
        }
    }


    private void showLoading(boolean isLoading) {
        if (binding == null) {
            Log.w(TAG, "showLoading called but binding is null.");
            return;
        }
        Log.d(TAG, "Setting loading state: " + isLoading);

        binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        // Disable/Enable interactive elements
        binding.buttonSave.setEnabled(!isLoading);
        binding.buttonCancel.setEnabled(!isLoading);
        binding.buttonChangeImage.setEnabled(!isLoading);
        binding.editProductName.setEnabled(!isLoading);
        binding.editProductType.setEnabled(!isLoading);
        binding.editProductDescription.setEnabled(!isLoading);
        binding.editProductIngredients.setEnabled(!isLoading);
        binding.editProductPreparation.setEnabled(!isLoading);
        binding.editProductPrice.setEnabled(!isLoading);
        binding.editProductAcidity.setEnabled(!isLoading);
        binding.editProductBody.setEnabled(!isLoading);
        binding.editProductAftertaste.setEnabled(!isLoading);

    }

    private void showErrorState(String message) {
        if(getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    
    private void showSuccessMessage(String message) {
        if(getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

}