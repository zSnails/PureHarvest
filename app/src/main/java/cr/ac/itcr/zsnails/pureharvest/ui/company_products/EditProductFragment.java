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

// Imports necesarios para List
import java.util.List;
import java.util.ArrayList; // Opcional, pero común en Firestore

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cr.ac.itcr.zsnails.pureharvest.R; // Asegúrate que R está importado
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProductBinding;

public class EditProductFragment extends Fragment {

    private FragmentEditProductBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private String productId;
    private Uri imageUri;

    private static final String TAG = "EditProductFragment";
    private static final String ARG_PRODUCT_ID = "productId";
    private static final int IMAGE_PICK_CODE = 1000;

    // --- onCreate, onCreateView sin cambios ---
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
            Log.e(TAG, "Cannot load data: Product ID is null or empty.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: ID de producto inválido.", Toast.LENGTH_LONG).show();

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
                    // Verifica si el fragment todavía está adjunto y la vista existe
                    if (!isAdded() || binding == null) {
                        Log.w(TAG, "Fragment not attached or binding is null after Firestore success.");
                        showLoading(false);
                        return;
                    }

                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {

                            binding.editProductName.setText(getStringValue(data, "name"));
                            binding.editProductType.setText(getStringValue(data, "type"));
                            binding.editProductDescription.setText(getStringValue(data, "description"));
                            binding.editProductIngredients.setText(getStringValue(data, "ingredients"));
                            binding.editProductPreparation.setText(getStringValue(data, "preparation"));
                            binding.editProductAcidity.setText(getStringValue(data, "acidity"));
                            binding.editProductBody.setText(getStringValue(data, "body"));
                            binding.editProductAftertaste.setText(getStringValue(data, "aftertaste"));

                            Object priceObj = data.get("price");
                            if (priceObj instanceof Number) {
                                binding.editProductPrice.setText(String.format("%.2f", ((Number) priceObj).doubleValue()));
                            } else {
                                binding.editProductPrice.setText(getStringValue(data, "price")); // O manejar como string
                            }

                            Object imageUrlsObj = data.get("imageUrls");
                            String imageUrlToLoad = null;

                            if (imageUrlsObj instanceof String) {
                                // Caso 1: Es un String simple
                                imageUrlToLoad = (String) imageUrlsObj;
                                Log.d(TAG, "Image URL found (String): " + imageUrlToLoad);
                            } else if (imageUrlsObj instanceof List) {
                                // Caso 2: Es una Lista
                                @SuppressWarnings("unchecked")
                                List<Object> urlList = (List<Object>) imageUrlsObj;
                                if (!urlList.isEmpty()) {
                                    Object firstElement = urlList.get(0);
                                    if (firstElement instanceof String) {
                                        imageUrlToLoad = (String) firstElement;
                                        Log.d(TAG, "Image URL found (List, first element): " + imageUrlToLoad);
                                    } else {
                                        Log.w(TAG, "First element in imageUrls List is not a String: " + firstElement);
                                    }
                                } else {
                                    Log.w(TAG, "imageUrls field is an empty List.");
                                }
                            } else if (imageUrlsObj != null) {
                                // Caso 3: Es otro tipo, o nulo (ya cubierto abajo)
                                Log.w(TAG, "imageUrls field is not a String or List, type: " + imageUrlsObj.getClass().getName());
                            } else {
                                Log.w(TAG, "imageUrls field is null or missing.");
                            }


                            if (imageUrlToLoad != null && !imageUrlToLoad.trim().isEmpty()) {
                                Glide.with(requireContext()) // requireContext() es seguro aquí porque verificamos isAdded()
                                        .load(imageUrlToLoad.trim()) // trim() por si acaso hay espacios
                                        .placeholder(R.drawable.ic_placeholder_image) // ¡Añade un placeholder!
                                        .error(R.drawable.ic_error_image)       // ¡Añade una imagen de error!
                                        .into(binding.imageProduct);
                            } else {
                                Log.w(TAG, "No valid image URL to load. Setting placeholder.");
                                binding.imageProduct.setImageResource(R.drawable.ic_placeholder_image);
                            }

                        } else {
                            Log.e(TAG, "Product data is null for ID: " + productId);
                            Toast.makeText(getContext(), "No se encontraron datos para este producto.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Product document does not exist for ID: " + productId);
                        if (getContext() != null) { // getContext() puede ser null si el fragment se desvincula
                            Toast.makeText(getContext(), "El producto no existe.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        Log.w(TAG, "Fragment not attached during Firestore failure.");
                        return;
                    }
                    showLoading(false);
                    Log.e(TAG, "Error fetching product data for ID: " + productId, e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    if(binding != null) {
                        binding.imageProduct.setImageResource(R.drawable.ic_error_image);
                    }
                });
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value == null) return "";
        return String.valueOf(value);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null && binding != null && getContext() != null ) {
            imageUri = data.getData();
            Glide.with(requireContext())
                    .load(imageUri)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(binding.imageProduct);
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
        if (binding == null) return;

        if (productId == null || productId.isEmpty()) {
            if(getContext() != null) Toast.makeText(getContext(), "Error: ID de producto inválido.", Toast.LENGTH_SHORT).show();
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
            if(getContext() != null) Toast.makeText(getContext(), "Nombre y Precio son requeridos.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr.replace(',', '.'));
        } catch (NumberFormatException e) {
            if(getContext() != null) Toast.makeText(getContext(), "Precio inválido.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        Map<String, Object> productUpdates = new HashMap<>();
        productUpdates.put("name", name);
        productUpdates.put("type", type);
        productUpdates.put("description", description);
        productUpdates.put("ingredients", ingredients);
        productUpdates.put("preparation", preparation);
        productUpdates.put("price", price);
        productUpdates.put("acidity", acidity);
        productUpdates.put("body", body);
        productUpdates.put("aftertaste", aftertaste);

        if (imageUri != null) {
            uploadImageAndUpdateProduct(productUpdates);
        } else {

            updateProductFirestore(productUpdates);
        }
    }

    private void uploadImageAndUpdateProduct(Map<String, Object> productUpdates) {
        if (imageUri == null) {
            Log.w(TAG, "uploadImageAndUpdateProduct called with null imageUri");
            updateProductFirestore(productUpdates); // Actualiza el resto de datos
            return;
        }

        showLoading(true); // Asegúrate que el loading está activo
        StorageReference storageRef = storage.getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("product_images/" + fileName); // Simplificado, podrías usar productId si es único y estable

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Log.d(TAG, "Image uploaded successfully. URL: " + uri.toString());

                            List<String> imageUrlsList = new ArrayList<>();
                            imageUrlsList.add(uri.toString());
                            productUpdates.put("imageUrls", imageUrlsList);

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
        if (productId == null || productId.isEmpty()) {
            Log.e(TAG, "Cannot update Firestore, product ID is invalid.");
            showLoading(false);
            if(getContext() != null) Toast.makeText(getContext(), "Error interno: ID de producto no válido.", Toast.LENGTH_SHORT).show();
            return;
        }


        firestore.collection("products").document(productId)
                .update(productUpdates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d(TAG, "Product updated successfully in Firestore.");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Producto actualizado con éxito.", Toast.LENGTH_SHORT).show();
                    }
                    imageUri = null;
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error updating product in Firestore", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al actualizar producto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleCancel() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Operación Cancelada", Toast.LENGTH_SHORT).show();
        }
        imageUri = null;
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void showLoading(boolean isLoading) {
        if (binding != null) {


            binding.buttonSave.setEnabled(!isLoading);
            binding.buttonCancel.setEnabled(!isLoading);
            binding.buttonChangeImage.setEnabled(!isLoading);
            binding.editProductName.setEnabled(!isLoading);
            binding.editProductType.setEnabled(!isLoading);
        } else {
            Log.w(TAG, "showLoading called but binding is null. State: " + isLoading);
        }
    }
}