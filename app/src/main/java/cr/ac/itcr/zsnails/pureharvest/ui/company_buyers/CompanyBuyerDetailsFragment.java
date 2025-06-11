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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyerDetailsBinding;

public class CompanyBuyerDetailsFragment extends Fragment {

    private static final String TAG = "BuyerDetailsFragment";
    private FragmentCompanyBuyerDetailsBinding binding;
    private FirebaseFirestore db;
    private String buyerId;
    private String sellerId;
    private String buyerPhone;
    private String buyerEmail;

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
            sellerId = getArguments().getString("seller_id");
        }

        if (buyerId != null && !buyerId.isEmpty() && sellerId != null && !sellerId.isEmpty()) {
            fetchBuyerDetails(buyerId);
            fetchOrderCount(buyerId, sellerId);
        } else {
            Log.e(TAG, "Buyer ID or Seller ID is null or empty.");
            binding.progressBarDetails.setVisibility(View.GONE);
            binding.textViewDetailsError.setText("Invalid buyer or seller ID.");
            binding.textViewDetailsError.setVisibility(View.VISIBLE);
        }

        binding.buttonContactBuyer.setOnClickListener(v -> showContactDialog());
    }

    private void fetchBuyerDetails(String id) {
        binding.progressBarDetails.setVisibility(View.VISIBLE);
        binding.layoutDetailsContent.setVisibility(View.GONE);
        binding.textViewDetailsError.setVisibility(View.GONE);
        binding.buttonContactBuyer.setVisibility(View.GONE);

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
                            buyerEmail = document.getString("email");
                            buyerPhone = document.getString("phone");

                            binding.textViewBuyerDetailId.setText(id);
                            binding.textViewBuyerDetailName.setText(fullName != null ? fullName : "N/A");
                            binding.textViewBuyerDetailEmail.setText(buyerEmail != null ? buyerEmail : "N/A");
                            binding.textViewBuyerDetailPhone.setText(buyerPhone != null ? buyerPhone : "N/A");

                            binding.layoutDetailsContent.setVisibility(View.VISIBLE);
                            if ((buyerPhone != null && !buyerPhone.isEmpty()) || (buyerEmail != null && !buyerEmail.isEmpty())) {
                                binding.buttonContactBuyer.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.w(TAG, "No such document for buyer ID: " + id);
                            binding.textViewDetailsError.setText("Buyer details not found.");
                            binding.textViewDetailsError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error getting buyer details: ", task.getException());
                        binding.textViewDetailsError.setText("Error loading buyer details.");
                        binding.textViewDetailsError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void fetchOrderCount(String bId, String sId) {
        binding.textViewBuyerDetailItemsBought.setText("Loading...");

        db.collection("orders")
                .whereEqualTo("userId", bId)
                .whereEqualTo("sellerId", sId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null || binding == null) {
                        return;
                    }

                    if (task.isSuccessful() && task.getResult() != null) {
                        int count = task.getResult().size();
                        binding.textViewBuyerDetailItemsBought.setText(String.valueOf(count));
                    } else {
                        Log.e(TAG, "Error getting order count: ", task.getException());
                        binding.textViewBuyerDetailItemsBought.setText("N/A");
                    }
                });
    }


    private void showContactDialog() {
        final CharSequence[] options = {"Call", "Send SMS", "Send WhatsApp", "Send Email"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Contact Buyer");
        builder.setItems(options, (dialog, item) -> {
            switch (item) {
                case 0:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + buyerPhone));
                        startActivity(callIntent);
                    } else {
                        Toast.makeText(getContext(), "Phone number not available.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + buyerPhone));
                        startActivity(smsIntent);
                    } else {
                        Toast.makeText(getContext(), "Phone number not available.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    if (buyerPhone != null && !buyerPhone.isEmpty()) {
                        try {
                            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                            whatsappIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + buyerPhone));
                            startActivity(whatsappIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getContext(), "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Phone number not available.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3:
                    if (buyerEmail != null && !buyerEmail.isEmpty()) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + buyerEmail));
                        try {
                            startActivity(emailIntent);
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