package cr.ac.itcr.zsnails.pureharvest.ui.home;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public HomeViewModel() {
        loadProductsFromFireBase();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    private void loadProductsFromFireBase() {
        db.collection("products")
                .get(Source.SERVER)
                .addOnSuccessListener(result -> {
                    List<Product> productList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : result) {
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                    }
                    products.setValue(productList);
                })
                .addOnFailureListener(e -> {
                    // Log the error for developers
                    Log.e("HomeViewModel", "Failed to load products from database", e);

                    // You could set an empty list to avoid app crashing or display a placeholder
                    products.setValue(new ArrayList<>());

                    // Optional: Show user-friendly message (e.g., using LiveData and Toast in Fragment)
                });
    }
}
