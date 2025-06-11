package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.content.ActivityNotFoundException;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyerDetailsBinding;

public class CompanyBuyerDetailsFragment extends Fragment {

    private static final String TAG = "BuyerDetailsFragment";
    private FragmentCompanyBuyerDetailsBinding binding;
    private FirebaseFirestore db;
    private String buyerId;
    private String sellerId;
    private String buyerPhone;
    private String buyerEmail;

    private PurchasedProductsAdapter productsAdapter;
    private List<PurchasedProduct> purchasedProductList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompanyBuyerDetailsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        if (getArguments() != null) {
            buyerId = getArguments().getString("buyer_id");
            sellerId = getArguments().getString("seller_id");
        }

        if (buyerId != null && !buyerId.isEmpty() && sellerId != null && !sellerId.isEmpty()) {
            fetchBuyerDetails(buyerId);
            fetchPurchasedProducts(buyerId, sellerId);
        } else {
            Log.e(TAG, "Buyer ID or Seller ID is null or empty.");
            binding.progressBarDetails.setVisibility(View.GONE);
            binding.textViewDetailsError.setText("Invalid buyer or seller ID.");
            binding.textViewDetailsError.setVisibility(View.VISIBLE);
        }

        binding.buttonContactBuyer.setOnClickListener(v -> showContactDialog());
    }

    private void setupRecyclerView() {
        purchasedProductList = new ArrayList<>();
        productsAdapter = new PurchasedProductsAdapter(purchasedProductList);
        binding.recyclerViewPurchasedProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPurchasedProducts.setAdapter(productsAdapter);
    }

    private void fetchBuyerDetails(String id) {
        binding.progressBarDetails.setVisibility(View.VISIBLE);
        binding.layoutDetailsContent.setVisibility(View.GONE);
        binding.textViewDetailsError.setVisibility(View.GONE);
        binding.buttonContactBuyer.setVisibility(View.GONE);

        db.collection("users").document(id).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressBarDetails.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        String fullName = document.getString("fullName");
                        buyerEmail = document.getString("email");
                        buyerPhone = document.getString("phone");

                        binding.textViewBuyerDetailName.setText(fullName != null ? fullName : "N/A");
                        binding.textViewBuyerDetailEmail.setText(buyerEmail != null ? buyerEmail : "N/A");
                        binding.textViewBuyerDetailPhone.setText(buyerPhone != null ? buyerPhone : "N/A");

                        binding.layoutDetailsContent.setVisibility(View.VISIBLE);
                        if ((buyerPhone != null && !buyerPhone.isEmpty()) || (buyerEmail != null && !buyerEmail.isEmpty())) {
                            binding.buttonContactBuyer.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error getting buyer details: ", task.getException());
                        binding.textViewDetailsError.setText("Error loading buyer details.");
                        binding.textViewDetailsError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void fetchPurchasedProducts(String bId, String sId) {
        binding.progressBarProducts.setVisibility(View.VISIBLE);
        binding.recyclerViewPurchasedProducts.setVisibility(View.GONE);
        binding.textViewNoProducts.setVisibility(View.GONE);

        db.collection("orders")
                .whereEqualTo("userId", bId)
                .whereEqualTo("sellerId", sId)
                .get()
                .addOnCompleteListener(orderTask -> {
                    if (!isAdded() || binding == null) return;

                    if (orderTask.isSuccessful() && orderTask.getResult() != null) {
                        if (orderTask.getResult().isEmpty()) {
                            binding.progressBarProducts.setVisibility(View.GONE);
                            binding.textViewNoProducts.setVisibility(View.VISIBLE);
                            return;
                        }

                        List<QueryDocumentSnapshot> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : orderTask.getResult()) {
                            orders.add(doc);
                        }
                        processProductDetails(orders);

                    } else {
                        Log.e(TAG, "Error getting orders for product list", orderTask.getException());
                        binding.progressBarProducts.setVisibility(View.GONE);
                        binding.textViewNoProducts.setText("Error loading products.");
                        binding.textViewNoProducts.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void processProductDetails(List<QueryDocumentSnapshot> orders) {
        List<PurchasedProduct> fetchedProducts = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        int totalOrders = orders.size();

        for (QueryDocumentSnapshot orderDoc : orders) {
            String productId = orderDoc.getString("productId");
            Timestamp timestamp = orderDoc.getTimestamp("date");
            Date date = (timestamp != null) ? timestamp.toDate() : null;

            if (productId == null || productId.isEmpty()) {
                if (counter.incrementAndGet() == totalOrders) {
                    updateProductListUI(fetchedProducts);
                }
                continue;
            }

            db.collection("products").document(productId).get()
                    .addOnCompleteListener(productTask -> {
                        if (productTask.isSuccessful() && productTask.getResult() != null && productTask.getResult().exists()) {
                            DocumentSnapshot productDoc = productTask.getResult();
                            String name = productDoc.getString("name");
                            Double price = productDoc.getDouble("price");
                            String firstImageUrl = null;
                            try {
                                List<String> imageUrls = (List<String>) productDoc.get("imageUrls");
                                if (imageUrls != null && !imageUrls.isEmpty()) {
                                    firstImageUrl = imageUrls.get(0);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing imageUrls", e);
                            }
                            fetchedProducts.add(new PurchasedProduct(productId, name != null ? name : "Unknown", price != null ? price : 0.0, date, firstImageUrl));
                        } else {
                            Log.w(TAG, "Product not found for ID: " + productId);
                            fetchedProducts.add(new PurchasedProduct(productId, "Product not found", 0.0, date, null));
                        }

                        if (counter.incrementAndGet() == totalOrders) {
                            updateProductListUI(fetchedProducts);
                        }
                    });
        }
    }

    private void updateProductListUI(List<PurchasedProduct> productList) {
        if (!isAdded() || binding == null) return;
        binding.progressBarProducts.setVisibility(View.GONE);
        if (productList.isEmpty()) {
            binding.textViewNoProducts.setVisibility(View.VISIBLE);
            binding.recyclerViewPurchasedProducts.setVisibility(View.GONE);
        } else {
            binding.textViewNoProducts.setVisibility(View.GONE);
            productsAdapter.updateData(productList);
            binding.recyclerViewPurchasedProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showContactDialog() {
        final CharSequence[] options = {"Call", "Send SMS", "Send WhatsApp", "Send Email"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Contact Buyer");
        builder.setItems(options, (dialog, item) -> {
            switch (item) {
                case 0:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + buyerPhone)));
                    } else {
                        Toast.makeText(getContext(), "Phone number not available.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + buyerPhone)));
                    } else {
                        Toast.makeText(getContext(), "Phone number not available.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + buyerPhone)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getContext(), "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Phone number not available.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3:
                    if (buyerEmail != null && !buyerEmail.isEmpty()) {
                        try {
                            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + buyerEmail)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getContext(), "No email client found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Email address not available.", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}