// File: cr.ac.itcr.zsnails.pureharvest.ui.orders.OrderDetailsFragment.java
package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import android.os.Bundle;
// import android.text.TextUtils; // No se usa directamente aquí
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

import java.text.NumberFormat; // Para formatear el precio
import java.text.SimpleDateFormat;
import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Product; // IMPORTAR EL NUEVO MODELO PRODUCT

public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String TAG = "OrderDetailsFragment";

    private String orderId;
    private FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CR")); // Formato de moneda para Costa Rica (Colones)

    // TextViews para la orden
    private TextView tvOrderIdValue, tvOrderDateValue, tvSellerIdValue, tvUserIdValue, tvProductIdFromOrder;
    // TextViews para el producto (nuevos)
    private TextView tvLabelProductName, tvProductName, tvLabelProductPrice, tvProductPrice;

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
        }
        db = FirebaseFirestore.getInstance();
        // currencyFormat.setCurrency(Currency.getInstance("USD")); // Si quieres USD u otra moneda
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        tvOrderDetailsTitle = view.findViewById(R.id.tvOrderDetailsTitle);
        // backButtonOrderDetails = view.findViewById(R.id.your_back_button_id);

        // TextViews de la Orden
        tvOrderIdValue = view.findViewById(R.id.tvOrderId);
        tvOrderDateValue = view.findViewById(R.id.tvOrderDate);
        tvSellerIdValue = view.findViewById(R.id.tvSellerId);
        tvUserIdValue = view.findViewById(R.id.tvUserId);
        tvProductIdFromOrder = view.findViewById(R.id.tvProductIds); // Este es el ID del producto EN la orden

        // TextViews del Producto (nuevos)
        tvLabelProductName = view.findViewById(R.id.tvLabelProductName);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvLabelProductPrice = view.findViewById(R.id.tvLabelProductPrice);
        tvProductPrice = view.findViewById(R.id.tvProductPrice);

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
        // if (backButtonOrderDetails != null) {
        //     backButtonOrderDetails.setOnClickListener(v -> navigateBack());
        // }
    }

    private void fetchOrderDetails() {
        if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.VISIBLE);
        if (contentLayout != null) contentLayout.setVisibility(View.GONE); // Ocultar contenido hasta que todo esté listo

        db.collection("orders").document(orderId).get()
                .addOnCompleteListener(task -> {
                    // No ocultar el progressBar aquí todavía si vamos a hacer otra consulta
                    if (!isAdded() || getContext() == null) return;

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Order order = document.toObject(Order.class);
                            if (order != null) {
                                Log.d(TAG, "Order Fetched: " + order.getDocumentId() + ", ProductId in Order: " + order.getProductId());
                                displayOrderBaseDetails(order); // Muestra detalles base de la orden

                                // Ahora, si la orden tiene un productId, busca ese producto
                                if (order.getProductId() != null && !order.getProductId().isEmpty()) {
                                    fetchProductDetails(order.getProductId());
                                } else {
                                    Log.w(TAG, "Order does not have a productId.");
                                    showProductDetailsAsNotAvailable();
                                    if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                                    if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE); // Mostrar contenido de la orden
                                }
                            } else {
                                Log.e(TAG, "Order object is NULL after document.toObject() for ID: " + orderId);
                                if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE);
                                showErrorAndGoBack(getString(R.string.error_deserializing_order));
                            }
                        } else {
                            Log.w(TAG, "No such order document with ID: " + orderId);
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
                    if (progressBarOrderDetails != null) progressBarOrderDetails.setVisibility(View.GONE); // Ahora sí ocultar
                    if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE); //

                    if (productTask.isSuccessful()) {
                        DocumentSnapshot productDocument = productTask.getResult();
                        if (productDocument != null && productDocument.exists()) {
                            Product product = productDocument.toObject(Product.class);
                            if (product != null) {
                                Log.d(TAG, "Product Fetched: " + product.getName() + ", Price: " + product.getPrice());
                                displayProductSpecificDetails(product);
                            } else {
                                Log.e(TAG, "Product object is NULL after document.toObject() for ID: " + productIdToFetch);
                                showProductDetailsAsNotAvailable();
                            }
                        } else {
                            Log.w(TAG, "No such product document with ID: " + productIdToFetch);
                            showProductDetailsAsNotAvailable();
                        }
                    } else {
                        Log.e(TAG, "Error fetching product details for ID: " + productIdToFetch, productTask.getException());
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

        // Mostrar el ID del producto que viene en la orden
        if (tvProductIdFromOrder != null) {
            tvProductIdFromOrder.setText(order.getProductId() != null && !order.getProductId().isEmpty() ?
                    order.getProductId() : naText);
        }
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
            // Formatear el precio. Ejemplo: ₡1,250.00
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