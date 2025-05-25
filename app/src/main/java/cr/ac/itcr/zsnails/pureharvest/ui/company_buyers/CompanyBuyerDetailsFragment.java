package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyerDetailsBinding;

public class CompanyBuyerDetailsFragment extends Fragment {

    private static final String TAG = "BuyerDetailsFragment";
    private FragmentCompanyBuyerDetailsBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private String userName;
    private int itemsBought;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            userId = getArguments().getString(CompanyBuyersAdapter.KEY_USER_ID);
            userName = getArguments().getString(CompanyBuyersAdapter.KEY_USER_NAME, "N/A");
            itemsBought = getArguments().getInt(CompanyBuyersAdapter.KEY_ITEMS_BOUGHT, 0);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompanyBuyerDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty. Cannot load details.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Buyer ID not provided.", Toast.LENGTH_LONG).show();
            }
            binding.textViewNoDetails.setVisibility(View.VISIBLE);
            binding.detailsScrollView.setVisibility(View.GONE);
            binding.progressBarBuyerDetails.setVisibility(View.GONE);
            return;
        }

        binding.textViewDetailId.setText(userId);
        binding.textViewDetailName.setText(userName);
        binding.textViewDetailItemsBought.setText(String.valueOf(itemsBought));

        fetchAdditionalBuyerDetails();
    }

    private void fetchAdditionalBuyerDetails() {
        binding.progressBarBuyerDetails.setVisibility(View.VISIBLE);
        binding.detailsScrollView.setVisibility(View.VISIBLE);
        binding.textViewNoDetails.setVisibility(View.GONE);


        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) {
                        binding.progressBarBuyerDetails.setVisibility(View.GONE);
                        return;
                    }
                    binding.progressBarBuyerDetails.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String email = document.getString("email");
                            String phone = document.getString("phone");

                            binding.textViewDetailEmail.setText(email != null ? email : "N/A");
                            binding.textViewDetailPhone.setText(phone != null ? phone : "N/A");
                            binding.detailsScrollView.setVisibility(View.VISIBLE);

                        } else {
                            Log.w(TAG, "No such user document for ID: " + userId);
                            Toast.makeText(getContext(), "Buyer details not found.", Toast.LENGTH_SHORT).show();
                            binding.textViewDetailEmail.setText("N/A");
                            binding.textViewDetailPhone.setText("N/A");
                        }
                    } else {
                        Log.e(TAG, "Error getting user details: ", task.getException());
                        Toast.makeText(getContext(), "Error fetching buyer details.", Toast.LENGTH_SHORT).show();
                        binding.textViewDetailEmail.setText("Error");
                        binding.textViewDetailPhone.setText("Error");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}