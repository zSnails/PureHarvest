// File: cr.ac.itcr.zsnails.pureharvest.ui.orders.OrderDetailsFragment.java
package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.os.Bundle;
// import android.text.TextUtils; // Ya no es necesario para TextUtils.join
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String TAG = "OrderDetailsFragment";

    private String orderId;
    private FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private TextView tvOrderId, tvOrderDate, tvSellerId, tvUserId, tvProductIdValue; // CAMBIO: tvProductIds a tvProductIdValue
    private TextView tvOrderDetailsTitle;
    private ImageButton backButtonOrderDetails;
    private ProgressBar progressBarOrderDetails;
    private LinearLayout contentLayout;

    private NavController navController;

    public OrderDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
            Log.d(TAG, "onCreate: Received orderId: " + orderId);
        } else {
            Log.w(TAG, "onCreate: Arguments bundle is null.");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        tvOrderDetailsTitle = view.findViewById(R.id.tvOrderDetailsTitle);
        // backButtonOrderDetails = view.findViewById(R.id.your_back_button_id); // Si tienes uno

        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        tvSellerId = view.findViewById(R.id.tvSellerId);
        tvUserId = view.findViewById(R.id.tvUserId);
        tvProductIdValue = view.findViewById(R.id.tvProductIds); // USAREMOS EL MISMO TEXTVIEW CON ID tvProductIds
        // PERO LO RENOMBRO EN JAVA PARA CLARIDAD (tvProductIdValue)

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
            Log.d(TAG, "onViewCreated: Fetching details for orderId: " + orderId);
            fetchOrderDetails();
        } else {
            Log.e(TAG, "onViewCreated: Order ID is null or empty. Cannot fetch details.");
            showError(getString(R.string.error_invalid_order_id_display));
            navigateBack();
        }
    }

    private void setupToolbar() {
        if (tvOrderDetailsTitle != null) {
            tvOrderDetailsTitle.setText(getString(R.string.title_order_details));
        }
        // Si tienes un botón de retroceso específico en tu layout:
        // if (backButtonOrderDetails != null) {
        //     backButtonOrderDetails.setOnClickListener(v -> navigateBack());
        // }
    }

    private void fetchOrderDetails() {
        if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.VISIBLE);
        if (contentLayout != null) contentLayout.setVisibility(View.GONE);

        db.collection("orders").document(orderId).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) {
                        Log.w(TAG, "Fragment not added or context is null in onComplete.");
                        return;
                    }
                    if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            Order order = document.toObject(Order.class);

                            if (order != null) {
                                Log.d(TAG, "Order Object Deserialized: documentId=" + order.getDocumentId());
                                Log.d(TAG, "Order Object Deserialized: userId=" + order.getUserId());
                                Log.d(TAG, "Order Object Deserialized: sellerId=" + order.getSellerId());
                                Log.d(TAG, "Order Object Deserialized: date=" + (order.getDate() != null ? order.getDate().toDate() : "null"));
                                // LOG PARA EL NUEVO CAMPO productId (String)
                                Log.d(TAG, "Order Object Deserialized: productId=" + order.getProductId());

                                displayOrderDetails(order);
                                if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE);
                            } else {
                                Log.e(TAG, "Order object is NULL after document.toObject() for ID: " + orderId + ". " +
                                        "Verifica el modelo Order.java y los nombres de campo en Firestore.");
                                showError(getString(R.string.error_deserializing_order));
                            }
                        } else {
                            Log.w(TAG, "No such document with ID: " + orderId);
                            showError(getString(R.string.error_order_not_found));
                        }
                    } else {
                        String errorMsg = getString(R.string.error_fetching_order_details);
                        if (task.getException() != null) {
                            errorMsg += ": " + task.getException().getMessage();
                            Log.e(TAG, "Error getting document: " + orderId, task.getException());
                        }
                        showError(errorMsg);
                    }
                });
    }

    private void displayOrderDetails(Order order) {
        if (order == null) {
            Log.e(TAG, "displayOrderDetails: Order object is null. Cannot display.");
            return;
        }
        String naText = getString(R.string.not_available_short);

        if (tvOrderId != null) tvOrderId.setText(order.getDocumentId() != null ? order.getDocumentId() : naText);

        if (tvOrderDate != null) {
            if (order.getDate() != null) {
                try {
                    tvOrderDate.setText(dateFormat.format(order.getDate().toDate()));
                } catch (Exception e) {
                    Log.e(TAG, "Error formatting date", e);
                    tvOrderDate.setText(naText);
                }
            } else {
                tvOrderDate.setText(naText);
            }
        }

        if (tvSellerId != null) tvSellerId.setText(order.getSellerId() != null ? order.getSellerId() : naText);
        if (tvUserId != null) tvUserId.setText(order.getUserId() != null ? order.getUserId() : naText);

        // CAMBIO: Lógica para mostrar el productId (String)
        if (tvProductIdValue != null) {
            if (order.getProductId() != null && !order.getProductId().isEmpty()) {
                tvProductIdValue.setText(order.getProductId());
                Log.d(TAG, "Displaying ProductID in UI: " + order.getProductId());
            } else {
                tvProductIdValue.setText(naText);
                Log.d(TAG, "ProductID in Order object is null or empty. Displaying N/A in UI.");
            }
        } else {
            Log.e(TAG, "tvProductIdValue (TextView for Product ID) is null! Check layout ID R.id.tvProductIds.");
        }
    }

    private void showError(String message) {
        if (getContext() == null || !isAdded()) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        if (contentLayout != null) contentLayout.setVisibility(View.GONE);
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