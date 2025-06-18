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

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentStandOutPaymentBinding;

public class StandOutPaymentFragment extends Fragment {

    private FragmentStandOutPaymentBinding binding;
    private FirebaseFirestore db;
    private String productId;
    private String productName;

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
            Toast.makeText(getContext(), getString(R.string.stand_out_payment_missing_product_id), Toast.LENGTH_LONG).show();
            binding.buttonConfirmPaymentAction.setEnabled(false);
            return;
        }

        fetchProductDetails();
        binding.buttonConfirmPaymentAction.setOnClickListener(v -> showConfirmationDialog());
        binding.buttonCancelPayment.setOnClickListener(v -> navigateBack());
    }

    private void fetchProductDetails() {
        binding.progressBarPayment.setVisibility(View.VISIBLE);
        binding.buttonConfirmPaymentAction.setEnabled(false);

        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        productName = documentSnapshot.getString("name");
                        Double price = documentSnapshot.getDouble("price");
                        List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");
                        String imageUrl = null;
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            imageUrl = imageUrls.get(0);
                        }

                        binding.textProductNameFeature.setText(productName);
                        if (price != null) {
                            binding.textProductPriceFeature.setText(String.format(Locale.US, "$%.2f", price));
                        } else {
                            binding.textProductPriceFeature.setText(getString(R.string.stand_out_payment_price_not_available));
                        }

                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(binding.imageProductToFeature);

                        binding.productDetailsGroup.setVisibility(View.VISIBLE);
                        binding.progressBarPayment.setVisibility(View.GONE);
                        binding.buttonConfirmPaymentAction.setEnabled(true);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.stand_out_payment_load_details_failed), Toast.LENGTH_SHORT).show();
                        binding.progressBarPayment.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), getString(R.string.stand_out_payment_load_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                    binding.progressBarPayment.setVisibility(View.GONE);
                });
    }

    private void showConfirmationDialog() {
        String confirmationMessage = getString(R.string.stand_out_payment_confirmation_dialog_message, (productName != null ? productName : ""));

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.stand_out_payment_confirmation_dialog_title)
                .setMessage(confirmationMessage)
                .setPositiveButton(R.string.stand_out_payment_confirmation_dialog_positive, (dialog, which) -> processPayment())
                .setNegativeButton(R.string.stand_out_payment_confirmation_dialog_negative, null)
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
                    Toast.makeText(getContext(), getString(R.string.stand_out_payment_success), Toast.LENGTH_LONG).show();
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    binding.progressBarPayment.setVisibility(View.GONE);
                    binding.buttonConfirmPaymentAction.setEnabled(true);
                    Toast.makeText(getContext(), getString(R.string.stand_out_payment_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateBack() {
        if (isAdded() && getView() != null) {
            NavController navController = Navigation.findNavController(requireView());
            navController.popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}