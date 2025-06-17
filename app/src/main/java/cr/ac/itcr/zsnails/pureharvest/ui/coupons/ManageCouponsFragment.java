package cr.ac.itcr.zsnails.pureharvest.ui.coupons;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentManageCouponsBinding;

public class ManageCouponsFragment extends Fragment {

    private FragmentManageCouponsBinding binding;
    private FirebaseFirestore firestore;
    private String productId;
    private String editingCouponId = null;
    private boolean isEditing = false;

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

        binding.buttonDeleteCoupon.setOnClickListener(v -> {
            if (editingCouponId != null) {
                firestore.collection("coupons").document(editingCouponId)
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Cupón eliminado", Toast.LENGTH_SHORT).show();
                            resetForm();
                            loadCoupons();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
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

        double discount;
        int maxUses;
        try {
            discount = Double.parseDouble(discountStr);
            maxUses = Integer.parseInt(maxUsesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Formato inválido en descuento o usos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (discount <= 0 || discount > 100) {
            Toast.makeText(getContext(), "El descuento debe ser entre 1% y 100%", Toast.LENGTH_SHORT).show();
            return;
        }

        if (maxUses <= 0) {
            Toast.makeText(getContext(), "El número de usos debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String sellerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";

        Map<String, Object> couponData = new HashMap<>();
        couponData.put("code", code);
        couponData.put("discountPercentage", discount);
        couponData.put("maxUses", maxUses);
        couponData.put("expirationTimestamp", expiration);
        couponData.put("sellerId", sellerId);
        couponData.put("applicableProductIds", java.util.Arrays.asList(productId));

        if (isEditing && editingCouponId != null) {
            firestore.collection("coupons").document(editingCouponId)
                    .update(couponData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Cupón actualizado", Toast.LENGTH_SHORT).show();
                        resetForm();
                        loadCoupons();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            couponData.put("uses", 0); // Solo al crear
            firestore.collection("coupons").add(couponData)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(getContext(), "Cupón creado", Toast.LENGTH_SHORT).show();
                        resetForm();
                        loadCoupons();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void loadCoupons() {
        if (productId == null) return;

        firestore.collection("coupons")
                .whereArrayContains("applicableProductIds", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> coupons = new ArrayList<>();
                    List<String> documentIds = new ArrayList<>();

                    for (var doc : querySnapshot.getDocuments()) {
                        coupons.add(doc.getData());
                        documentIds.add(doc.getId());
                    }

                    binding.recyclerCoupons.setAdapter(new CouponAdapter(coupons, (position, couponData) -> {
                        editingCouponId = documentIds.get(position);
                        isEditing = true;

                        binding.editCouponCode.setText((String) couponData.get("code"));
                        binding.editCouponDiscount.setText(String.valueOf(couponData.get("discountPercentage")));
                        binding.editCouponMaxUses.setText(String.valueOf(couponData.get("maxUses")));

                        Long expiration = (Long) couponData.get("expirationTimestamp");
                        if (expiration != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(expiration);
                            binding.editCouponExpiration.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                                    (calendar.get(Calendar.MONTH) + 1) + "/" +
                                    calendar.get(Calendar.YEAR));
                            binding.editCouponExpiration.setTag(expiration);
                        }

                        binding.buttonSaveCoupon.setText("Update coupon");
                        binding.buttonDeleteCoupon.setVisibility(View.VISIBLE);
                    }));
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error cargando cupones", Toast.LENGTH_SHORT).show());
    }

    private void resetForm() {
        binding.editCouponCode.setText("");
        binding.editCouponDiscount.setText("");
        binding.editCouponMaxUses.setText("");
        binding.editCouponExpiration.setText("");
        binding.editCouponExpiration.setTag(null);
        binding.buttonSaveCoupon.setText("Save Coupon");
        binding.buttonDeleteCoupon.setVisibility(View.GONE);
        isEditing = false;
        editingCouponId = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}