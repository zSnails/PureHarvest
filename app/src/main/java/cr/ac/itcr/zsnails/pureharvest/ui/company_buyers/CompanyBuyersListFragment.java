package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cr.ac.itcr.zsnails.pureharvest.MainActivity;
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyersListBinding;

public class CompanyBuyersListFragment extends Fragment {

    private static final String TAG = "CompanyBuyersFragment";
    private FragmentCompanyBuyersListBinding binding;
    private CompanyBuyersAdapter adapter;
    private List<CompanyBuyer> companyBuyerList;
    private FirebaseFirestore db;
    private String currentSellerId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompanyBuyersListBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        companyBuyerList = new ArrayList<>();
        adapter = new CompanyBuyersAdapter(companyBuyerList);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                binding.textViewNoBuyers.setText(getString(R.string.text_seller_id_not_configured));
                binding.textViewNoBuyers.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchCompanyBuyersData() {
        if (binding == null) return;
        binding.progressBarCompanyBuyers.setVisibility(View.VISIBLE);
        binding.recyclerViewCompanyBuyers.setVisibility(View.GONE);
        binding.textViewNoBuyers.setVisibility(View.GONE);

        db.collection("orders")
                .whereEqualTo("sellerId", currentSellerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null || binding == null) {
                        if (binding != null) binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                        return;
                    }
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Integer> userOrderCounts = new HashMap<>();
                        List<String> uniqueUserIds = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getString("userId");
                            if (userId != null && !userId.isEmpty()) {
                                userOrderCounts.put(userId, userOrderCounts.getOrDefault(userId, 0) + 1);
                                if (!uniqueUserIds.contains(userId)) {
                                    uniqueUserIds.add(userId);
                                }
                            }
                        }

                        if (uniqueUserIds.isEmpty()) {
                            binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                            binding.textViewNoBuyers.setText(getString(R.string.text_no_buyers_list_empty));
                            binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                            adapter.updateData(new ArrayList<>());
                            Log.d(TAG, "No orders found for this seller, or no userIds in orders.");
                        } else {
                            fetchUserDetailsFromFirebase(uniqueUserIds, userOrderCounts);
                        }
                    } else {
                        binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                        binding.textViewNoBuyers.setText(getString(R.string.error_fetching_orders_message));
                        binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Error getting orders: ", task.getException());
                        String errorMessage = getString(R.string.error_fetching_orders_toast);
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserDetailsFromFirebase(List<String> userIds, Map<String, Integer> userOrderCounts) {
        List<CompanyBuyer> fetchedBuyers = new ArrayList<>();
        if (userIds.isEmpty()) {
            if (binding != null) {
                binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                binding.textViewNoBuyers.setText(getString(R.string.text_no_buyers_list_empty));
                binding.textViewNoBuyers.setVisibility(View.VISIBLE);
            }
            adapter.updateData(new ArrayList<>());
            return;
        }

        AtomicInteger tasksCompleted = new AtomicInteger(0);
        int totalTasks = userIds.size();
        final String naValue = "N/A"; // Literal "N/A" as sentinel for the model

        for (String userId : userIds) {
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(userTask -> {
                        if (!isAdded() || getContext() == null) {
                            if (tasksCompleted.incrementAndGet() == totalTasks) {
                                if (binding != null) binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                            }
                            return;
                        }
                        if (userTask.isSuccessful()) {
                            DocumentSnapshot userDocument = userTask.getResult();
                            if (userDocument != null && userDocument.exists()) {
                                String fullName = userDocument.getString("fullName");
                                if (fullName == null || fullName.trim().isEmpty()) fullName = naValue;

                                String email = userDocument.getString("email");
                                if (email == null || email.trim().isEmpty()) email = naValue;

                                String phone = userDocument.getString("phone");
                                if (phone == null || phone.trim().isEmpty()) phone = naValue;

                                int orderCount = userOrderCounts.getOrDefault(userId, 0);
                                fetchedBuyers.add(new CompanyBuyer(userDocument.getId(), fullName, orderCount, email, phone));
                            } else {
                                Log.w(TAG, "User document not found for ID: " + userId + ". Using ID as name.");
                                int orderCount = userOrderCounts.getOrDefault(userId, 0);
                                String fallbackName = getString(R.string.user_fallback_name_format, userId);
                                fetchedBuyers.add(new CompanyBuyer(userId, fallbackName, orderCount, naValue, naValue));
                            }
                        } else {
                            Log.e(TAG, "Error fetching user details for " + userId, userTask.getException());
                            int orderCount = userOrderCounts.getOrDefault(userId, 0);
                            String errorFallbackName = getString(R.string.user_fallback_name_error_format, userId);
                            fetchedBuyers.add(new CompanyBuyer(userId, errorFallbackName, orderCount, naValue, naValue));
                        }

                        if (tasksCompleted.incrementAndGet() == totalTasks) {
                            if (binding != null) {
                                binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                                if (fetchedBuyers.isEmpty()) {
                                    binding.textViewNoBuyers.setText(getString(R.string.text_no_buyers_list_empty));
                                    binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                                } else {
                                    binding.recyclerViewCompanyBuyers.setVisibility(View.VISIBLE);
                                    binding.textViewNoBuyers.setVisibility(View.GONE);
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
}