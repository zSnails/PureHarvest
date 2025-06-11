package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyerDetailsBinding;

public class CompanyBuyerDetailsFragment extends Fragment {

    private static final String TAG = "BuyerDetailsFragment";
    private FragmentCompanyBuyerDetailsBinding binding;
    private FirebaseFirestore db;
    private String buyerId;

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

        if (getArguments() != null) {
            buyerId = getArguments().getString("buyer_id");
        }

        if (buyerId != null && !buyerId.isEmpty()) {
            fetchBuyerDetails(buyerId);
        } else {
            Log.e(TAG, "Buyer ID is null or empty.");
            binding.progressBarDetails.setVisibility(View.GONE);
            binding.textViewDetailsError.setText("Invalid buyer ID.");
            binding.textViewDetailsError.setVisibility(View.VISIBLE);
        }
    }

    private void fetchBuyerDetails(String id) {
        binding.progressBarDetails.setVisibility(View.VISIBLE);
        binding.layoutDetailsContent.setVisibility(View.GONE);
        binding.textViewDetailsError.setVisibility(View.GONE);

        db.collection("users").document(id).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null || binding == null) {
                        return;
                    }

                    binding.progressBarDetails.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String fullName = document.getString("fullName");
                            String email = document.getString("email");

                            binding.textViewBuyerDetailName.setText(fullName != null ? fullName : "N/A");
                            binding.textViewBuyerDetailEmail.setText(email != null ? email : "N/A");

                            binding.layoutDetailsContent.setVisibility(View.VISIBLE);
                        } else {
                            Log.w(TAG, "No such document for buyer ID: " + id);
                            binding.textViewDetailsError.setText("Buyer details not found.");
                            binding.textViewDetailsError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error getting buyer details: ", task.getException());
                        binding.textViewDetailsError.setText("Error loading buyer details.");
                        binding.textViewDetailsError.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Error loading buyer details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}