package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompnayProductsListBinding;

public class CompanyProductsListFragment extends Fragment {

    private FragmentCompnayProductsListBinding binding;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static final String TAG = "CompanyProductsList";
    private static final String SELLER_ID_TO_FILTER = "1";

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
        int numberOfColumns = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        binding.recyclerViewProducts.setLayoutManager(layoutManager);
        binding.recyclerViewProducts.setAdapter(productAdapter);
        binding.recyclerViewProducts.setHasFixedSize(true);
    }

    private void fetchProductsFromFirestore() {
        Log.d(TAG, "Fetching products from Firestore for sellerId: " + SELLER_ID_TO_FILTER);
        showLoading(true);

        firestore.collection("products")
                .whereEqualTo("sellerId", SELLER_ID_TO_FILTER)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Successfully fetched " + queryDocumentSnapshots.size() + " documents.");
                    productList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No products found for seller ID: " + SELLER_ID_TO_FILTER);
                    } else {
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

                                if (imageUrlsObj instanceof String && !((String) imageUrlsObj).isEmpty()) {
                                    imageUrl = (String) imageUrlsObj;
                                } else if (imageUrlsObj instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    List<Object> urlList = (List<Object>) imageUrlsObj;
                                    for (Object urlObj : urlList) {
                                        if (urlObj instanceof String && !((String) urlObj).isEmpty()) {
                                            imageUrl = (String) urlObj;
                                            break;
                                        }
                                    }
                                }

                                if (imageUrl == null) {
                                    Log.w(TAG, "Product ID: " + id + " - No valid image URL found in 'imageUrls' field.");
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

    private void showLoading(boolean isLoading) {
        if (binding == null) return;

        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            binding.recyclerViewProducts.setVisibility(View.GONE);
            binding.textViewEmptyList.setVisibility(View.GONE);
        }
    }

    private void updateEmptyViewVisibility() {
        if (binding == null) return;

        boolean isEmpty = productList.isEmpty();
        binding.textViewEmptyList.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            Log.d(TAG, "Product list is empty. Showing empty text view.");
        } else {
            Log.d(TAG, "Product list has items. Showing RecyclerView.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
