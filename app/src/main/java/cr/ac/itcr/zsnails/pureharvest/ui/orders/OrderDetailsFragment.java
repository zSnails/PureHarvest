package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.R;


public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String TAG = "OrderDetailsFragment";

    private String orderId;
    private FirebaseFirestore db;
    private Order currentOrder;
    private User currentUserDetails;

    private TextView tvOrderIdValue, tvOrderDateValue;
    private TextView tvOrderStatus, tvLabelOrderStatus;
    private Button btnChangeStatus;

    private TextView tvLabelUserFullName, tvUserFullName, tvLabelUserEmail, tvUserEmail, tvLabelUserPhone, tvUserPhone;
    private Button btnContactBuyer;

    private TextView tvOrderDetailsTitle;
    private ProgressBar progressBarOrderDetails;
    private LinearLayout contentLayout;

    private NavController navController;

    private RecyclerView recyclerViewOrderProductsWithQuantity;
    private PurchasedProductOrderAdapter purchasedProductOrderAdapter;
    private List<PurchasedProductOrder> productDisplayListWithQuantity;

    private LinearLayout layoutOrderTotal;
    private TextView tvOrderTotalValue;


    public OrderDetailsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
        db = FirebaseFirestore.getInstance();
        productDisplayListWithQuantity = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        tvOrderDetailsTitle = view.findViewById(R.id.tvOrderDetailsTitle);

        tvOrderIdValue = view.findViewById(R.id.tvOrderId);
        tvOrderDateValue = view.findViewById(R.id.tvOrderDate);

        tvLabelOrderStatus = view.findViewById(R.id.tvLabelOrderStatus);
        tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
        btnChangeStatus = view.findViewById(R.id.btnChangeStatus);

        tvLabelUserFullName = view.findViewById(R.id.tvLabelUserFullName);
        tvUserFullName = view.findViewById(R.id.tvUserFullName);
        tvLabelUserEmail = view.findViewById(R.id.tvLabelUserEmail);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvLabelUserPhone = view.findViewById(R.id.tvLabelUserPhone);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        btnContactBuyer = view.findViewById(R.id.btnContactBuyer);

        progressBarOrderDetails = view.findViewById(R.id.progressBarOrderDetails);
        contentLayout = view.findViewById(R.id.orderDetailsContent);

        layoutOrderTotal = view.findViewById(R.id.layoutOrderTotal);
        tvOrderTotalValue = view.findViewById(R.id.tvOrderTotalValue);

        recyclerViewOrderProductsWithQuantity = view.findViewById(R.id.recyclerViewOrderProductsWithQuantity);
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        purchasedProductOrderAdapter = new PurchasedProductOrderAdapter(productDisplayListWithQuantity);
        recyclerViewOrderProductsWithQuantity.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrderProductsWithQuantity.setAdapter(purchasedProductOrderAdapter);
        recyclerViewOrderProductsWithQuantity.setVisibility(View.GONE);
        if (layoutOrderTotal != null) {
            layoutOrderTotal.setVisibility(View.GONE);
        }
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

        btnContactBuyer.setOnClickListener(v -> showContactOptionsDialog());
    }

    private void setupToolbar() {
        if (tvOrderDetailsTitle != null) {
            tvOrderDetailsTitle.setText(getString(R.string.title_order_details));
        }
    }

    private void fetchOrderDetails() {
        if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.VISIBLE);
        if (contentLayout != null) contentLayout.setVisibility(View.GONE);
        if (recyclerViewOrderProductsWithQuantity != null) recyclerViewOrderProductsWithQuantity.setVisibility(View.GONE);
        if (layoutOrderTotal != null) layoutOrderTotal.setVisibility(View.GONE);


        db.collection("orders").document(orderId).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) return;

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            currentOrder = document.toObject(Order.class);
                            if (currentOrder != null) {
                                displayOrderBaseDetails(currentOrder);
                                setupStatusSection(currentOrder);

                                if (currentOrder.getUserId() != null && !currentOrder.getUserId().isEmpty()) {
                                    fetchUserDetails(currentOrder.getUserId());
                                } else {
                                    displayUserDetails(null);
                                }

                                if (currentOrder.getProductsBought() != null && !currentOrder.getProductsBought().isEmpty()) {
                                    fetchProductsForDisplay(currentOrder.getProductsBought());
                                } else {
                                    Log.w(TAG, "Order does not have any productsBought.");
                                    updateProductDisplay(new ArrayList<>());
                                    displayOrderTotal(0.0);
                                    finalizeFetch();
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

    private void fetchUserDetails(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(userTask -> {
                    if (!isAdded() || getContext() == null) return;

                    if (userTask.isSuccessful()) {
                        DocumentSnapshot userDocument = userTask.getResult();
                        if (userDocument != null && userDocument.exists()) {
                            currentUserDetails = userDocument.toObject(User.class);
                            if (currentUserDetails != null) {
                                displayUserDetails(currentUserDetails);
                                btnContactBuyer.setVisibility(View.VISIBLE);
                            } else {
                                displayUserDetails(null);
                                btnContactBuyer.setVisibility(View.GONE);
                            }
                        } else {
                            displayUserDetails(null);
                            btnContactBuyer.setVisibility(View.GONE);
                        }
                    } else {
                        displayUserDetails(null);
                        btnContactBuyer.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.error_fetching_user_details_toast), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchProductsForDisplay(List<Order.OrderItem> productRefs) {
        if (productRefs == null || productRefs.isEmpty()) {
            updateProductDisplay(new ArrayList<>());
            displayOrderTotal(0.0);
            finalizeFetch();
            return;
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        List<Integer> quantitiesAssociatedWithTasks = new ArrayList<>();

        for (Order.OrderItem productRef : productRefs) {
            // Object idObject = productRef.id;
            // String productIdString = (idObject instanceof String) ? (String) idObject : null;
            String productIdString = productRef.id;

            //Object amountObject = productRef.amount;
            int currentProductQuantity = productRef.amount;

            if (currentProductQuantity <= 0) {
                currentProductQuantity = 1;
            }

            //if (productIdString != null && !productIdString.isEmpty()) {
            tasks.add(db.collection("products").document(productIdString).get());
            quantitiesAssociatedWithTasks.add(currentProductQuantity);
            //} else if (idObject != null) {
            //    Log.w(TAG, "Product ID in productsBought is not a String: " + idObject.getClass().getName());
            //} else {
            //    Log.w(TAG, "Product ID is null in productsBought.");
            //}
        }

        if (tasks.isEmpty()) {
            updateProductDisplay(new ArrayList<>());
            displayOrderTotal(0.0);
            finalizeFetch();
            return;
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            if (!isAdded() || getContext() == null) return;
            List<PurchasedProductOrder> fetchedProducts = new ArrayList<>();
            double orderTotal = 0.0;

            for (int i = 0; i < results.size(); i++) {
                Object result = results.get(i);
                DocumentSnapshot productDocument = (DocumentSnapshot) result;

                int productQuantityForThisItem = 1;
                if (i < quantitiesAssociatedWithTasks.size()) {
                    productQuantityForThisItem = quantitiesAssociatedWithTasks.get(i);
                } else {
                    Log.w(TAG, "Mismatch between fetched products and quantities list size. Defaulting quantity for product ID: " + productDocument.getId());
                }

                if (productDocument.exists()) {
                    String name = productDocument.getString("name");
                    Double priceDouble = productDocument.getDouble("price");
                    double price = (priceDouble != null) ? priceDouble : 0.0;
                    List<String> imageUrls = (List<String>) productDocument.get("imageUrls");
                    String imageUrl = (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;

                    orderTotal += (price * productQuantityForThisItem);

                    PurchasedProductOrder product = new PurchasedProductOrder(
                            productDocument.getId(),
                            name != null ? name : getString(R.string.not_available_short),
                            price,
                            imageUrl,
                            productQuantityForThisItem
                    );
                    fetchedProducts.add(product);
                }
            }
            updateProductDisplay(fetchedProducts);
            displayOrderTotal(orderTotal);
            finalizeFetch();
        }).addOnFailureListener(e -> {
            if (!isAdded() || getContext() == null) return;
            Log.e(TAG, "Error fetching some product details", e);
            Toast.makeText(getContext(), getString(R.string.error_fetching_some_product_details), Toast.LENGTH_SHORT).show();
            updateProductDisplay(new ArrayList<>());
            displayOrderTotal(0.0);
            finalizeFetch();
        });
    }

    private void displayOrderTotal(double total) {
        if (tvOrderTotalValue != null && layoutOrderTotal != null && getContext() != null) {
            tvOrderTotalValue.setText(String.format(Locale.US, getString(R.string.order_total_price_format), total));
            layoutOrderTotal.setVisibility(View.VISIBLE);
        }
    }


    private void finalizeFetch() {
        if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
        if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE);
        if (recyclerViewOrderProductsWithQuantity != null && productDisplayListWithQuantity != null && !productDisplayListWithQuantity.isEmpty()) {
            recyclerViewOrderProductsWithQuantity.setVisibility(View.VISIBLE);
        } else if (recyclerViewOrderProductsWithQuantity != null) {
            recyclerViewOrderProductsWithQuantity.setVisibility(View.GONE);
        }
    }


    private void updateProductDisplay(List<PurchasedProductOrder> products) {
        if (purchasedProductOrderAdapter != null) {
            productDisplayListWithQuantity.clear();
            productDisplayListWithQuantity.addAll(products);
            purchasedProductOrderAdapter.notifyDataSetChanged();
        }
    }


    private void displayOrderBaseDetails(Order order) {
        if (order == null) return;
        String naText = getString(R.string.not_available_short);
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());


        if (tvOrderIdValue != null) tvOrderIdValue.setText(order.getDocumentId() != null ? order.getDocumentId() : naText);
        if (tvOrderDateValue != null) {
            if (order.getDate() != null) {
                try { tvOrderDateValue.setText(displayDateFormat.format(order.getDate().toDate())); }
                catch (Exception e) { tvOrderDateValue.setText(naText); }
            } else { tvOrderDateValue.setText(naText); }
        }
    }

    private void displayUserDetails(User user) {
        String naText = getString(R.string.not_available_short);

        if (tvLabelUserFullName != null) tvLabelUserFullName.setVisibility(View.VISIBLE);
        if (tvLabelUserEmail != null) tvLabelUserEmail.setVisibility(View.VISIBLE);
        if (tvLabelUserPhone != null) tvLabelUserPhone.setVisibility(View.VISIBLE);

        if (tvUserFullName != null) {
            tvUserFullName.setText((user != null && user.getFullName() != null) ? user.getFullName() : naText);
            tvUserFullName.setVisibility(View.VISIBLE);
        }
        if (tvUserEmail != null) {
            tvUserEmail.setText((user != null && user.getEmail() != null) ? user.getEmail() : naText);
            tvUserEmail.setVisibility(View.VISIBLE);
        }
        if (tvUserPhone != null) {
            tvUserPhone.setText((user != null && user.getPhone() != null) ? user.getPhone() : naText);
            tvUserPhone.setVisibility(View.VISIBLE);
        }

        if (user != null && (user.getPhone() != null || user.getEmail() != null)) {
            btnContactBuyer.setVisibility(View.VISIBLE);
        } else {
            btnContactBuyer.setVisibility(View.GONE);
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
        if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.VISIBLE);
        db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() == null || !isAdded()) return;
                    if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.status_update_success), Toast.LENGTH_SHORT).show();
                    currentOrder.setStatus(newStatus);
                    tvOrderStatus.setText(getStatusString(newStatus));
                    setStatusTextColor(newStatus);
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null || !isAdded()) return;
                    if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
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
                case 1:
                    colorResId = R.color.orange;
                    break;
                case 2:
                    colorResId = R.color.leaf_green;
                    break;
                case 0:
                default:
                    colorResId = R.color.text_secondary_on_background;
                    break;
            }
        } else {
            colorResId = R.color.text_secondary_on_background;
        }
        tvOrderStatus.setTextColor(ContextCompat.getColor(getContext(), colorResId));
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

    private void showContactOptionsDialog() {
        if (getContext() == null || currentUserDetails == null) return;

        final List<String> optionsList = new ArrayList<>();
        final String phone = currentUserDetails.getPhone();
        final String email = currentUserDetails.getEmail();

        if (phone != null && !phone.trim().isEmpty()) {
            optionsList.add(getString(R.string.contact_option_call));
            optionsList.add(getString(R.string.contact_option_sms));
            optionsList.add(getString(R.string.contact_option_whatsapp));
        }
        if (email != null && !email.trim().isEmpty()) {
            optionsList.add(getString(R.string.contact_option_email));
        }

        if (optionsList.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_contact_info_available), Toast.LENGTH_SHORT).show();
            return;
        }

        final CharSequence[] options = optionsList.toArray(new CharSequence[0]);

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.contact_options_title))
                .setItems(options, (dialog, which) -> {
                    String selectedOption = options[which].toString();
                    if (selectedOption.equals(getString(R.string.contact_option_call))) {
                        initiateCall(phone);
                    } else if (selectedOption.equals(getString(R.string.contact_option_sms))) {
                        sendSms(phone);
                    } else if (selectedOption.equals(getString(R.string.contact_option_whatsapp))) {
                        sendWhatsAppMessage(phone);
                    } else if (selectedOption.equals(getString(R.string.contact_option_email))) {
                        sendEmail(email);
                    }
                })
                .show();
    }

    private void initiateCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.error_no_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("[^0-9+]", "")));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.error_app_not_found_for_action), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.error_no_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber.replaceAll("[^0-9+]", "")));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.error_app_not_found_for_action), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendWhatsAppMessage(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.error_no_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }
        String cleanedPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + cleanedPhoneNumber));

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.error_whatsapp_not_installed), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.error_no_email_address), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.fromParts("mailto", emailAddress, null));
        try {
            startActivity(Intent.createChooser(intent, "Send email..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.error_app_not_found_for_action), Toast.LENGTH_SHORT).show();
        }
    }
}