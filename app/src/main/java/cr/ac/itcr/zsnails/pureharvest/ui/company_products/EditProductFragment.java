package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProductBinding;

public class EditProductFragment extends Fragment {

    private FragmentEditProductBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private String productId;

    private static final String TAG = "EditProductFragment";
    private static final String ARG_PRODUCT_ID = "productId";

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

        binding.backButtonEditProduct.setOnClickListener(v -> navigateBack());

        if (productId != null && !productId.isEmpty()) {
            loadProductData();
        } else {
            Log.e(TAG, "Cannot load data: Product ID is null or empty.");
            showErrorState("Error: ID de producto inválido.");
            setButtonsEnabled(false);
        }


        binding.buttonChangeImage.setOnClickListener(v -> handleManageImages());
        binding.buttonSave.setOnClickListener(v -> handleSaveChanges());
        binding.buttonCancel.setOnClickListener(v -> handleCancel());
        binding.buttonDeleteProduct.setOnClickListener(v -> handleDeleteProductConfirmation());
    }

    private void handleDeleteProductConfirmation() {
        if (getContext() == null || !isAdded() || productId == null || productId.isEmpty()) {
            Log.e(TAG, "Cannot delete: context, fragment state, or productId invalid.");
            showErrorState("No se puede eliminar el producto en este momento.");
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer y borrará también las imágenes asociadas.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Eliminar", (dialog, whichButton) -> performProductDeletion())
                .setNegativeButton("Cancelar", (dialog, whichButton) -> Log.d(TAG, "Product deletion cancelled by user."))
                .show();
    }
    private void performProductDeletion() {
        if (productId == null || productId.isEmpty() || firestore == null) {
            showErrorState("Error interno al intentar eliminar.");
            return;
        }
        showLoading(true);
        Log.d(TAG, "Attempting to delete product with ID: " + productId);
        firestore.collection("products").document(productId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore document deleted successfully for ID: " + productId);
                    deleteProductImagesFromStorage(); // Clean up storage afterwards
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error deleting Firestore document for ID: " + productId, e);
                    showErrorState("Error al eliminar datos del producto: " + e.getMessage());
                });
    }
    private void deleteProductImagesFromStorage() {
        if (productId == null || productId.isEmpty() || storage == null || !isAdded()) {
            Log.w(TAG, "Skipping storage deletion due to invalid state.");
            showLoading(false);
            showSuccessMessage("Datos del producto eliminados.");
            navigateBack();
            return;
        }
        StorageReference productImagesRef = storage.getReference().child("product_images/" + productId);
        Log.d(TAG, "Attempting to delete images in folder: " + productImagesRef.getPath());
        productImagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (!isAdded()) return;
                    List<StorageReference> items = listResult.getItems();
                    if (items.isEmpty()) {
                        Log.d(TAG, "No images found in storage for product ID: " + productId + ". Deletion complete.");
                        showLoading(false);
                        showSuccessMessage("Producto eliminado.");
                        navigateBack();
                        return;
                    }
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (StorageReference item : items) {
                        Log.d(TAG, "Adding delete task for: " + item.getPath());
                        deleteTasks.add(item.delete());
                    }
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded()) return;
                                Log.d(TAG, "All images deleted successfully from storage for product ID: " + productId);
                                showLoading(false);
                                showSuccessMessage("Producto e imágenes eliminados.");
                                navigateBack();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                showLoading(false);
                                Log.e(TAG, "Error deleting some images from storage for product ID: " + productId, e);
                                showErrorState("Producto eliminado, pero ocurrió un error al borrar imágenes.");
                                navigateBack();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    showLoading(false);
                    Log.e(TAG, "Error listing images in storage for product ID: " + productId, e);
                    showSuccessMessage("Producto eliminado (no se encontraron imágenes o error al listar).");
                    navigateBack();
                });
    }


    private void loadProductData() {
        if (productId == null || productId.isEmpty()) return;
        showLoading(true);
        firestore.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) return;
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        populateFields(documentSnapshot);
                        setButtonsEnabled(true);
                    } else {
                        showErrorState("El producto no existe.");
                        setButtonsEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    showLoading(false);
                    showErrorState("Error al cargar datos: " + e.getMessage());
                    binding.imageProduct.setImageResource(R.drawable.ic_error_image);
                    setButtonsEnabled(false);
                });
    }
    private void populateFields(DocumentSnapshot doc) {
        if (binding == null) return;
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
            binding.editProductPrice.setText(String.format(java.util.Locale.US, "%.2f", price));
        } else {
            binding.editProductPrice.setText("");
        }
        Object imageUrlsObj = doc.get("imageUrls");
        String imageUrlToLoad = null;
        if (imageUrlsObj instanceof List) {
            @SuppressWarnings("unchecked") List<Object> urlList = (List<Object>) imageUrlsObj;
            if (!urlList.isEmpty() && urlList.get(0) instanceof String && !TextUtils.isEmpty((String) urlList.get(0))) {
                imageUrlToLoad = (String) urlList.get(0);
            }
        } else if (imageUrlsObj instanceof String && !TextUtils.isEmpty((String) imageUrlsObj)) {
            imageUrlToLoad = (String) imageUrlsObj;
        }
        if (imageUrlToLoad != null && getContext() != null && isAdded()) {
            Glide.with(this).load(imageUrlToLoad).placeholder(R.drawable.ic_placeholder_image).error(R.drawable.ic_error_image).into(binding.imageProduct);
        } else {
            if(binding != null) binding.imageProduct.setImageResource(R.drawable.ic_placeholder_image);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void handleManageImages() {
        if (getContext() != null && isAdded() && getView() != null) {
            if (productId != null && !productId.isEmpty()) {
                Log.d(TAG, "Navigating to Manage Images for product ID: " + productId);
                // --- Navigation Logic ---
                Bundle args = new Bundle();
                args.putString("productId", productId); // Pass the product ID
                // Replace R.id.action_editProductFragment_to_manageImagesFragment with your actual action ID
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_editProductFragment_to_manageImagesFragment, args);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Navigation action not found. Ensure it's defined in your nav graph.", e);
                    showErrorState("Error de navegación.");
                }

            } else {
                showErrorState("ID de producto no válido para administrar imágenes.");
            }
        } else {
            Log.w(TAG, "Cannot navigate: context, view, or fragment state invalid.");
        }
    }



    private void handleSaveChanges() {
        if (binding == null || productId == null || productId.isEmpty()) {
            showErrorState(binding == null ? "Error interno (binding)" : "Error: ID de producto inválido.");
            return;
        }


        String name = binding.editProductName.getText().toString().trim();
        String priceStr = binding.editProductPrice.getText().toString().trim();
        boolean valid = true;
        if (name.isEmpty()) { binding.layoutProductName.setError("Nombre es requerido"); valid = false; } else { binding.layoutProductName.setError(null); }
        if (priceStr.isEmpty()) { binding.layoutProductPrice.setError("Precio es requerido"); valid = false; } else { binding.layoutProductPrice.setError(null); }
        double price = 0;
        if (valid) {
            try { price = Double.parseDouble(priceStr.replace(',', '.')); if (price < 0) throw new NumberFormatException(); binding.layoutProductPrice.setError(null); }
            catch (NumberFormatException e) { binding.layoutProductPrice.setError("Precio inválido"); valid = false; }
        }
        if (!valid) return;


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


        updateProductFirestoreOnlyText(productUpdates); // Call dedicated method
    }

    private void updateProductFirestoreOnlyText(Map<String, Object> productUpdates) {
        if (productId == null || productId.isEmpty() || firestore == null) {
            showLoading(false);
            showErrorState("Error interno al guardar.");
            return;
        }


        firestore.collection("products").document(productId)
                .update(productUpdates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    showSuccessMessage("Producto actualizado con éxito.");
                    // imageUri = null; // No longer needed
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error updating product text fields in Firestore for ID: " + productId, e);
                    showErrorState("Error al actualizar producto: " + e.getMessage());
                });
    }

    private void handleCancel() {
        // imageUri = null;
        navigateBack();
    }


    private void navigateBack() {
        if (getView() != null && isAdded()) { Navigation.findNavController(requireView()).navigateUp(); }
        else if (getActivity() != null) { getActivity().getSupportFragmentManager().popBackStack(); }
    }
    private void setButtonsEnabled(boolean enabled) {
        if (binding == null) return;
        binding.buttonSave.setEnabled(enabled);
        binding.buttonCancel.setEnabled(enabled);
        binding.buttonChangeImage.setEnabled(enabled);
        binding.buttonDeleteProduct.setEnabled(enabled);
        binding.backButtonEditProduct.setEnabled(enabled);
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
    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        setButtonsEnabled(!isLoading);
    }
    private void showErrorState(String message) {
        if(getContext() != null && isAdded()) Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
    private void showSuccessMessage(String message) {
        if(getContext() != null && isAdded()) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}