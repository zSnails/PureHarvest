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
                // Consider showing a persistent error or navigating back if critical
            }
        } else {
            Log.e(TAG, "Product ID not provided in arguments.");
            // Consider showing a persistent error or navigating back
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
            showErrorState(getString(R.string.error_invalid_product_id_manage_images));
            // Optionally disable all UI elements or show an error placeholder view
            setAllSlotsEnabled(false);
        }
    }

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
            final int slotIndex = i;
            changeButtons[i].setOnClickListener(v -> handlePickImage(slotIndex));
            deleteButtons[i].setOnClickListener(v -> handleDeleteImageConfirmation(slotIndex));
        }
    }

    private void loadInitialImages() {
        if (productId == null || productId.isEmpty()) return;
        // showSlotLoading(-1, true); // Consider if a global loading state is desired

        firestore.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) return;

                    currentImageUrls.clear();
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
                        showErrorState(getString(R.string.error_product_not_found_manage_images));
                    }
                    updateUISlots();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    Log.e(TAG, "Error fetching product data for images", e);
                    showErrorState(String.format(getString(R.string.error_loading_images_generic), e.getMessage()));
                    updateUISlots(); // Update UI to reflect empty or error state
                });
    }

    private void updateUISlots() {
        if (!isAdded() || binding == null) return;

        for (int i = 0; i < 5; i++) {
            if (i < currentImageUrls.size()) {
                String url = currentImageUrls.get(i);
                if (!TextUtils.isEmpty(url)) {
                    setSlotState(i, url, false);
                } else {
                    setSlotState(i, null, true);
                }
            } else {
                setSlotState(i, null, true);
            }
            progressBars[i].setVisibility(View.GONE);
        }
    }

    private void setSlotState(int slotIndex, @Nullable String imageUrl, boolean isEmpty) {
        if (!isAdded() || slotIndex < 0 || slotIndex >= 5) return;

        if (isEmpty || TextUtils.isEmpty(imageUrl)) {
            imageSlots[slotIndex].setImageResource(0);
            if (getContext() != null) { // Check context for getResources
                imageSlots[slotIndex].setBackgroundColor(getResources().getColor(R.color.placeholder_grey));
            }
            emptyTexts[slotIndex].setVisibility(View.VISIBLE);
            changeButtons[slotIndex].setText(getString(R.string.button_add_image));
            changeButtons[slotIndex].setVisibility(View.VISIBLE);
            deleteButtons[slotIndex].setVisibility(View.GONE);
        } else {
            emptyTexts[slotIndex].setVisibility(View.GONE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(imageSlots[slotIndex]);
            changeButtons[slotIndex].setText(getString(R.string.button_change_image_action));
            changeButtons[slotIndex].setVisibility(View.VISIBLE);
            deleteButtons[slotIndex].setVisibility(View.VISIBLE);
        }
        progressBars[slotIndex].setVisibility(View.GONE);
        changeButtons[slotIndex].setEnabled(true);
        deleteButtons[slotIndex].setEnabled(true);
    }

    private void handlePickImage(int slotIndex) {
        currentPickingSlot = slotIndex;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE_BASE + slotIndex);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ReqCode=" + requestCode + ", ResCode=" + resultCode);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode >= IMAGE_PICK_CODE_BASE && requestCode < IMAGE_PICK_CODE_BASE + 5) {
                int slotIndex = requestCode - IMAGE_PICK_CODE_BASE;
                Uri selectedImageUri = data.getData();
                Log.d(TAG, "Image picked for slot " + slotIndex + ": " + selectedImageUri.toString());
                uploadImageForSlot(slotIndex, selectedImageUri);
            } else {
                Log.w(TAG, String.format(getString(R.string.log_unknown_activity_request_code), requestCode));
            }
        } else {
            Log.d(TAG, getString(R.string.log_image_pick_cancelled));
        }
    }

    private void uploadImageForSlot(int slotIndex, @NonNull Uri imageUri) {
        if (productId == null || productId.isEmpty() || storage == null || !isAdded() || slotIndex < 0 || slotIndex >= 5) {
            showErrorState(getString(R.string.error_starting_upload));
            return;
        }
        showSlotLoading(slotIndex, true);
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
                                updateFirestoreWithNewUrl(slotIndex, downloadUri.toString());
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                showSlotLoading(slotIndex, false);
                                Log.e(TAG, "Error getting download URL for slot " + slotIndex, e);
                                showErrorState(getString(R.string.error_getting_image_url));
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    showSlotLoading(slotIndex, false);
                    Log.e(TAG, "Error uploading image for slot " + slotIndex, e);
                    showErrorState(getString(R.string.error_uploading_image));
                });
    }

    private void updateFirestoreWithNewUrl(int slotIndex, String newUrl) {
        if (productId == null || productId.isEmpty() || firestore == null || !isAdded()) return;
        DocumentReference productRef = firestore.collection("products").document(productId);

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

            if (slotIndex < existingUrls.size()) {
                String oldUrl = existingUrls.get(slotIndex);
                deleteImageFromStorage(oldUrl);
                existingUrls.set(slotIndex, newUrl);
                Log.d(TAG,"Replacing URL at index " + slotIndex);
            } else if (slotIndex < 5) {
                while(existingUrls.size() <= slotIndex && existingUrls.size() < 5){
                    existingUrls.add(null);
                }
                if(existingUrls.size() > slotIndex){
                    existingUrls.set(slotIndex, newUrl);
                } else {
                    existingUrls.add(newUrl);
                }
                Log.d(TAG, "Adding new URL at index " + slotIndex);
            } else {
                Log.e(TAG,"Slot index " + slotIndex + " is out of bounds (max 5).");
                throw new FirebaseFirestoreException("Slot index out of bounds", FirebaseFirestoreException.Code.INVALID_ARGUMENT);
            }
            while (!existingUrls.isEmpty() && existingUrls.get(existingUrls.size() - 1) == null) {
                existingUrls.remove(existingUrls.size() - 1);
            }
            transaction.update(productRef, "imageUrls", existingUrls);
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (!isAdded()) return;
            Log.d(TAG, "Firestore updated successfully for slot " + slotIndex);
            showSuccessMessage(getString(R.string.success_image_updated));
            loadInitialImages();
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            showSlotLoading(slotIndex, false);
            Log.e(TAG, "Firestore transaction failed for slot " + slotIndex, e);
            showErrorState(String.format(getString(R.string.error_saving_image_url), e.getMessage()));
        });
    }

    private void handleDeleteImageConfirmation(int slotIndex) {
        if (getContext() == null || !isAdded() || slotIndex < 0 || slotIndex >= currentImageUrls.size()) {
            Log.e(TAG, "Cannot delete image: invalid state or index.");
            return;
        }
        String urlToDelete = currentImageUrls.get(slotIndex);
        if(TextUtils.isEmpty(urlToDelete)){
            Log.w(TAG,"Attempting to delete an empty/null URL at index: "+slotIndex);
            updateUISlots();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_title_confirm_image_deletion))
                .setMessage(getString(R.string.dialog_message_confirm_image_deletion))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(getString(R.string.dialog_button_delete), (dialog, which) -> {
                    performImageDeletion(slotIndex, urlToDelete);
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), null)
                .show();
    }

    private void performImageDeletion(int slotIndex, String urlToDelete) {
        if (productId == null || productId.isEmpty() || firestore == null || !isAdded()) return;
        showSlotLoading(slotIndex, true);
        DocumentReference productRef = firestore.collection("products").document(productId);

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
                existingUrls.remove(slotIndex);
                Log.d(TAG,"Removing URL at index " + slotIndex);
                transaction.update(productRef, "imageUrls", existingUrls);
            } else {
                Log.w(TAG, "URL to delete not found at expected index " + slotIndex + " or list modified.");
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (!isAdded()) return;
            Log.d(TAG, "Firestore updated after removing URL for slot " + slotIndex);
            deleteImageFromStorage(urlToDelete, slotIndex);
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            showSlotLoading(slotIndex, false);
            Log.e(TAG, "Firestore transaction failed for deleting URL at slot " + slotIndex, e);
            showErrorState(String.format(getString(R.string.error_deleting_image_url), e.getMessage()));
        });
    }

    private void deleteImageFromStorage(String urlToDelete) {
        deleteImageFromStorage(urlToDelete, -1);
    }

    private void deleteImageFromStorage(String urlToDelete, int slotIndex) {
        if (TextUtils.isEmpty(urlToDelete) || storage == null || !isAdded()) {
            Log.w(TAG,"Skipping storage deletion for URL: " + urlToDelete + " (invalid state or URL)");
            if (slotIndex != -1) {
                showSlotLoading(slotIndex, false);
                loadInitialImages();
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
                        if(slotIndex != -1) {
                            showSlotLoading(slotIndex, false);
                            showSuccessMessage(getString(R.string.success_image_deleted));
                            loadInitialImages();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        Log.e(TAG, "Failed to delete image from storage: " + urlToDelete, e);
                        if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            Log.w(TAG, "Image already deleted or not found in storage (expected after reference removal).");
                            if(slotIndex != -1) {
                                showSlotLoading(slotIndex, false);
                                showSuccessMessage(getString(R.string.success_image_deleted_not_found_in_storage));
                                loadInitialImages();
                            }
                        } else {
                            showErrorState(getString(R.string.error_deleting_image_from_storage));
                            if(slotIndex != -1) {
                                showSlotLoading(slotIndex, false);
                                loadInitialImages();
                            }
                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid URL format for storage deletion: " + urlToDelete, e);
            if(slotIndex != -1) {
                showSlotLoading(slotIndex, false);
                showErrorState(getString(R.string.error_invalid_image_url_for_deletion));
                loadInitialImages();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void navigateBack() {
        if (getView() != null && isAdded()) {
            try {
                Navigation.findNavController(requireView()).navigateUp();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error navigating up, controller not found or view not attached.", e);
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        } else if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void showSlotLoading(int slotIndex, boolean isLoading) {
        if (!isAdded() || binding == null) return;
        if (slotIndex >= 0 && slotIndex < 5) {
            progressBars[slotIndex].setVisibility(isLoading ? View.VISIBLE : View.GONE);
            changeButtons[slotIndex].setEnabled(!isLoading);
            deleteButtons[slotIndex].setEnabled(!isLoading);
            if (isLoading) {
                imageSlots[slotIndex].setAlpha(0.5f);
                emptyTexts[slotIndex].setVisibility(View.GONE);
            } else {
                imageSlots[slotIndex].setAlpha(1.0f);
            }
        } else if (slotIndex == -1) { // General loading (e.g., initial load)
            for(int i = 0; i < 5; i++) {
                // Decide if you want to show all progress bars or a central one
                // For now, keeping it simple:
                // progressBars[i].setVisibility(isLoading ? View.VISIBLE : View.GONE);
                changeButtons[i].setEnabled(!isLoading);
                deleteButtons[i].setEnabled(!isLoading);
                // imageSlots[i].setAlpha(isLoading ? 0.5f : 1.0f); // Maybe too much visual noise
            }
        }
    }

    private void setAllSlotsEnabled(boolean enabled) {
        if (binding == null || !isAdded()) return;
        for (int i = 0; i < 5; i++) {
            changeButtons[i].setEnabled(enabled);
            deleteButtons[i].setEnabled(enabled);
            // You might want to disable imageViews too if they are clickable
            // imageSlots[i].setEnabled(enabled);
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