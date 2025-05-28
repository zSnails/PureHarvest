package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentStandOutPaymentBinding;

public class StandOutPaymentFragment extends Fragment {

    private FragmentStandOutPaymentBinding binding;
    private FirebaseFirestore db;
    private String productId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStandOutPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(getContext(), "Product ID is missing. Cannot proceed.", Toast.LENGTH_LONG).show();
            binding.buttonConfirmPaymentAction.setEnabled(false);
            return;
        }

        binding.buttonConfirmPaymentAction.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Payment")
                .setMessage("Are you sure you want to proceed with the payment to feature this product?")
                .setPositiveButton("Confirm", (dialog, which) -> processPayment())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPayment() {
        binding.progressBarPayment.setVisibility(View.VISIBLE);
        binding.buttonConfirmPaymentAction.setEnabled(false);

        List<Object> paymentData = Arrays.asList(true, Timestamp.now());

        db.collection("products").document(productId)
                .update("standOutPayment", paymentData)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBarPayment.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Payment successful! Product will now be featured.", Toast.LENGTH_LONG).show();
                    NavController navController = Navigation.findNavController(requireView());
                    navController.popBackStack();
                })
                .addOnFailureListener(e -> {
                    binding.progressBarPayment.setVisibility(View.GONE);
                    binding.buttonConfirmPaymentAction.setEnabled(true);
                    Toast.makeText(getContext(), "Payment failed. Please try again." + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}