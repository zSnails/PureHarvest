package cr.ac.itcr.zsnails.pureharvest.ui.coupons;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentManageCouponsBinding;

public class ManageCouponsFragment extends Fragment {

    private FragmentManageCouponsBinding binding;
    private FirebaseFirestore firestore;
    private String productId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageCouponsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        firestore = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        binding.editCouponExpiration.setOnClickListener(v -> showDatePicker());

        binding.buttonSaveCoupon.setOnClickListener(v -> saveCoupon());
        binding.recyclerCoupons.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadCoupons();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (DatePicker view, int year, int month, int dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth, 0, 0);
            long timestamp = calendar.getTimeInMillis();
            binding.editCouponExpiration.setTag(timestamp);
            binding.editCouponExpiration.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveCoupon() {
        String code = binding.editCouponCode.getText().toString().trim();
        String discountStr = binding.editCouponDiscount.getText().toString().trim();
        String maxUsesStr = binding.editCouponMaxUses.getText().toString().trim();
        Long expiration = (Long) binding.editCouponExpiration.getTag();

        if (code.isEmpty() || discountStr.isEmpty() || maxUsesStr.isEmpty() || expiration == null) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double discount = Double.parseDouble(discountStr);
        int maxUses = Integer.parseInt(maxUsesStr);


        Map<String, Object> couponData = new HashMap<>();
        couponData.put("code", code);
        couponData.put("discountPercentage", discount);
        couponData.put("maxUses", maxUses);
        couponData.put("uses", 0);
        couponData.put("expirationTimestamp", expiration);
        couponData.put("sellerId", "CURRENT_USER_ID"); //Luego aqui se pone el seller id cuando se logea
        couponData.put("applicableProductIds", java.util.Arrays.asList(productId));

        firestore.collection("coupons").add(couponData)
                .addOnSuccessListener(docRef -> Toast.makeText(getContext(), "Cupón creado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadCoupons() {
        if (productId == null) return;

        firestore.collection("coupons")
                .whereArrayContains("applicableProductIds", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> coupons = new ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        coupons.add(doc.getData());
                    }
                    binding.recyclerCoupons.setAdapter(new CouponAdapter(coupons));
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading coupons", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}