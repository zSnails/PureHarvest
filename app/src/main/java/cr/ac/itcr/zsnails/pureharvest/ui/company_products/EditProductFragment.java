package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProductBinding;

public class EditProductFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FragmentEditProductBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private String productId;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProductBinding.inflate(inflater, container, false);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Obtener el ID del producto desde los argumentos
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        // Cargar los datos actuales del producto
        loadProductData();

        // Configurar el listener para cambiar la imagen
        binding.btnChangeImage.setOnClickListener(v -> openImageChooser());

        // Configurar el botón para confirmar cambios
        binding.btnConfirmChanges.setOnClickListener(v -> saveChanges());

        return binding.getRoot();
    }

    private void loadProductData() {
        // Aquí iría la lógica para cargar los datos actuales de Firestore y mostrarlos
        firestore.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        Long price = documentSnapshot.getLong("price");
                        String imageUrl = documentSnapshot.getString("imageUrl");

                        // Mostrar los valores actuales
                        binding.productName.setText(name);
                        binding.productPrice.setText(String.valueOf(price));
                        // Aquí puedes cargar la imagen con Glide, por ejemplo:
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(getContext()).load(imageUrl).into(binding.productImage);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("EditProductFragment", "Error loading product data", e));
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.productImage.setImageURI(imageUri); // Mostrar la imagen seleccionada en el ImageView
        }
    }

    private void saveChanges() {
        String name = binding.productName.getText().toString();
        String priceString = binding.productPrice.getText().toString();

        if (name.isEmpty() || priceString.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceString);

        // Si hay una nueva imagen, se sube a Firebase Storage
        if (imageUri != null) {
            StorageReference imageRef = storage.getReference()
                    .child("product_images/" + productId + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateProductInFirestore(name, price, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EditProductFragment", "Error uploading image", e);
                        updateProductInFirestore(name, price, null);
                    });
        } else {
            // Si no se cambia la imagen, se actualiza solo el nombre y el precio
            updateProductInFirestore(name, price, null);
        }
    }

    private void updateProductInFirestore(String name, int price, String imageUrl) {
        // Actualizamos los datos del producto en Firestore
        firestore.collection("products")
                .document(productId)
                .update("name", name, "price", price, "imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed(); // Volver al fragmento anterior
                })
                .addOnFailureListener(e -> {
                    Log.e("EditProductFragment", "Error updating product", e);
                    Toast.makeText(getContext(), "Error al actualizar el producto", Toast.LENGTH_SHORT).show();
                });
    }
}
