package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order; // Ensure this import is correct

public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String TAG = "OrderDetailsFragment";

    private String orderId;
    private FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private TextView tvOrderId, tvOrderDate, tvSellerId, tvUserId, tvProductIds;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;


    public OrderDetailsFragment() {
        // Required empty public constructor
    }

    public static OrderDetailsFragment newInstance(String orderId) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
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

        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        tvSellerId = view.findViewById(R.id.tvSellerId);
        tvUserId = view.findViewById(R.id.tvUserId);
        tvProductIds = view.findViewById(R.id.tvProductIds);
        progressBar = view.findViewById(R.id.progressBarOrderDetails);
        contentLayout = view.findViewById(R.id.orderDetailsContent);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (orderId != null && !orderId.isEmpty()) {
            fetchOrderDetails();
        } else {
            Toast.makeText(getContext(), "ID de pedido no válido.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Order ID is null or empty");
            // Optionally navigate back or show an error state
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void fetchOrderDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        db.collection("orders").document(orderId).get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Order order = document.toObject(Order.class);
                            if (order != null) {
                                // The @DocumentId annotation in Order model should set order.documentId
                                // If not, or if you prefer: order.setDocumentId(document.getId());
                                displayOrderDetails(order);
                                contentLayout.setVisibility(View.VISIBLE);
                            } else {
                                showError("No se pudieron deserializar los datos del pedido.");
                            }
                        } else {
                            showError("Pedido no encontrado.");
                            Log.w(TAG, "No such document with ID: " + orderId);
                        }
                    } else {
                        showError("Error al obtener el pedido: " + task.getException().getMessage());
                        Log.e(TAG, "Error getting document: ", task.getException());
                    }
                });
    }

    private void displayOrderDetails(Order order) {
        tvOrderId.setText(order.getDocumentId() != null ? order.getDocumentId() : "N/A");

        if (order.getDate() != null) {
            tvOrderDate.setText(dateFormat.format(order.getDate().toDate()));
        } else {
            tvOrderDate.setText("N/A");
        }

        tvSellerId.setText(order.getSellerId() != null ? order.getSellerId() : "N/A");
        tvUserId.setText(order.getUserId() != null ? order.getUserId() : "N/A");

        if (order.getProductIDs() != null && !order.getProductIDs().isEmpty()) {
            tvProductIds.setText(TextUtils.join(", ", order.getProductIDs()));
        } else {
            tvProductIds.setText("N/A");
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        // Consider showing an error message in the UI instead of just a toast
        // and potentially navigate back.
        if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
    }
}