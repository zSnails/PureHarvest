package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger; // For managing uploads

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentManageImagesBinding;

public class ManageImagesFragment extends Fragment {

    private FragmentManageImagesBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String productId;
    private List<String> currentImageUrls = new ArrayList<>(); // Store current URLs

    // Arrays to hold references to UI elements for easier access
    private ImageView[] imageSlots = new ImageView[5];
    private Button[] changeButtons = new Button[5];
    private Button[] deleteButtons = new Button[5];
    private ProgressBar[] progressBars = new ProgressBar[5];
    private TextView[] emptyTexts = new TextView[5];

    private int currentPickingSlot = -1; // Track which slot is being picked for

    private static final String TAG = "ManageImagesFragment";
    private static final String ARG_PRODUCT_ID = "productId";
    private static final int IMAGE_PICK_CODE_BASE = 2000; // Base request code

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
            Log.d(TAG, "Received Product ID: " + productId);
            if (productId == null || productId.isEmpty()) {
                Log.e(TAG, "Product ID is invalid.");
                // Handle error - maybe navigate back or show permanent error
            }
        } else {
            Log.e(TAG, "Product ID not provided in arguments.");
            // Handle error
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageImagesBinding.inflate(inflater, container, false);
        initializeUIReferences();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButtonManageImages.setOnClickListener(v -> navigateBack());

        if (productId != null && !productId.isEmpty()) {
            loadInitialImages();
            setupButtonListeners();
        } else {
            showErrorState("Error: ID de producto no válido.");
            // Disable all buttons or show an error view
        }
    }

    // Helper to initialize arrays of UI elements
    private void initializeUIReferences() {
        imageSlots[0] = binding.imageSlot1;
        imageSlots[1] = binding.imageSlot2;
        imageSlots[2] = binding.imageSlot3;
        imageSlots[3] = binding.imageSlot4;
        imageSlots[4] = binding.imageSlot5;

        changeButtons[0] = binding.buttonChange1;
        changeButtons[1] = binding.buttonChange2;
        changeButtons[2] = binding.buttonChange3;
        changeButtons[3] = binding.buttonChange4;
        changeButtons[4] = binding.buttonChange5;

        deleteButtons[0] = binding.buttonDelete1;
        deleteButtons[1] = binding.buttonDelete2;
        deleteButtons[2] = binding.buttonDelete3;
        deleteButtons[3] = binding.buttonDelete4;
        deleteButtons[4] = binding.buttonDelete5;

        progressBars[0] = binding.progressSlot1;
        progressBars[1] = binding.progressSlot2;
        progressBars[2] = binding.progressSlot3;
        progressBars[3] = binding.progressSlot4;
        progressBars[4] = binding.progressSlot5;

        emptyTexts[0] = binding.textEmpty1;
        emptyTexts[1] = binding.textEmpty2;
        emptyTexts[2] = binding.textEmpty3;
        emptyTexts[3] = binding.textEmpty4;
        emptyTexts[4] = binding.textEmpty5;
    }

    private void setupButtonListeners() {
        for (int i = 0; i < 5; i++) {
            final int slotIndex = i; // Need final variable for lambda
            changeButtons[i].setOnClickListener(v -> handlePickImage(slotIndex));
            deleteButtons[i].setOnClickListener(v -> handleDeleteImageConfirmation(slotIndex));
        }
    }

    private void loadInitialImages() {
        if (productId == null || productId.isEmpty()) return;
        showSlotLoading(-1, true); // Show general loading initially? Maybe not needed.

        firestore.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) return;

                    currentImageUrls.clear(); // Clear previous data
                    if (documentSnapshot.exists()) {
                        Object urlsObj = documentSnapshot.get("imageUrls");
                        if (urlsObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Object> rawList = (List<Object>) urlsObj;
                            for(Object item : rawList) {
                                if(item instanceof String && !((String)item).isEmpty()){
                                    currentImageUrls.add((String)item);
                                }
                            }
                            Log.d(TAG, "Loaded " + currentImageUrls.size() + " image URLs from Firestore.");
                        } else {
                            Log.w(TAG, "'imageUrls' field is not a List or is missing.");
                        }
                    } else {
                        Log.w(TAG, "Product document " + productId + " does not exist.");
                        showErrorState("Producto no encontrado."); // Or handle differently
                    }
                    updateUISlots(); // Update UI based on loaded URLs
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    Log.e(TAG, "Error fetching product data for images", e);
                    showErrorState("Error al cargar imágenes: " + e.getMessage());
                    updateUISlots(); // Update UI to show empty state
                });
    }

    // Updates all 5 slots based on the currentImageUrls list
    private void updateUISlots() {
        if (!isAdded() || binding == null) return;

        for (int i = 0; i < 5; i++) {
            if (i < currentImageUrls.size()) {
                // Image exists for this slot
                String url = currentImageUrls.get(i);
                if (!TextUtils.isEmpty(url)) {
                    setSlotState(i, url, false); // Load image, hide empty text
                } else {
                    setSlotState(i, null, true); // Treat empty URL as empty slot
                }
            } else {
                // No image for this slot
                setSlotState(i, null, true); // Show empty state
            }
            progressBars[i].setVisibility(View.GONE); // Ensure progress is hidden
        }
    }

    // Sets the state (image or empty) for a single slot
    private void setSlotState(int slotIndex, @Nullable String imageUrl, boolean isEmpty) {
        if (!isAdded() || slotIndex < 0 || slotIndex >= 5) return;

        if (isEmpty || TextUtils.isEmpty(imageUrl)) {
            // Set empty state
            imageSlots[slotIndex].setImageResource(0); // Clear image
            imageSlots[slotIndex].setBackgroundColor(getResources().getColor(R.color.placeholder_grey)); // Use color placeholder
            emptyTexts[slotIndex].setVisibility(View.VISIBLE);
            changeButtons[slotIndex].setText("Añadir"); // Change button text
            changeButtons[slotIndex].setVisibility(View.VISIBLE); // Show "Añadir"
            deleteButtons[slotIndex].setVisibility(View.GONE);    // Hide "Eliminar"
        } else {
            // Load image and set occupied state
            emptyTexts[slotIndex].setVisibility(View.GONE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image) // Use drawable placeholders
                    .error(R.drawable.ic_error_image)
                    .into(imageSlots[slotIndex]);
            changeButtons[slotIndex].setText("Cambiar"); // Set button text
            changeButtons[slotIndex].setVisibility(View.VISIBLE); // Show "Cambiar"
            deleteButtons[slotIndex].setVisibility(View.VISIBLE); // Show "Eliminar"
        }
        // Ensure progress bar is hidden unless explicitly shown by upload/delete
        progressBars[slotIndex].setVisibility(View.GONE);
        changeButtons[slotIndex].setEnabled(true); // Ensure buttons are enabled
        deleteButtons[slotIndex].setEnabled(true);
    }

    // Handles clicking "Cambiar" or "Añadir"
    private void handlePickImage(int slotIndex) {
        currentPickingSlot = slotIndex; // Remember which slot we're picking for
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // Use unique request code for each slot to potentially handle multiple picks
        startActivityForResult(intent, IMAGE_PICK_CODE_BASE + slotIndex);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ReqCode=" + requestCode + ", ResCode=" + resultCode);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode >= IMAGE_PICK_CODE_BASE && requestCode < IMAGE_PICK_CODE_BASE + 5) {
                int slotIndex = requestCode - IMAGE_PICK_CODE_BASE; // Determine slot from request code
                Uri selectedImageUri = data.getData();
                Log.d(TAG, "Image picked for slot " + slotIndex + ": " + selectedImageUri.toString());

                // Start upload process for the selected image and slot
                uploadImageForSlot(slotIndex, selectedImageUri);
            } else {
                Log.w(TAG,"onActivityResult: Unknown request code " + requestCode);
            }
        } else {
            Log.d(TAG, "onActivityResult: No image selected or result not OK.");
        }
    }


    private void uploadImageForSlot(int slotIndex, @NonNull Uri imageUri) {
        if (productId == null || productId.isEmpty() || storage == null || !isAdded() || slotIndex < 0 || slotIndex >= 5) {
            showErrorState("Error al iniciar subida.");
            return;
        }

        showSlotLoading(slotIndex, true); // Show progress for this specific slot

        StorageReference storageRef = storage.getReference();
        String imagePath = "product_images/" + productId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imagePath);

        Log.d(TAG, "Uploading image for slot " + slotIndex + " to: " + imagePath);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Upload success for slot " + slotIndex);
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                if (!isAdded()) return;
                                Log.d(TAG, "Download URL for slot " + slotIndex + ": " + downloadUri.toString());
                                updateFirestoreWithNewUrl(slotIndex, downloadUri.toString()); // Update Firestore with new URL
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                showSlotLoading(slotIndex, false);
                                Log.e(TAG, "Error getting download URL for slot " + slotIndex, e);
                                showErrorState("Error al obtener URL de imagen.");
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    showSlotLoading(slotIndex, false);
                    Log.e(TAG, "Error uploading image for slot " + slotIndex, e);
                    showErrorState("Error al subir imagen.");
                });
    }

    // Updates Firestore after successful upload
    private void updateFirestoreWithNewUrl(int slotIndex, String newUrl) {
        if (productId == null || productId.isEmpty() || firestore == null || !isAdded()) return;

        DocumentReference productRef = firestore.collection("products").document(productId);

        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(productRef);
            List<String> existingUrls = new ArrayList<>();
            Object urlsObj = snapshot.get("imageUrls");

            if (urlsObj instanceof List) {
                // Filter only valid strings from the existing list
                @SuppressWarnings("unchecked") List<Object> rawList = (List<Object>) urlsObj;
                for(Object item : rawList){
                    if(item instanceof String && !((String)item).isEmpty()){
                        existingUrls.add((String)item);
                    }
                }
            }

            // Decide whether to add or replace based on slotIndex
            if (slotIndex < existingUrls.size()) {
                // Replace existing URL - Also delete the old image from storage
                String oldUrl = existingUrls.get(slotIndex);
                deleteImageFromStorage(oldUrl); // Attempt to delete the old one
                existingUrls.set(slotIndex, newUrl); // Replace with new URL
                Log.d(TAG,"Replacing URL at index " + slotIndex);
            } else if (slotIndex < 5) { // Ensure we don't exceed max slots
                // Add new URL (if list size is less than slot index + 1)
                while(existingUrls.size() <= slotIndex && existingUrls.size() < 5){
                    existingUrls.add(null); // Pad with nulls if necessary (though direct add should work)
                }
                if(existingUrls.size() > slotIndex){
                    existingUrls.set(slotIndex, newUrl); // If padding worked
                } else {
                    existingUrls.add(newUrl); // Add normally if within bounds
                }

                Log.d(TAG, "Adding new URL at index " + slotIndex);
            } else {
                Log.e(TAG,"Slot index " + slotIndex + " is out of bounds (max 5).");
                throw new FirebaseFirestoreException("Slot index out of bounds", FirebaseFirestoreException.Code.INVALID_ARGUMENT);
            }

            // Remove any trailing nulls that might have been added during padding
            while (!existingUrls.isEmpty() && existingUrls.get(existingUrls.size() - 1) == null) {
                existingUrls.remove(existingUrls.size() - 1);
            }


            transaction.update(productRef, "imageUrls", existingUrls);
            return null; // Transaction must return null on success
        }).addOnSuccessListener(aVoid -> {
            if (!isAdded()) return;
            Log.d(TAG, "Firestore updated successfully for slot " + slotIndex);
            showSuccessMessage("Imagen actualizada.");
            loadInitialImages(); // Reload all images to refresh UI state consistently
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            showSlotLoading(slotIndex, false); // Hide loading for this slot on failure
            Log.e(TAG, "Firestore transaction failed for slot " + slotIndex, e);
            showErrorState("Error al guardar URL: " + e.getMessage());
        });
    }


    // Confirmation dialog before deleting
    private void handleDeleteImageConfirmation(int slotIndex) {
        if (getContext() == null || !isAdded() || slotIndex < 0 || slotIndex >= currentImageUrls.size()) {
            Log.e(TAG, "Cannot delete image: invalid state or index.");
            return;
        }
        String urlToDelete = currentImageUrls.get(slotIndex);
        if(TextUtils.isEmpty(urlToDelete)){
            Log.w(TAG,"Attempting to delete an empty/null URL at index: "+slotIndex);
            // Maybe just refresh the UI here?
            updateUISlots();
            return;
        }


        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar esta imagen?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    performImageDeletion(slotIndex, urlToDelete);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Performs the actual deletion
    private void performImageDeletion(int slotIndex, String urlToDelete) {
        if (productId == null || productId.isEmpty() || firestore == null || !isAdded()) return;

        showSlotLoading(slotIndex, true);

        DocumentReference productRef = firestore.collection("products").document(productId);

        // Use a transaction to ensure atomicity between reading and writing the list
        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(productRef);
            List<String> existingUrls = new ArrayList<>();
            Object urlsObj = snapshot.get("imageUrls");
            if (urlsObj instanceof List) {
                @SuppressWarnings("unchecked") List<Object> rawList = (List<Object>) urlsObj;
                for(Object item : rawList){
                    if(item instanceof String && !((String)item).isEmpty()){
                        existingUrls.add((String)item);
                    }
                }
            }

            if (slotIndex < existingUrls.size() && urlToDelete.equals(existingUrls.get(slotIndex))) {
                // Remove the URL from the list
                existingUrls.remove(slotIndex);
                Log.d(TAG,"Removing URL at index " + slotIndex);
                transaction.update(productRef, "imageUrls", existingUrls);
            } else {
                Log.w(TAG, "URL to delete not found at expected index " + slotIndex + " or list modified.");
                // Don't throw error, just log, maybe the list changed? Proceed to storage deletion anyway?
                // Or throw: throw new FirebaseFirestoreException("URL mismatch", FirebaseFirestoreException.Code.ABORTED);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (!isAdded()) return;
            Log.d(TAG, "Firestore updated after removing URL for slot " + slotIndex);
            // Now delete from Storage
            deleteImageFromStorage(urlToDelete, slotIndex); // Pass slotIndex for UI update
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            showSlotLoading(slotIndex, false);
            Log.e(TAG, "Firestore transaction failed for deleting URL at slot " + slotIndex, e);
            showErrorState("Error al eliminar URL: " + e.getMessage());
        });
    }

    // Deletes a single image file from Storage
    private void deleteImageFromStorage(String urlToDelete) {
        deleteImageFromStorage(urlToDelete, -1); // Call overload with invalid slot index
    }

    private void deleteImageFromStorage(String urlToDelete, int slotIndex) {
        if (TextUtils.isEmpty(urlToDelete) || storage == null || !isAdded()) {
            Log.w(TAG,"Skipping storage deletion for URL: " + urlToDelete + " (invalid state or URL)");
            // If called from delete flow, need to update UI anyway
            if (slotIndex != -1) {
                showSlotLoading(slotIndex, false);
                loadInitialImages(); // Refresh UI after failed storage delete but successful Firestore update
            }
            return;
        }

        try {
            StorageReference imageRef = storage.getReferenceFromUrl(urlToDelete);
            Log.d(TAG, "Attempting to delete from storage: " + imageRef.getPath());
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        if (!isAdded()) return;
                        Log.d(TAG, "Successfully deleted image from storage: " + urlToDelete);
                        // If called from delete flow, update UI for that slot
                        if(slotIndex != -1) {
                            showSlotLoading(slotIndex, false);
                            showSuccessMessage("Imagen eliminada.");
                            loadInitialImages(); // Refresh UI fully
                        }
                        // If called during replacement, no specific UI update needed here
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        Log.e(TAG, "Failed to delete image from storage: " + urlToDelete, e);
                        // Check if the error is because the file doesn't exist (which is okay after deleting reference)
                        if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            Log.w(TAG, "Image already deleted or not found in storage (expected after reference removal).");
                            if(slotIndex != -1) { // If part of delete flow
                                showSlotLoading(slotIndex, false);
                                showSuccessMessage("Imagen eliminada (no encontrada en almacenamiento).");
                                loadInitialImages();
                            }
                        } else {
                            // Actual error deleting
                            showErrorState("Error al borrar imagen de almacenamiento.");
                            if(slotIndex != -1) {
                                showSlotLoading(slotIndex, false);
                                loadInitialImages(); // Still refresh UI
                            }
                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid URL format for storage deletion: " + urlToDelete, e);
            if(slotIndex != -1) { // If part of delete flow
                showSlotLoading(slotIndex, false);
                showErrorState("URL de imagen inválida para borrar.");
                loadInitialImages(); // Refresh UI
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Helper Methods ---
    private void navigateBack() {
        if (getView() != null && isAdded()) {
            Navigation.findNavController(requireView()).navigateUp();
        } else if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // Shows/hides loading indicator for a specific slot, or all if slotIndex is -1
    private void showSlotLoading(int slotIndex, boolean isLoading) {
        if (!isAdded() || binding == null) return;

        if (slotIndex >= 0 && slotIndex < 5) {
            // Specific slot
            progressBars[slotIndex].setVisibility(isLoading ? View.VISIBLE : View.GONE);
            changeButtons[slotIndex].setEnabled(!isLoading);
            deleteButtons[slotIndex].setEnabled(!isLoading);
            if (isLoading) {
                // Optionally hide image/text while loading slot
                imageSlots[slotIndex].setAlpha(isLoading ? 0.5f : 1.0f);
                emptyTexts[slotIndex].setVisibility(View.GONE);
            } else {
                imageSlots[slotIndex].setAlpha(1.0f);
                // Don't show empty text here, let updateUISlots handle it
            }

        } else if (slotIndex == -1) {
            // General loading state (maybe not needed for this screen)
            // You could show a main progress bar or disable all buttons
            for(int i = 0; i < 5; i++) {
                progressBars[i].setVisibility(isLoading ? View.VISIBLE : View.GONE);
                changeButtons[i].setEnabled(!isLoading);
                deleteButtons[i].setEnabled(!isLoading);
                imageSlots[i].setAlpha(isLoading ? 0.5f : 1.0f);
            }
        }
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