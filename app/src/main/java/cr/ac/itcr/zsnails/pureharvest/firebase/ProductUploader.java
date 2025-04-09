package cr.ac.itcr.zsnails.pureharvest.firebase;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import cr.ac.itcr.zsnails.pureharvest.model.Product;

public class ProductUploader {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface UploadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void uploadProduct(Context context, Product product, List<android.net.Uri> imageUris, UploadCallback callback) {
        // TODO: Replace simulation with real image upload to Firebase Storage (ask negrini)
        Toast.makeText(context, "Simulation mode: images will not be uploaded", Toast.LENGTH_SHORT).show();

        // Simulate empty image URLs
        product.imageUrls = Collections.emptyList();

        saveProduct(product, callback);
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
