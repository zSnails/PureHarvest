package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompnayProductsListBinding;

public class CompanyProductsListFragment extends Fragment {

    FragmentCompnayProductsListBinding binding;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final String TAG = "CompanyProductsList";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCompnayProductsListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        fetchProductsFromFirestore();

        return root;
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(productList);
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewProducts.setAdapter(productAdapter);
    }

    private void fetchProductsFromFirestore() {
        Log.d(TAG, "Fetching products from Firestore for sellerId: 1");
        showLoading(true);

        firestore.collection("products")
                .whereEqualTo("sellerId", "1")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Successfully fetched " + queryDocumentSnapshots.size() + " documents.");
                    productList.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                String id = doc.getId();
                                String name = doc.getString("name");

                                Number priceNumber = null;
                                Object priceObj = doc.get("price");
                                if (priceObj instanceof Number) {
                                    priceNumber = (Number) priceObj;
                                }

                                Object imageUrlsObj = doc.get("imageUrls");
                                String imageUrl = null;

                                if (imageUrlsObj instanceof String) {
                                    imageUrl = (String) imageUrlsObj;
                                    Log.v(TAG, "Product ID: " + id + " - imageUrls is String: " + imageUrl);
                                } else if (imageUrlsObj instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    List<Object> urlList = (List<Object>) imageUrlsObj;
                                    if (!urlList.isEmpty() && urlList.get(0) instanceof String) {
                                        imageUrl = (String) urlList.get(0);
                                        Log.v(TAG, "Product ID: " + id + " - imageUrls is List, using first URL: " + imageUrl);
                                    } else {
                                        Log.w(TAG, "Product ID: " + id + " - imageUrls List is empty or first element is not a String.");
                                    }
                                } else if (imageUrlsObj != null) {
                                    Log.w(TAG, "Product ID: " + id + " - imageUrls has unexpected type: " + imageUrlsObj.getClass().getName());
                                } else {
                                    Log.w(TAG, "Product ID: " + id + " - imageUrls field is null or missing.");
                                }

                                if (id != null && name != null && !name.isEmpty() && priceNumber != null) {
                                    Product product = new Product(id, name, priceNumber.intValue(), imageUrl);
                                    productList.add(product);
                                } else {
                                    Log.w(TAG, "Skipping product due to missing/invalid data. ID: " + (id != null ? id : "N/A") + ", Name: " + name + ", PriceObj: " + priceObj);
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document " + doc.getId(), e);
                            }
                        }
                    }

                    if (productAdapter != null) {
                        productAdapter.notifyDataSetChanged();
                        Log.d(TAG, "RecyclerView adapter notified. Total products in list: " + productList.size());
                    } else {
                        Log.e(TAG, "Adapter was null when trying to notify.");
                    }

                    showLoading(false);
                    updateEmptyViewVisibility();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching products from Firestore", e);
                    showLoading(false);
                    updateEmptyViewVisibility();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        if (binding != null) {
        }
    }

    private void updateEmptyViewVisibility() {
        if (binding != null && binding.recyclerViewProducts != null) {
            if (productList.isEmpty()) {
                binding.recyclerViewProducts.setVisibility(View.GONE);
            } else {
                binding.recyclerViewProducts.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
