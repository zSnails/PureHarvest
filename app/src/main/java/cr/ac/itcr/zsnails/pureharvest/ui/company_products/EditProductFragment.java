package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.Objects;
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProductBinding;

public class EditProductFragment extends Fragment {

    private FragmentEditProductBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String productId;
    private static final String TAG = "EditProductFragment";
    private static final String ARG_PRODUCT_ID = "productId";
    private String[] productTypesArray;
    private String coffeeTypeString;

    private static final String[] CANONICAL_PRODUCT_TYPE_KEYS_ENGLISH = {"Coffee", "Honey", "Vegetable", "Specialty", "Gourmet", "Base/Normal", "Organic"};
    private static final String[] CANONICAL_PRODUCT_TYPE_KEYS_SPANISH = {"Café", "Miel", "Hortaliza", "Especialidad", "Gourmet", "Base/Normal", "Orgánico"};


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
        productTypesArray = getResources().getStringArray(R.array.product_types_array);
        if (productTypesArray.length > 0) {
            coffeeTypeString = productTypesArray[0];
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, productTypesArray);
        binding.spinnerProductType.setAdapter(adapter);

        binding.spinnerProductType.setOnItemClickListener((parent, v, position, id) -> {
            String selectedType = (String) parent.getItemAtPosition(position);
            updateFieldVisibility(selectedType);
        });


        if (productId != null && !productId.isEmpty()) {
            loadProductData();
        } else {
            Log.e(TAG, "Cannot load data: Product ID is null or empty.");
            showErrorState(getString(R.string.error_invalid_product_id));
            setButtonsEnabled(false);
            updateFieldVisibility(productTypesArray.length > 0 ? productTypesArray[0] : "");
        }

        binding.buttonChangeImage.setOnClickListener(v -> handleManageImages());
        binding.buttonSave.setOnClickListener(v -> handleSaveChanges());
        binding.buttonCancel.setOnClickListener(v -> handleCancel());
        binding.buttonDeleteProduct.setOnClickListener(v -> handleDeleteProductConfirmation());
        binding.buttonManageCoupons.setOnClickListener(v -> handleManageCoupons());
    }

    private void handleManageCoupons() {
        if (getContext() != null && isAdded() && getView() != null) {
            if (productId != null && !productId.isEmpty()) {
                Log.d(TAG, "Navigating to Manage Coupons for product ID: " + productId);
                Bundle args = new Bundle();
                args.putString("productId", productId);
                try {
                    Navigation.findNavController(requireView()).navigate(
                            R.id.action_editProductFragment_to_manageCouponsFragment, args);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Navigation action not found. Ensure it's defined in your nav graph.", e);
                    showErrorState(getString(R.string.error_navigation_generic));
                }
            } else {
                showErrorState("Product ID inválido para administrar cupones.");
            }
        } else {
            Log.w(TAG, "No se puede navegar: estado del fragmento no válido.");
        }
    }


    private void updateFieldVisibility(String selectedLocalizedType) {
        if (binding == null || coffeeTypeString == null) return;

        boolean isCoffeeProduct;
        if (selectedLocalizedType == null) {
            isCoffeeProduct = false;
        } else {
            isCoffeeProduct = selectedLocalizedType.equalsIgnoreCase(coffeeTypeString);
        }

        binding.layoutProductCertifications.setVisibility(isCoffeeProduct ? View.VISIBLE : View.GONE);
        binding.layoutProductFlavorsAromas.setVisibility(isCoffeeProduct ? View.VISIBLE : View.GONE);
        binding.layoutProductAcidity.setVisibility(isCoffeeProduct ? View.VISIBLE : View.GONE);
        binding.layoutProductBody.setVisibility(isCoffeeProduct ? View.VISIBLE : View.GONE);
        binding.layoutProductAftertaste.setVisibility(isCoffeeProduct ? View.VISIBLE : View.GONE);
        binding.layoutProductIngredients.setVisibility(isCoffeeProduct ? View.VISIBLE : View.GONE);
        binding.layoutProductPreparation.setVisibility(isCoffeeProduct ? View.VISIBLE : View.GONE);

        if (isCoffeeProduct) {
            ((androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) binding.buttonDeleteProduct.getLayoutParams())
                    .topToBottom = binding.layoutProductPreparation.getId();
        } else {
            ((androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) binding.buttonDeleteProduct.getLayoutParams())
                    .topToBottom = binding.layoutProductDescription.getId();
        }
        binding.buttonDeleteProduct.requestLayout();
    }


    private void handleDeleteProductConfirmation() {
        if (getContext() == null || !isAdded() || productId == null || productId.isEmpty()) {
            Log.e(TAG, "Cannot delete: context, fragment state, or productId invalid.");
            showErrorState(getString(R.string.error_cannot_delete_product_now));
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_title_confirm_deletion))
                .setMessage(getString(R.string.dialog_message_confirm_deletion))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(getString(R.string.dialog_button_delete), (dialog, whichButton) -> performProductDeletion())
                .setNegativeButton(getString(R.string.dialog_button_cancel), (dialog, whichButton) -> Log.d(TAG, getString(R.string.log_product_deletion_cancelled)))
                .show();
    }
    private void performProductDeletion() {
        if (productId == null || productId.isEmpty() || firestore == null) {
            showErrorState(getString(R.string.error_internal_while_deleting));
            return;
        }
        showLoading(true);
        Log.d(TAG, "Attempting to delete product with ID: " + productId);
        firestore.collection("products").document(productId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore document deleted successfully for ID: " + productId);
                    deleteProductImagesFromStorage();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error deleting Firestore document for ID: " + productId, e);
                    showErrorState(String.format(getString(R.string.error_updating_product_generic), e.getMessage()));
                });
    }
    private void deleteProductImagesFromStorage() {
        if (productId == null || productId.isEmpty() || storage == null || !isAdded()) {
            Log.w(TAG, "Skipping storage deletion due to invalid state.");
            showLoading(false);
            showSuccessMessage(getString(R.string.success_product_data_deleted));
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
                        showSuccessMessage(getString(R.string.success_product_deleted));
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
                                showSuccessMessage(getString(R.string.success_product_and_images_deleted));
                                navigateBack();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                showLoading(false);
                                Log.e(TAG, "Error deleting some images from storage for product ID: " + productId, e);
                                showErrorState(getString(R.string.error_product_deleted_images_error));
                                navigateBack();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    showLoading(false);
                    Log.e(TAG, "Error listing images in storage for product ID: " + productId, e);
                    showSuccessMessage(getString(R.string.success_product_deleted_no_images_or_list_error));
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
                        showErrorState(getString(R.string.error_product_does_not_exist));
                        setButtonsEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    showLoading(false);
                    showErrorState(String.format(getString(R.string.error_loading_data_generic), e.getMessage()));
                    if (binding != null && binding.imageProduct != null) {
                        binding.imageProduct.setImageResource(R.drawable.ic_error_image);
                    }
                    setButtonsEnabled(false);
                });
    }
    private void populateFields(DocumentSnapshot doc) {
        if (binding == null) return;
        binding.editProductName.setText(doc.getString("name"));

        String typeFromFirestore = doc.getString("type");
        String localizedTypeToDisplay = null;
        int typeIndex = -1;

        if (typeFromFirestore != null && !typeFromFirestore.trim().isEmpty()) {
            String cleanedTypeFromFirestore = typeFromFirestore.trim();

            for (int i = 0; i < CANONICAL_PRODUCT_TYPE_KEYS_ENGLISH.length; i++) {
                if (CANONICAL_PRODUCT_TYPE_KEYS_ENGLISH[i].equalsIgnoreCase(cleanedTypeFromFirestore)) {
                    typeIndex = i;
                    break;
                }
            }

            if (typeIndex == -1 && CANONICAL_PRODUCT_TYPE_KEYS_SPANISH.length == CANONICAL_PRODUCT_TYPE_KEYS_ENGLISH.length) {
                for (int i = 0; i < CANONICAL_PRODUCT_TYPE_KEYS_SPANISH.length; i++) {
                    if (CANONICAL_PRODUCT_TYPE_KEYS_SPANISH[i].equalsIgnoreCase(cleanedTypeFromFirestore)) {
                        typeIndex = i;
                        break;
                    }
                }
            }
        }

        if (typeIndex != -1 && typeIndex < productTypesArray.length) {
            localizedTypeToDisplay = productTypesArray[typeIndex];
        } else {
            if (productTypesArray.length > 0) {
                localizedTypeToDisplay = productTypesArray[0];
                Log.w(TAG, "Product type from Firestore '" + typeFromFirestore +
                        "' not matched or invalid. Defaulting to: " + localizedTypeToDisplay);
            } else {
                Log.e(TAG, "Product type from Firestore not matched, and productTypesArray is empty.");
            }
        }

        if (localizedTypeToDisplay != null) {
            binding.spinnerProductType.setText(localizedTypeToDisplay, false);
            updateFieldVisibility(localizedTypeToDisplay);
        } else {
            Log.e(TAG, "Could not determine localized product type to display for spinner.");
            updateFieldVisibility(null);
        }


        Long ratingLong = doc.getLong("rating");
        if (ratingLong != null) {
            binding.editProductRating.setText(String.valueOf(ratingLong.intValue()));
        } else {
            binding.editProductRating.setText("");
        }

        binding.editProductDescription.setText(doc.getString("description"));
        binding.editProductIngredients.setText(doc.getString("ingredients"));
        binding.editProductPreparation.setText(doc.getString("preparation"));
        binding.editProductAcidity.setText(doc.getString("acidity"));
        binding.editProductBody.setText(doc.getString("body"));
        binding.editProductAftertaste.setText(doc.getString("aftertaste"));
        binding.editProductCertifications.setText(doc.getString("certifications"));
        binding.editProductFlavorsAromas.setText(doc.getString("flavorsAndAromas"));

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
            if(binding != null && binding.imageProduct != null) binding.imageProduct.setImageResource(R.drawable.ic_placeholder_image);
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
                Bundle args = new Bundle();
                args.putString(ARG_PRODUCT_ID, productId);
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_editProductFragment_to_manageImagesFragment, args);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Navigation action not found. Ensure it's defined in your nav graph.", e);
                    showErrorState(getString(R.string.error_navigation_generic));
                }

            } else {
                showErrorState(getString(R.string.error_invalid_product_id_for_manage_images));
            }
        } else {
            Log.w(TAG, "Cannot navigate: context, view, or fragment state invalid.");
        }
    }


    private void handleSaveChanges() {
        if (binding == null) {
            showErrorState(getString(R.string.error_internal_binding_null));
            return;
        }
        if (productId == null || productId.isEmpty()) {
            showErrorState(getString(R.string.error_invalid_product_id));
            return;
        }

        String name = Objects.requireNonNull(binding.editProductName.getText()).toString().trim();
        String selectedLocalizedType = binding.spinnerProductType.getText().toString();
        String ratingStr = Objects.requireNonNull(binding.editProductRating.getText()).toString().trim();
        String priceStr = Objects.requireNonNull(binding.editProductPrice.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.editProductDescription.getText()).toString().trim();

        boolean valid = true;
        if (name.isEmpty()) { binding.layoutProductName.setError(getString(R.string.error_name_required)); valid = false; } else { binding.layoutProductName.setError(null); }
        if (priceStr.isEmpty()) { binding.layoutProductPrice.setError(getString(R.string.error_price_required)); valid = false; } else { binding.layoutProductPrice.setError(null); }
        if (ratingStr.isEmpty()) { binding.layoutProductRating.setError(getString(R.string.error_rating_required)); valid = false; } else { binding.layoutProductRating.setError(null); }

        if (description.isEmpty()) { binding.layoutProductDescription.setError(getString(R.string.error_description_required)); valid = false; } else { binding.layoutProductDescription.setError(null); }

        double price = 0;
        if (valid && !priceStr.isEmpty()) {
            try { price = Double.parseDouble(priceStr.replace(',', '.')); if (price < 0) throw new NumberFormatException("Price cannot be negative"); binding.layoutProductPrice.setError(null); }
            catch (NumberFormatException e) { binding.layoutProductPrice.setError(getString(R.string.error_invalid_price)); valid = false; }
        }

        int rating = 0;
        if (valid && !ratingStr.isEmpty()) {
            try {
                rating = Integer.parseInt(ratingStr);
                if (rating < 1 || rating > 5) {
                    binding.layoutProductRating.setError(getString(R.string.error_invalid_rating_range));
                    valid = false;
                } else {
                    binding.layoutProductRating.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.layoutProductRating.setError(getString(R.string.error_invalid_rating_format));
                valid = false;
            }
        }

        String canonicalTypeToSave = null;
        int selectedIndex = -1;
        for(int i=0; i < productTypesArray.length; i++){
            if(productTypesArray[i].equalsIgnoreCase(selectedLocalizedType)){
                selectedIndex = i;
                break;
            }
        }
        if(selectedIndex != -1 && selectedIndex < CANONICAL_PRODUCT_TYPE_KEYS_ENGLISH.length){
            canonicalTypeToSave = CANONICAL_PRODUCT_TYPE_KEYS_ENGLISH[selectedIndex];
        }
        if(canonicalTypeToSave == null){
            Log.w(TAG, "Could not find English canonical key for localized type: " + selectedLocalizedType + ". Saving localized string as fallback.");
            canonicalTypeToSave = selectedLocalizedType;
        }


        Map<String, Object> productUpdates = new HashMap<>();
        productUpdates.put("name", name);
        productUpdates.put("type", canonicalTypeToSave);
        productUpdates.put("rating", rating);
        productUpdates.put("price", price);
        productUpdates.put("description", description);

        if (coffeeTypeString != null && selectedLocalizedType.equalsIgnoreCase(coffeeTypeString)) {
            String acidity = Objects.requireNonNull(binding.editProductAcidity.getText()).toString().trim();
            String body = Objects.requireNonNull(binding.editProductBody.getText()).toString().trim();
            String aftertaste = Objects.requireNonNull(binding.editProductAftertaste.getText()).toString().trim();
            String ingredients = Objects.requireNonNull(binding.editProductIngredients.getText()).toString().trim();
            String preparation = Objects.requireNonNull(binding.editProductPreparation.getText()).toString().trim();

            if (acidity.isEmpty()) { binding.layoutProductAcidity.setError(getString(R.string.error_acidity_required)); valid = false; } else { binding.layoutProductAcidity.setError(null); }
            if (body.isEmpty()) { binding.layoutProductBody.setError(getString(R.string.error_body_required)); valid = false; } else { binding.layoutProductBody.setError(null); }
            if (aftertaste.isEmpty()) { binding.layoutProductAftertaste.setError(getString(R.string.error_aftertaste_required)); valid = false; } else { binding.layoutProductAftertaste.setError(null); }
            if (ingredients.isEmpty()) { binding.layoutProductIngredients.setError(getString(R.string.error_ingredients_required)); valid = false; } else { binding.layoutProductIngredients.setError(null); }
            if (preparation.isEmpty()) { binding.layoutProductPreparation.setError(getString(R.string.error_preparation_required)); valid = false; } else { binding.layoutProductPreparation.setError(null); }

            productUpdates.put("certifications", Objects.requireNonNull(binding.editProductCertifications.getText()).toString().trim());
            productUpdates.put("flavorsAndAromas", Objects.requireNonNull(binding.editProductFlavorsAromas.getText()).toString().trim());
            productUpdates.put("acidity", acidity);
            productUpdates.put("body", body);
            productUpdates.put("aftertaste", aftertaste);
            productUpdates.put("ingredients", ingredients);
            productUpdates.put("preparation", preparation);
        }

        if (!valid) {
            showLoading(false); // Make sure loading is hidden if validation fails early
            return;
        }
        showLoading(true); // Show loading only if all client-side validation passes
        updateProductFirestoreOnlyText(productUpdates);
    }

    private void updateProductFirestoreOnlyText(Map<String, Object> productUpdates) {
        if (productId == null || productId.isEmpty() || firestore == null) {
            showLoading(false);
            showErrorState(getString(R.string.error_internal_while_saving));
            return;
        }
        firestore.collection("products").document(productId)
                .update(productUpdates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    showSuccessMessage(getString(R.string.success_product_updated));
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error updating product text fields in Firestore for ID: " + productId, e);
                    showErrorState(String.format(getString(R.string.error_updating_product_generic), e.getMessage()));
                });
    }

    private void handleCancel() {
        navigateBack();
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

    private void setButtonsEnabled(boolean enabled) {
        if (binding == null) return;
        binding.buttonSave.setEnabled(enabled);
        binding.buttonCancel.setEnabled(enabled);
        binding.buttonChangeImage.setEnabled(enabled);
        binding.buttonDeleteProduct.setEnabled(enabled);

        binding.editProductName.setEnabled(enabled);
        binding.spinnerProductType.setEnabled(enabled);
        binding.editProductRating.setEnabled(enabled);
        binding.editProductPrice.setEnabled(enabled);
        binding.editProductDescription.setEnabled(enabled);

        binding.editProductCertifications.setEnabled(enabled);
        binding.editProductFlavorsAromas.setEnabled(enabled);
        binding.editProductAcidity.setEnabled(enabled);
        binding.editProductBody.setEnabled(enabled);
        binding.editProductAftertaste.setEnabled(enabled);
        binding.editProductIngredients.setEnabled(enabled);
        binding.editProductPreparation.setEnabled(enabled);
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