package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.app.Activity;
import android.content.DialogInterface; // Import DialogInterface
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;        // Import Task
import com.google.android.gms.tasks.Tasks;       // Import Tasks
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;    // Import ListResult
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

        // Back Button Listener
        binding.backButtonEditProduct.setOnClickListener(v -> navigateBack());

        // Load initial data
        if (productId != null && !productId.isEmpty()) {
            loadProductData();
        } else {
            Log.e(TAG, "Cannot load data: Product ID is null or empty.");
            showErrorState("Error: ID de producto inválido.");
            setButtonsEnabled(false); // Disable buttons if ID is invalid
        }

        // Setup Button Listeners
        // Changed button text -> action should now lead to image management
        binding.buttonChangeImage.setOnClickListener(v -> handleManageImages());
        binding.buttonSave.setOnClickListener(v -> handleSaveChanges());
        binding.buttonCancel.setOnClickListener(v -> handleCancel());
        binding.buttonDeleteProduct.setOnClickListener(v -> handleDeleteProductConfirmation()); // Call confirmation method

    }

    // Renamed the original handleDeleteProduct to show the confirmation dialog first
    private void handleDeleteProductConfirmation() {
        if (getContext() == null || !isAdded() || productId == null || productId.isEmpty()) {
            Log.e(TAG, "Cannot delete: context, fragment state, or productId invalid.");
            showErrorState("No se puede eliminar el producto en este momento.");
            return;
        }

        // Show Confirmation Dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer y borrará también las imágenes asociadas.")
                .setIcon(android.R.drawable.ic_dialog_alert) // Warning icon
                .setPositiveButton("Eliminar", (dialog, whichButton) -> {
                    // User clicked "Eliminar" - Proceed with actual deletion
                    performProductDeletion();
                })
                .setNegativeButton("Cancelar", (dialog, whichButton) -> {
                    // User clicked "Cancelar" - Do nothing, dialog dismisses automatically
                    Log.d(TAG, "Product deletion cancelled by user.");
                })
                .show();
    }

    // This method now performs the actual deletion after confirmation
    private void performProductDeletion() {
        if (productId == null || productId.isEmpty() || firestore == null) {
            showErrorState("Error interno al intentar eliminar.");
            return;
        }
        showLoading(true); // Show loading indicator during deletion
        Log.d(TAG, "Attempting to delete product with ID: " + productId);

        // --- Step 1: Delete Firestore Document ---
        firestore.collection("products").document(productId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore document deleted successfully for ID: " + productId);
                    // --- Step 2 (Optional but recommended): Delete associated images from Storage ---
                    // The success message and navigation back will happen inside deleteProductImagesFromStorage
                    deleteProductImagesFromStorage();
                })
                .addOnFailureListener(e -> {
                    showLoading(false); // Hide loading on failure
                    Log.e(TAG, "Error deleting Firestore document for ID: " + productId, e);
                    showErrorState("Error al eliminar datos del producto: " + e.getMessage());
                    // Don't navigate back on failure here, let user retry or cancel
                });
    }


    // --- Optional but Recommended: Delete Images from Storage ---
    private void deleteProductImagesFromStorage() {
        // Checks for valid state
        if (productId == null || productId.isEmpty() || storage == null || !isAdded()) {
            Log.w(TAG, "Skipping storage deletion due to invalid state.");
            showLoading(false); // Ensure loading is hidden if skipped
            // Show success for Firestore deletion and navigate back
            showSuccessMessage("Datos del producto eliminados.");
            navigateBack();
            return;
        }

        // Reference to the product's image folder (adjust path if needed)
        StorageReference productImagesRef = storage.getReference().child("product_images/" + productId);
        Log.d(TAG, "Attempting to delete images in folder: " + productImagesRef.getPath());

        productImagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (!isAdded()) return; // Check again before proceeding

                    List<StorageReference> items = listResult.getItems();
                    if (items.isEmpty()) {
                        Log.d(TAG, "No images found in storage for product ID: " + productId + ". Deletion complete.");
                        showLoading(false);
                        showSuccessMessage("Producto eliminado.");
                        navigateBack();
                        return; // Exit function
                    }

                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (StorageReference item : items) {
                        Log.d(TAG, "Adding delete task for: " + item.getPath());
                        deleteTasks.add(item.delete());
                    }

                    // Wait for all delete tasks to complete
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded()) return; // Final check
                                Log.d(TAG, "All images deleted successfully from storage for product ID: " + productId);
                                showLoading(false);
                                showSuccessMessage("Producto e imágenes eliminados.");
                                navigateBack();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                showLoading(false);
                                Log.e(TAG, "Error deleting some images from storage for product ID: " + productId, e);
                                // Inform user, but still navigate back as main data is gone
                                showErrorState("Producto eliminado, pero ocurrió un error al borrar imágenes.");
                                navigateBack();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    showLoading(false);
                    Log.e(TAG, "Error listing images in storage for product ID: " + productId, e);
                    // Usually okay if folder just doesn't exist.
                    showSuccessMessage("Producto eliminado (no se encontraron imágenes o error al listar).");
                    navigateBack(); // Navigate back even if listing failed
                });
    }


    // --- Load/Populate/onActivityResult/onDestroyView remain the same ---
    private void loadProductData() {
        if (productId == null || productId.isEmpty()) return; // Guard clause

        showLoading(true);
        firestore.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) {
                        Log.w(TAG, "Fragment not attached or binding null after Firestore success.");
                        return;
                    }
                    showLoading(false); // Hide loading indicator

                    if (documentSnapshot.exists()) {
                        populateFields(documentSnapshot);
                        setButtonsEnabled(true); // Enable buttons after successful load
                    } else {
                        Log.e(TAG, "Product document does not exist for ID: " + productId);
                        showErrorState("El producto no existe.");
                        setButtonsEnabled(false); // Disable buttons if product doesn't exist
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
                    binding.imageProduct.setImageResource(R.drawable.ic_error_image); // Show error image
                    setButtonsEnabled(false); // Disable buttons on load failure
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
            binding.editProductPrice.setText(String.format(java.util.Locale.US, "%.2f", price)); // Use Locale.US for dot decimal separator
        } else {
            binding.editProductPrice.setText("");
            Log.w(TAG, "Price field is missing or not a number for ID: " + productId);
        }

        Object imageUrlsObj = doc.get("imageUrls");
        String imageUrlToLoad = null;

        if (imageUrlsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> urlList = (List<Object>) imageUrlsObj;
            if (!urlList.isEmpty() && urlList.get(0) instanceof String && !TextUtils.isEmpty((String) urlList.get(0))) {
                imageUrlToLoad = (String) urlList.get(0);
            }
        } else if (imageUrlsObj instanceof String && !TextUtils.isEmpty((String) imageUrlsObj)) {
            imageUrlToLoad = (String) imageUrlsObj;
        }

        if (imageUrlToLoad != null && getContext() != null && isAdded()) {
            Glide.with(this)
                    .load(imageUrlToLoad)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(binding.imageProduct);
        } else {
            Log.w(TAG, "No valid image URL to load or context/fragment invalid. Setting placeholder.");
            if(binding != null) {
                binding.imageProduct.setImageResource(R.drawable.ic_placeholder_image);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null && binding != null && getContext() != null && isAdded()) {
            imageUri = data.getData();
            Log.d(TAG, "Image selected: " + imageUri.toString());
            Glide.with(this)
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

    // This button now needs to handle "Manage Images"
    private void handleManageImages() {
        // TODO: Implement navigation to your image management fragment/activity
        if (getContext() != null && isAdded()) {
            if (productId != null && !productId.isEmpty()) {
                Log.d(TAG, "Navigating to Manage Images for product ID: " + productId);
                // Example using Navigation Component (replace with your actual action/destination ID)
                // Bundle args = new Bundle();
                // args.putString("productId", productId);
                // Navigation.findNavController(requireView()).navigate(R.id.action_editProductFragment_to_manageImagesFragment, args);
                Toast.makeText(getContext(), "Ir a Administrar Imágenes (ID: " + productId + ")", Toast.LENGTH_SHORT).show(); // Placeholder
            } else {
                showErrorState("ID de producto no válido para administrar imágenes.");
            }
        }
    }

    // Renamed original handleChangeImage to specifically handle picking *one* image for replacement during edit.
    // If 'Administrar Imágenes' button does image picking, this might become redundant or change.
    private void handlePickReplacementImage() { // Maybe rename buttonChangeImage handler if needed
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }


    // --- handleSaveChanges, uploadImageAndUpdateProduct, updateProductFirestore remain the same ---
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

        boolean valid = true;
        if (name.isEmpty()) {
            binding.layoutProductName.setError("Nombre es requerido");
            valid = false;
        } else {
            binding.layoutProductName.setError(null);
        }

        if (priceStr.isEmpty()) {
            binding.layoutProductPrice.setError("Precio es requerido");
            valid = false;
        } else {
            binding.layoutProductPrice.setError(null);
        }

        double price = 0;
        if (valid) {
            try {
                price = Double.parseDouble(priceStr.replace(',', '.'));
                if (price < 0) throw new NumberFormatException("Price cannot be negative");
                binding.layoutProductPrice.setError(null);
            } catch (NumberFormatException e) {
                binding.layoutProductPrice.setError("Precio inválido");
                Log.e(TAG, "Invalid price format: " + priceStr, e);
                valid = false;
            }
        }

        if (!valid) {
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

        // Only upload/update image if a *new* one was selected via onActivityResult
        if (imageUri != null) {
            uploadImageAndUpdateProduct(productUpdates);
        } else {
            // No new image selected, just update text fields
            // Crucially, DO NOT overwrite imageUrls if no new image was chosen
            updateProductFirestore(productUpdates);
        }
    }

    private void uploadImageAndUpdateProduct(Map<String, Object> productUpdates) {
        if (imageUri == null) {
            updateProductFirestore(productUpdates);
            return;
        }
        if (storage == null) {
            showErrorState("Error de almacenamiento.");
            showLoading(false);
            return;
        }

        showLoading(true);

        StorageReference storageRef = storage.getReference();
        String imagePath = "product_images/" + productId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imagePath);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            List<String> imageUrlsList = new ArrayList<>();
                            imageUrlsList.add(uri.toString());
                            // IMPORTANT: This decides if you REPLACE all images or ADD to existing.
                            // For an "Edit" screen where user picks ONE replacement, replacing is common.
                            // If "Manage Images" handles adding/deleting multiple, this might only *set* the primary.
                            // Assuming replace for now:
                            productUpdates.put("imageUrls", imageUrlsList);
                            updateProductFirestore(productUpdates);
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Log.e(TAG, "Error getting download URL", e);
                            showErrorState("Error al obtener URL de imagen: " + e.getMessage());
                        }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error uploading image", e);
                    showErrorState("Error al subir imagen: " + e.getMessage());
                });
    }

    private void updateProductFirestore(Map<String, Object> productUpdates) {
        if (productId == null || productId.isEmpty() || firestore == null) {
            showLoading(false);
            showErrorState("Error interno al guardar.");
            return;
        }

        firestore.collection("products").document(productId)
                .update(productUpdates) // Use update, not set, to avoid deleting fields not included
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    showSuccessMessage("Producto actualizado con éxito.");
                    imageUri = null; // Reset local URI state
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error updating product in Firestore for ID: " + productId, e);
                    showErrorState("Error al actualizar producto: " + e.getMessage());
                });
    }


    private void handleCancel() {
        imageUri = null;
        navigateBack();
    }

    // --- Helper Methods ---
    private void navigateBack() {
        if (getView() != null && isAdded()) {
            Navigation.findNavController(requireView()).navigateUp();
        } else if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // Helper to enable/disable all interactive buttons
    private void setButtonsEnabled(boolean enabled) {
        if (binding == null) return;
        binding.buttonSave.setEnabled(enabled);
        binding.buttonCancel.setEnabled(enabled);
        binding.buttonChangeImage.setEnabled(enabled); // This is now "Manage Images"
        binding.buttonDeleteProduct.setEnabled(enabled);
        binding.backButtonEditProduct.setEnabled(enabled);
        // Also enable/disable input fields? Optional, but good UX during load/save
        binding.editProductName.setEnabled(enabled);
        binding.editProductType.setEnabled(enabled);
        binding.editProductDescription.setEnabled(enabled);
        binding.editProductIngredients.setEnabled(enabled);
        binding.editProductPreparation.setEnabled(enabled);
        binding.editProductPrice.setEnabled(enabled);
        binding.editProductAcidity.setEnabled(enabled);
        binding.editProductBody.setEnabled(enabled);
        binding.editProductAftertaste.setEnabled(enabled);
    }

    // Updated loading state handler
    private void showLoading(boolean isLoading) {
        if (binding == null) {
            Log.w(TAG, "showLoading called but binding is null.");
            return;
        }
        Log.d(TAG, "Setting loading state: " + isLoading);
        binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        setButtonsEnabled(!isLoading); // Use helper to manage button states
    }

    private void showErrorState(String message) {
        if(getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccessMessage(String message) {
        if(getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}