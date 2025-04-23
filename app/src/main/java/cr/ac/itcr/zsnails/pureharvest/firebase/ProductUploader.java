package cr.ac.itcr.zsnails.pureharvest.firebase;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import cr.ac.itcr.zsnails.pureharvest.model.Product;

public class ProductUploader {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("product_images");

    public interface UploadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void uploadProduct(Context context, Product product, List<Uri> imageUris, UploadCallback callback) {
        if (imageUris.isEmpty()) {
            Toast.makeText(context, "Debe seleccionar al menos una imagen", Toast.LENGTH_SHORT).show();
            callback.onFailure(new Exception("No se seleccionaron imágenes"));
            return;
        }

        List<String> uploadedImageUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (Uri imageUri : imageUris) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child(imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedImageUrls.add(uri.toString());

                            if (uploadCount.incrementAndGet() == imageUris.size()) { // If all have already been uploaded, we save the product
                                product.imageUrls = uploadedImageUrls;
                                saveProduct(product, callback);
                            }
                        });
                    })
                    .addOnFailureListener(callback::onFailure);
        }
    }

    private void saveProduct(Product product, UploadCallback callback) {
        String newId = UUID.randomUUID().toString();
        product.id = newId;

        firestore.collection("products").document(newId)
                .set(product)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
