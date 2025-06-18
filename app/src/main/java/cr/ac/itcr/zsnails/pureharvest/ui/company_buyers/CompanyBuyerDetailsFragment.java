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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cr.ac.itcr.zsnails.pureharvest.R;
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
            fetchOrdersAndProductDetails(buyerId, sellerId);
        } else {
            Log.e(TAG, "Buyer ID or Seller ID is null or empty.");
            binding.progressBarDetails.setVisibility(View.GONE);
            binding.textViewDetailsError.setText(getString(R.string.error_invalid_buyer_or_seller_id));
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
        final String naValue = getString(R.string.info_not_available);

        db.collection("users").document(id).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressBarDetails.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        String fullName = document.getString("fullName");
                        buyerEmail = document.getString("email");
                        buyerPhone = document.getString("phone");

                        binding.textViewBuyerDetailName.setText(fullName != null ? fullName : naValue);
                        binding.textViewBuyerDetailEmail.setText(buyerEmail != null ? buyerEmail : naValue);
                        binding.textViewBuyerDetailPhone.setText(buyerPhone != null ? buyerPhone : naValue);

                        binding.layoutDetailsContent.setVisibility(View.VISIBLE);
                        if ((buyerPhone != null && !buyerPhone.isEmpty()) || (buyerEmail != null && !buyerEmail.isEmpty())) {
                            binding.buttonContactBuyer.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error getting buyer details: ", task.getException());
                        binding.textViewDetailsError.setText(getString(R.string.error_loading_buyer_details));
                        binding.textViewDetailsError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void fetchOrdersAndProductDetails(String bId, String sId) {
        binding.progressBarProducts.setVisibility(View.VISIBLE);
        binding.recyclerViewPurchasedProducts.setVisibility(View.GONE);
        binding.textViewNoProducts.setVisibility(View.GONE);

        db.collection("orders").whereEqualTo("userId", bId).get().addOnCompleteListener(orderTask -> {
            if (!isAdded() || binding == null) return;

            if (orderTask.isSuccessful() && orderTask.getResult() != null) {
                QuerySnapshot ordersSnapshot = orderTask.getResult();
                if (ordersSnapshot.isEmpty()) {
                    binding.progressBarProducts.setVisibility(View.GONE);
                    binding.textViewNoProducts.setVisibility(View.VISIBLE);
                    binding.textViewBuyerDetailItemsBought.setText("0");
                    updateProductListUI(new ArrayList<>());
                    return;
                }

                AtomicInteger relevantOrderCount = new AtomicInteger(0);
                List<PurchasedProduct> allFilteredProducts = Collections.synchronizedList(new ArrayList<>());
                AtomicInteger ordersToProcess = new AtomicInteger(ordersSnapshot.size());

                if (ordersToProcess.get() == 0) {
                    binding.progressBarProducts.setVisibility(View.GONE);
                    binding.textViewBuyerDetailItemsBought.setText("0");
                    updateProductListUI(new ArrayList<>());
                    return;
                }

                for (QueryDocumentSnapshot orderDoc : ordersSnapshot) {
                    List<Map<String, Object>> productsInOrder = (List<Map<String, Object>>) orderDoc.get("productsBought");
                    Timestamp timestamp = orderDoc.getTimestamp("date");
                    Date orderDate = (timestamp != null) ? timestamp.toDate() : new Date();

                    if (productsInOrder == null || productsInOrder.isEmpty()) {
                        if (ordersToProcess.decrementAndGet() == 0) {
                            binding.textViewBuyerDetailItemsBought.setText(String.valueOf(relevantOrderCount.get()));
                            updateProductListUI(allFilteredProducts);
                        }
                        continue;
                    }

                    List<Task<DocumentSnapshot>> productTasks = new ArrayList<>();
                    for (Map<String, Object> productRef : productsInOrder) {
                        Object idObject = productRef.get("id");
                        if (idObject instanceof String) {
                            productTasks.add(db.collection("products").document((String) idObject).get());
                        }
                    }

                    Tasks.whenAllSuccess(productTasks).addOnSuccessListener(results -> {
                        boolean orderIsRelevant = false;
                        for (Object result : results) {
                            DocumentSnapshot productDoc = (DocumentSnapshot) result;
                            if (productDoc.exists() && sId.equals(productDoc.getString("sellerId"))) {
                                orderIsRelevant = true;
                                String name = productDoc.getString("name");
                                Double price = productDoc.getDouble("price");
                                List<String> imageUrls = (List<String>) productDoc.get("imageUrls");
                                String imageUrl = (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
                                allFilteredProducts.add(new PurchasedProduct(productDoc.getId(), name, price != null ? price : 0.0, orderDate, imageUrl));
                            }
                        }
                        if (orderIsRelevant) {
                            relevantOrderCount.incrementAndGet();
                        }
                        if (ordersToProcess.decrementAndGet() == 0) {
                            Map<String, PurchasedProduct> uniqueProductsMap = new LinkedHashMap<>();
                            for (PurchasedProduct product : allFilteredProducts) {
                                uniqueProductsMap.put(product.getProductId(), product);
                            }
                            List<PurchasedProduct> uniqueProductList = new ArrayList<>(uniqueProductsMap.values());

                            binding.textViewBuyerDetailItemsBought.setText(String.valueOf(relevantOrderCount.get()));
                            updateProductListUI(uniqueProductList);
                        }
                    }).addOnFailureListener(e -> {
                        if (ordersToProcess.decrementAndGet() == 0) {
                            binding.textViewBuyerDetailItemsBought.setText(String.valueOf(relevantOrderCount.get()));
                            updateProductListUI(allFilteredProducts);
                        }
                    });
                }
            } else {
                Log.e(TAG, "Error getting orders for product list", orderTask.getException());
                binding.progressBarProducts.setVisibility(View.GONE);
                binding.textViewNoProducts.setText(getString(R.string.error_loading_products));
                binding.textViewNoProducts.setVisibility(View.VISIBLE);
            }
        });
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
        if (getContext() == null) return;
        final CharSequence[] options = getResources().getStringArray(R.array.contact_options);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.contact_dialog_title));
        builder.setItems(options, (dialog, item) -> {
            switch (item) {
                case 0:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + buyerPhone)));
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_phone_not_available), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + buyerPhone)));
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_phone_not_available), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + buyerPhone)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getContext(), getString(R.string.error_whatsapp_not_installed), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_phone_not_available), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3:
                    if (buyerEmail != null && !buyerEmail.isEmpty()) {
                        try {
                            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + buyerEmail)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getContext(), getString(R.string.error_no_email_client), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_email_not_available), Toast.LENGTH_SHORT).show();
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