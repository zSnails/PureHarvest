package cr.ac.itcr.zsnails.pureharvest.domain.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;

public class ProductsRepository {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Product>> favoriteProducts = new MutableLiveData<>(new ArrayList<>());

    public ProductsRepository(@NonNull FirebaseFirestore db) {
        this.db = db;
    }

    public MutableLiveData<List<Product>> getFavoriteProducts(String ownerId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Product> prods = new ArrayList<>();
            db.collection("favorites").whereEqualTo("ownerId", ownerId).get()
                    .addOnSuccessListener(snapshot -> {
                        List<DocumentSnapshot> a = snapshot.getDocuments();
                        List<String> ids = new ArrayList<>();
                        a.forEach(doc -> {
                            FavoriteItem fav = doc.toObject(FavoriteItem.class);
                            ids.add(fav.productId);
                        });

                        if (ids.isEmpty()) {
                            return;
                        }
                        db.collection("products").whereIn("id", ids).get()
                                .addOnSuccessListener(document -> {
                                    document.getDocuments().forEach(doc -> {
                                        Product prod = doc.toObject(Product.class);
                                        Log.d("loading:items", String.format("Loaded this %s", prod.getName()));
                                        prods.add(prod);
                                    });
                                    Log.d("loading:items", String.format("Loaded %d items, posting them", prods.size()));
                                    favoriteProducts.postValue(prods);
                                });
                    });
        });
        return favoriteProducts;
    }

    private static class FavoriteItem {
        public String ownerId;
        public String productId;
    }
}
