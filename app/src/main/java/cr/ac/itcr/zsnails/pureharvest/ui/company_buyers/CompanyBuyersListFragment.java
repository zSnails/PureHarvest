package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cr.ac.itcr.zsnails.pureharvest.MainActivity;
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyersListBinding;

public class CompanyBuyersListFragment extends Fragment implements CompanyBuyersAdapter.OnBuyerClickListener {

    private static final String TAG = "CompanyBuyersFragment";
    private FragmentCompanyBuyersListBinding binding;
    private CompanyBuyersAdapter adapter;
    private List<CompanyBuyer> companyBuyerList;
    private FirebaseFirestore db;
    private String currentSellerId;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompanyBuyersListBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        companyBuyerList = new ArrayList<>();
        adapter = new CompanyBuyersAdapter(companyBuyerList, this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            navController = Navigation.findNavController(view);
        } catch (IllegalStateException e) {
            Log.e(TAG, "NavController not found for this view.", e);
        }

        binding.recyclerViewCompanyBuyers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCompanyBuyers.setAdapter(adapter);

        if (MainActivity.idGlobalUser != null && !MainActivity.idGlobalUser.isEmpty()) {
            currentSellerId = MainActivity.idGlobalUser;
            Log.d(TAG, "Current Seller ID: " + currentSellerId);
            fetchCompanyBuyersData();
        } else {
            Log.e(TAG, "idGlobalUser is null or empty. Cannot fetch buyers.");
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.error_seller_id_not_available), Toast.LENGTH_LONG).show();
            }
            if (binding != null) {
                binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                binding.textViewNoBuyers.setText(getString(R.string.error_seller_id_not_configured));
                binding.textViewNoBuyers.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchCompanyBuyersData() {
        if (binding == null) return;
        binding.progressBarCompanyBuyers.setVisibility(View.VISIBLE);
        binding.recyclerViewCompanyBuyers.setVisibility(View.GONE);
        binding.textViewNoBuyers.setVisibility(View.GONE);

        db.collection("orders").get().addOnCompleteListener(task -> {
            if (!isAdded() || getContext() == null || binding == null) {
                if (binding != null) binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                return;
            }
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot ordersSnapshot = task.getResult();
                if (ordersSnapshot.isEmpty()) {
                    binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                    binding.textViewNoBuyers.setText(getString(R.string.info_no_buyers_found));
                    binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                    adapter.updateData(new ArrayList<>());
                    return;
                }

                Map<String, Integer> userPurchaseCounts = new HashMap<>();
                AtomicInteger ordersProcessed = new AtomicInteger(0);
                int totalOrders = ordersSnapshot.size();

                for (QueryDocumentSnapshot orderDoc : ordersSnapshot) {
                    String userId = orderDoc.getString("userId");
                    List<Map<String, Object>> productsBought = (List<Map<String, Object>>) orderDoc.get("productsBought");

                    if (userId == null || productsBought == null || productsBought.isEmpty()) {
                        if (ordersProcessed.incrementAndGet() == totalOrders) {
                            fetchUserDetailsFromFirebase(userPurchaseCounts);
                        }
                        continue;
                    }

                    List<Task<DocumentSnapshot>> productTasks = new ArrayList<>();
                    for (Map<String, Object> productRef : productsBought) {
                        Object idObject = productRef.get("id");
                        if (idObject instanceof String) {
                            productTasks.add(db.collection("products").document((String) idObject).get());
                        }
                    }

                    if (productTasks.isEmpty()) {
                        if (ordersProcessed.incrementAndGet() == totalOrders) {
                            fetchUserDetailsFromFirebase(userPurchaseCounts);
                        }
                        continue;
                    }

                    Tasks.whenAllSuccess(productTasks).addOnSuccessListener(results -> {
                        int itemsFromThisSeller = 0;
                        for (int i = 0; i < results.size(); i++) {
                            DocumentSnapshot productDoc = (DocumentSnapshot) results.get(i);
                            if (productDoc.exists() && currentSellerId.equals(productDoc.getString("sellerId"))) {
                                Map<String, Object> productRef = productsBought.get(i);
                                Object amountObj = productRef.get("amount");
                                if (amountObj instanceof Number) {
                                    itemsFromThisSeller += ((Number) amountObj).intValue();
                                } else {
                                    itemsFromThisSeller += 1;
                                }
                            }
                        }
                        if (itemsFromThisSeller > 0) {
                            userPurchaseCounts.put(userId, userPurchaseCounts.getOrDefault(userId, 0) + itemsFromThisSeller);
                        }

                        if (ordersProcessed.incrementAndGet() == totalOrders) {
                            fetchUserDetailsFromFirebase(userPurchaseCounts);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching product details for an order", e);
                        if (ordersProcessed.incrementAndGet() == totalOrders) {
                            fetchUserDetailsFromFirebase(userPurchaseCounts);
                        }
                    });
                }
            } else {
                binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                binding.textViewNoBuyers.setText(getString(R.string.error_fetching_orders));
                binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error getting orders: ", task.getException());
            }
        });
    }

    private void fetchUserDetailsFromFirebase(Map<String, Integer> userOrderCounts) {
        if (userOrderCounts.isEmpty()) {
            if(binding != null) {
                binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                binding.textViewNoBuyers.setText(getString(R.string.info_no_buyers_found));
                binding.textViewNoBuyers.setVisibility(View.VISIBLE);
            }
            adapter.updateData(new ArrayList<>());
            return;
        }

        List<CompanyBuyer> fetchedBuyers = new ArrayList<>();
        AtomicInteger tasksCompleted = new AtomicInteger(0);
        int totalTasks = userOrderCounts.size();
        final String naValue = getString(R.string.info_not_available_short);

        for (String userId : userOrderCounts.keySet()) {
            db.collection("users").document(userId).get().addOnCompleteListener(userTask -> {
                if (!isAdded() || getContext() == null) {
                    if (tasksCompleted.incrementAndGet() == totalTasks && binding != null) {
                        binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                    }
                    return;
                }
                if (userTask.isSuccessful()) {
                    DocumentSnapshot userDocument = userTask.getResult();
                    if (userDocument != null && userDocument.exists()) {
                        String fullName = userDocument.getString("fullName");
                        String email = userDocument.getString("email");
                        String phone = userDocument.getString("phone");
                        int orderCount = userOrderCounts.getOrDefault(userId, 0);

                        fetchedBuyers.add(new CompanyBuyer(
                                userDocument.getId(),
                                (fullName != null ? fullName : naValue),
                                orderCount,
                                (email != null ? email : naValue),
                                (phone != null ? phone : naValue)
                        ));
                    }
                }
                if (tasksCompleted.incrementAndGet() == totalTasks) {
                    if (binding != null) {
                        binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                        if (fetchedBuyers.isEmpty()) {
                            binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                        } else {
                            binding.recyclerViewCompanyBuyers.setVisibility(View.VISIBLE);
                        }
                    }
                    adapter.updateData(fetchedBuyers);
                }
            });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewDetailsClick(CompanyBuyer buyer) {
        if (getContext() == null || !isAdded() || navController == null) {
            Log.w(TAG, "Cannot navigate: context/fragment not added or NavController is null.");
            return;
        }
        String buyerId = buyer.getId();
        if (buyerId == null || buyerId.isEmpty()) {
            Log.e(TAG, "Buyer ID is null or empty, cannot navigate to details.");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("buyer_id", buyerId);
        bundle.putString("seller_id", currentSellerId);
        try {
            navController.navigate(R.id.action_companyBuyersListFragment_to_companyBuyerDetailsFragment, bundle);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Navigation action/destination not found or other navigation error.", e);
        }
    }
}