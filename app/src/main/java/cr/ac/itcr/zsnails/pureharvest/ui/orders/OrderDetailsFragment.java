// File: cr.ac.itcr.zsnails.pureharvest.ui.orders.OrderDetailsFragment.java
package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;

public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String TAG = "OrderDetailsFragment";

    private String orderId;
    private FirebaseFirestore db;
    private Order currentOrder;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CR"));

    private TextView tvOrderIdValue, tvOrderDateValue, tvSellerIdValue, tvUserIdValue, tvProductIdFromOrder;
    private TextView tvLabelProductName, tvProductName, tvLabelProductPrice, tvProductPrice;
    private TextView tvOrderStatus, tvLabelOrderStatus;
    private Button btnChangeStatus;

    private TextView tvOrderDetailsTitle;
    private ProgressBar progressBarOrderDetails;
    private LinearLayout contentLayout;

    private NavController navController;

    public OrderDetailsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        tvOrderDetailsTitle = view.findViewById(R.id.tvOrderDetailsTitle);

        tvOrderIdValue = view.findViewById(R.id.tvOrderId);
        tvOrderDateValue = view.findViewById(R.id.tvOrderDate);
        tvSellerIdValue = view.findViewById(R.id.tvSellerId);
        tvUserIdValue = view.findViewById(R.id.tvUserId);
        tvProductIdFromOrder = view.findViewById(R.id.tvProductIds);

        tvLabelProductName = view.findViewById(R.id.tvLabelProductName);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvLabelProductPrice = view.findViewById(R.id.tvLabelProductPrice);
        tvProductPrice = view.findViewById(R.id.tvProductPrice);

        tvLabelOrderStatus = view.findViewById(R.id.tvLabelOrderStatus);
        tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
        btnChangeStatus = view.findViewById(R.id.btnChangeStatus);

        progressBarOrderDetails = view.findViewById(R.id.progressBarOrderDetails);
        contentLayout = view.findViewById(R.id.orderDetailsContent);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            navController = Navigation.findNavController(view);
        } catch (IllegalStateException e) {
            Log.e(TAG, "NavController not found for this view.", e);
        }
        setupToolbar();
        if (orderId != null && !orderId.isEmpty()) {
            fetchOrderDetails();
        } else {
            showErrorAndGoBack(getString(R.string.error_invalid_order_id_display));
        }
    }

    private void setupToolbar() {
        if (tvOrderDetailsTitle != null) {
            tvOrderDetailsTitle.setText(getString(R.string.title_order_details));
        }
    }

    private void fetchOrderDetails() {
        if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.VISIBLE);
        if (contentLayout != null) contentLayout.setVisibility(View.GONE);

        db.collection("orders").document(orderId).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) return;

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            currentOrder = document.toObject(Order.class);
                            if (currentOrder != null) {
                                Log.d(TAG, "Order Fetched: " + currentOrder.getDocumentId() + ", ProductId: " + currentOrder.getProductId() + ", Status: " + currentOrder.getStatus());
                                displayOrderBaseDetails(currentOrder);
                                setupStatusSection(currentOrder);

                                if (currentOrder.getProductId() != null && !currentOrder.getProductId().isEmpty()) {
                                    fetchProductDetails(currentOrder.getProductId());
                                } else {
                                    Log.w(TAG, "Order does not have a productId.");
                                    showProductDetailsAsNotAvailable();
                                    if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                                    if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                                showErrorAndGoBack(getString(R.string.error_deserializing_order));
                            }
                        } else {
                            if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                            showErrorAndGoBack(getString(R.string.error_order_not_found));
                        }
                    } else {
                        if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                        String errorMsg = getString(R.string.error_fetching_order_details);
                        if (task.getException() != null) errorMsg += ": " + task.getException().getMessage();
                        showErrorAndGoBack(errorMsg);
                    }
                });
    }

    private void fetchProductDetails(String productIdToFetch) {
        Log.d(TAG, "Fetching product details for productId: " + productIdToFetch);
        db.collection("products").document(productIdToFetch).get()
                .addOnCompleteListener(productTask -> {
                    if (!isAdded() || getContext() == null) return;
                    if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                    if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE);

                    if (productTask.isSuccessful()) {
                        DocumentSnapshot productDocument = productTask.getResult();
                        if (productDocument != null && productDocument.exists()) {
                            Product product = productDocument.toObject(Product.class);
                            if (product != null) {
                                displayProductSpecificDetails(product);
                            } else {
                                showProductDetailsAsNotAvailable();
                            }
                        } else {
                            showProductDetailsAsNotAvailable();
                        }
                    } else {
                        showProductDetailsAsNotAvailable();
                        Toast.makeText(getContext(), getString(R.string.error_fetching_product_details), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayOrderBaseDetails(Order order) {
        if (order == null) return;
        String naText = getString(R.string.not_available_short);

        if (tvOrderIdValue != null) tvOrderIdValue.setText(order.getDocumentId() != null ? order.getDocumentId() : naText);
        if (tvOrderDateValue != null) {
            if (order.getDate() != null) {
                try { tvOrderDateValue.setText(dateFormat.format(order.getDate().toDate())); }
                catch (Exception e) { tvOrderDateValue.setText(naText); }
            } else { tvOrderDateValue.setText(naText); }
        }
        if (tvSellerIdValue != null) tvSellerIdValue.setText(order.getSellerId() != null ? order.getSellerId() : naText);
        if (tvUserIdValue != null) tvUserIdValue.setText(order.getUserId() != null ? order.getUserId() : naText);

        if (tvProductIdFromOrder != null) {
            tvProductIdFromOrder.setText(order.getProductId() != null && !order.getProductId().isEmpty() ?
                    order.getProductId() : naText);
        }
    }

    private void setupStatusSection(Order order) {
        Integer status = order.getStatus();

        if (status == null) {
            tvOrderStatus.setText(getString(R.string.status_not_available));
            btnChangeStatus.setVisibility(View.GONE);
        } else {
            tvOrderStatus.setText(getStatusString(status));
            btnChangeStatus.setVisibility(View.VISIBLE);
            btnChangeStatus.setOnClickListener(v -> showStatusSelectionDialog());
        }
        setStatusTextColor(status);
    }

    private void showStatusSelectionDialog() {
        if (getContext() == null) return;
        String[] statusOptions = getResources().getStringArray(R.array.order_status_options);

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.select_new_status_title))
                .setItems(statusOptions, (dialog, which) -> {
                    if (currentOrder != null && currentOrder.getStatus() != null && which != currentOrder.getStatus()) {
                        showConfirmationDialog(which);
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void showConfirmationDialog(final int newStatusIndex) {
        if (getContext() == null) return;
        String[] statusOptions = getResources().getStringArray(R.array.order_status_options);
        String newStatusText = statusOptions[newStatusIndex];

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_status_change_title))
                .setMessage(getString(R.string.confirm_status_change_message, newStatusText))
                .setPositiveButton(getString(R.string.confirm_button), (dialog, which) -> updateOrderStatusInFirestore(newStatusIndex))
                .setNegativeButton(getString(R.string.cancel_button), null)
                .show();
    }

    private void updateOrderStatusInFirestore(final int newStatus) {
        if (orderId == null || orderId.isEmpty()) return;
        progressBarOrderDetails.setVisibility(View.VISIBLE);
        db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() == null || !isAdded()) return;
                    progressBarOrderDetails.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.status_update_success), Toast.LENGTH_SHORT).show();
                    currentOrder.setStatus(newStatus);
                    tvOrderStatus.setText(getStatusString(newStatus));
                    setStatusTextColor(newStatus);
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null || !isAdded()) return;
                    progressBarOrderDetails.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.status_update_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String getStatusString(Integer status) {
        if (getContext() == null || status == null) return getString(R.string.status_not_available);
        String[] statusOptions = getResources().getStringArray(R.array.order_status_options);
        if (status >= 0 && status < statusOptions.length) {
            return statusOptions[status];
        }
        return getString(R.string.status_not_available);
    }

    private void setStatusTextColor(Integer status) {
        if (getContext() == null || tvOrderStatus == null) return;

        int colorResId;
        if (status != null) {
            switch (status) {
                case 1: // On the Way
                    colorResId = R.color.orange;
                    break;
                case 2: // Delivered
                    colorResId = R.color.leaf_green;
                    break;
                case 0: // In Warehouse
                default:
                    colorResId = R.color.text_secondary_on_background;
                    break;
            }
        } else {
            colorResId = R.color.text_secondary_on_background;
        }
        tvOrderStatus.setTextColor(ContextCompat.getColor(getContext(), colorResId));
    }


    private void displayProductSpecificDetails(Product product) {
        String naText = getString(R.string.not_available_short);
        if (tvLabelProductName != null) tvLabelProductName.setVisibility(View.VISIBLE);
        if (tvProductName != null) {
            tvProductName.setText(product.getName() != null ? product.getName() : naText);
            tvProductName.setVisibility(View.VISIBLE);
        }
        if (tvLabelProductPrice != null) tvLabelProductPrice.setVisibility(View.VISIBLE);
        if (tvProductPrice != null) {
            tvProductPrice.setText(currencyFormat.format(product.getPrice()));
            tvProductPrice.setVisibility(View.VISIBLE);
        }
    }

    private void showProductDetailsAsNotAvailable() {
        String naText = getString(R.string.not_available_short);
        if (tvLabelProductName != null) tvLabelProductName.setVisibility(View.VISIBLE);
        if (tvProductName != null) {
            tvProductName.setText(naText);
            tvProductName.setVisibility(View.VISIBLE);
        }
        if (tvLabelProductPrice != null) tvLabelProductPrice.setVisibility(View.VISIBLE);
        if (tvProductPrice != null) {
            tvProductPrice.setText(naText);
            tvProductPrice.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorAndGoBack(String message) {
        if (getContext() == null || !isAdded()) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        navigateBack();
    }

    private void navigateBack() {
        if (navController != null && navController.getCurrentDestination() != null &&
                navController.getGraph().findNode(navController.getCurrentDestination().getId()) != null) {
            navController.popBackStack();
        } else if (getActivity() != null && !getActivity().isFinishing()) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Log.w(TAG, "NavigateBack: No NavController and no parent backstack.");
            }
        }
    }
}