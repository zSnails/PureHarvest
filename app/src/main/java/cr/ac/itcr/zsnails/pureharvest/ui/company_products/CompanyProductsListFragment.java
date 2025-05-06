package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController; // <-- Import NavController
import androidx.navigation.Navigation; // <-- Import Navigation
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils; // <-- Import TextUtils (used later)
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// No need for ImageButton import if using binding directly
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
// Ensure correct binding class name
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompnayProductsListBinding;


public class CompanyProductsListFragment extends Fragment {

    private FragmentCompnayProductsListBinding binding;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static final String TAG = "CompanyProductsList";
    private static final String SELLER_ID_TO_FILTER = "1"; // Consider making this dynamic

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCompnayProductsListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // --- Setup Back Button Listener ---
        binding.backButtonProductList.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp(); // Navigate back up the stack
        });
        // ---------------------------------

        setupRecyclerView();
        fetchProductsFromFirestore();

        return root;
    }

    private void setupRecyclerView() {
        // Use binding
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
                    // Check if fragment is still attached and binding is valid
                    if (binding == null || !isAdded()) {
                        Log.w(TAG, "Fragment detached or binding null after Firestore success. Aborting UI update.");
                        return;
                    }

                    Log.d(TAG, "Successfully fetched " + queryDocumentSnapshots.size() + " documents.");
                    productList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No products found for seller ID: " + SELLER_ID_TO_FILTER);
                    } else {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                String id = doc.getId();
                                String name = doc.getString("name");
                                Number priceNumber = doc.getDouble("price"); // Use getDouble for price

                                // Robust Image URL handling
                                Object imageUrlsObj = doc.get("imageUrls");
                                String imageUrl = null;
                                if (imageUrlsObj instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    List<Object> urlList = (List<Object>) imageUrlsObj;
                                    // Get first non-empty string URL from the list
                                    for (Object urlObj : urlList) {
                                        if (urlObj instanceof String && !TextUtils.isEmpty((String) urlObj)) {
                                            imageUrl = (String) urlObj;
                                            break;
                                        }
                                    }
                                } else if (imageUrlsObj instanceof String && !TextUtils.isEmpty((String) imageUrlsObj)) {
                                    imageUrl = (String) imageUrlsObj;
                                }

                                if (imageUrl == null) {
                                    Log.w(TAG, "Product ID: " + id + " - No valid image URL found or field missing/empty.");
                                }

                                // Check required fields
                                if (id != null && name != null && !name.isEmpty() && priceNumber != null) {
                                    // Adapt Product constructor if it expects double or handle conversion
                                    Product product = new Product(id, name, priceNumber.intValue(), imageUrl); // Assuming Product takes int
                                    productList.add(product);
                                } else {
                                    Log.w(TAG, "Skipping product due to missing/invalid data. ID: " + (id != null ? id : "N/A") + ", Name: " + name + ", Price: " + priceNumber);
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document " + doc.getId(), e);
                            }
                        }
                    }

                    // Notify adapter outside the loop
                    if (productAdapter != null) {
                        productAdapter.notifyDataSetChanged();
                        Log.d(TAG, "RecyclerView adapter notified. Total products in list: " + productList.size());
                    } else {
                        Log.e(TAG, "Adapter was null when trying to notify.");
                    }

                    // Update UI state AFTER processing all documents
                    showLoading(false);
                    updateEmptyViewVisibility();

                })
                .addOnFailureListener(e -> {
                    // Check binding and attachment
                    if (binding == null || !isAdded()) {
                        Log.w(TAG, "Fragment detached or binding null on Firestore failure. Aborting UI update.");
                        return;
                    }
                    Log.e(TAG, "Error fetching products from Firestore", e);
                    showLoading(false);
                    updateEmptyViewVisibility(); // Show empty list or recycler view based on current state
                    if (getContext() != null) { // Check context for Toast
                        Toast.makeText(getContext(), "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return; // Check binding validity

        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        // Only hide other views when loading STARTS
        if (isLoading) {
            binding.recyclerViewProducts.setVisibility(View.GONE);
            binding.textViewEmptyList.setVisibility(View.GONE);
        }
        // Let updateEmptyViewVisibility handle showing recycler/empty text when loading finishes
    }

    private void updateEmptyViewVisibility() {
        if (binding == null) return; // Check binding validity

        // Update visibility only if loading is finished
        if (binding.progressBar.getVisibility() == View.GONE) {
            boolean isEmpty = productList.isEmpty();
            binding.textViewEmptyList.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerViewProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            if (isEmpty) {
                Log.d(TAG, "Product list is empty. Showing empty text view.");
            } else {
                Log.d(TAG, "Product list has items. Showing RecyclerView.");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Nullify the binding
    }
}