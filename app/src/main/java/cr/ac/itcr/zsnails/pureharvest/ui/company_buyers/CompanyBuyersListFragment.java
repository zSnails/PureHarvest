package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import cr.ac.itcr.zsnails.pureharvest.MainActivity; // Asegúrate que esta importación sea correcta
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyersListBinding;

public class CompanyBuyersListFragment extends Fragment {

    private static final String TAG = "CompanyBuyersFragment";
    private FragmentCompanyBuyersListBinding binding;
    private CompanyBuyersAdapter adapter;
    private List<CompanyBuyer> companyBuyerList;
    private FirebaseFirestore db;
    private String currentSellerId; // Para almacenar el idGlobalUser

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
            Toast.makeText(getContext(), "Error: Seller ID not available.", Toast.LENGTH_LONG).show();
            binding.progressBarCompanyBuyers.setVisibility(View.GONE);
            binding.textViewNoBuyers.setText("Seller ID not configured.");
            binding.textViewNoBuyers.setVisibility(View.VISIBLE);
        }
    }

    private void fetchCompanyBuyersData() {
       // binding.progressBarCompanyBuyers.setVisibility(View.VISIBLE);
        binding.recyclerViewCompanyBuyers.setVisibility(View.GONE);
       // binding.textViewNoBuyers.setVisibility(View.GONE);

        db.collection("orders")
                .whereEqualTo("sellerId", currentSellerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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
                            binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                            adapter.updateData(new ArrayList<>()); // Limpiar lista si estaba llena
                            Log.d(TAG, "No orders found for this seller, or no userIds in orders.");
                        } else {
                            fetchUserDetails(uniqueUserIds, userOrderCounts);
                        }
                    } else {
                        binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                        binding.textViewNoBuyers.setText("Error fetching orders.");
                        binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Error getting orders: ", task.getException());
                        Toast.makeText(getContext(), "Error fetching orders: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserDetails(List<String> userIds, Map<String, Integer> userOrderCounts) {
        List<CompanyBuyer> fetchedBuyers = new ArrayList<>();

        if (userIds.isEmpty()) {
            binding.progressBarCompanyBuyers.setVisibility(View.GONE);
            binding.textViewNoBuyers.setVisibility(View.VISIBLE);
            adapter.updateData(new ArrayList<>());
            return;
        }

        AtomicInteger tasksCompleted = new AtomicInteger(0);
        int totalTasks = userIds.size();

        for (String userId : userIds) {
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            DocumentSnapshot userDocument = userTask.getResult();
                            if (userDocument != null && userDocument.exists()) {
                                String fullName = userDocument.getString("fullName");
                                if (fullName == null) fullName = "N/A";

                                int orderCount = userOrderCounts.getOrDefault(userId, 0);
                                fetchedBuyers.add(new CompanyBuyer(userId, fullName, orderCount));
                            } else {
                                Log.w(TAG, "User document not found for ID: " + userId + ". Using ID as name.");
                                int orderCount = userOrderCounts.getOrDefault(userId, 0);
                                fetchedBuyers.add(new CompanyBuyer(userId, "User: " + userId + " (Not Found)", orderCount));
                            }
                        } else {
                            Log.e(TAG, "Error fetching user details for " + userId, userTask.getException());
                            int orderCount = userOrderCounts.getOrDefault(userId, 0);
                            fetchedBuyers.add(new CompanyBuyer(userId, "User: " + userId + " (Error)", orderCount));
                        }

                        if (tasksCompleted.incrementAndGet() == totalTasks) {
                            binding.progressBarCompanyBuyers.setVisibility(View.GONE);
                            if (fetchedBuyers.isEmpty()) {
                                binding.textViewNoBuyers.setVisibility(View.VISIBLE);
                            } else {
                                binding.recyclerViewCompanyBuyers.setVisibility(View.VISIBLE);
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