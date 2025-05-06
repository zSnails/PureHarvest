package cr.ac.itcr.zsnails.pureharvest.ui.orders; // Correct package

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
// import androidx.recyclerview.widget.GridLayoutManager; // Ya no se usa GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager; // ****** IMPORTAR ESTE ******
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.R;
// Asegúrate que la importación de tu modelo Order es correcta
// import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

public class companyOrderListFragment extends Fragment implements CompanyOrderAdapter.OnOrderClickListener {

    private static final String TAG = "CompanyOrderListFrag";
    private final String TARGET_SELLER_ID = "1";

    private RecyclerView recyclerViewOrders;
    private CompanyOrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;

    private ProgressBar progressBarOrders;
    private TextView textViewEmptyOrdersList;
    private ImageButton backButtonOrdersList;
    private TextView titleTextViewOrders;

    private NavController navController;


    public companyOrderListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_company_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            navController = Navigation.findNavController(view);
        } catch (IllegalStateException e) {
            Log.e(TAG, "NavController not found for this view.", e);
        }

        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        progressBarOrders = view.findViewById(R.id.progressBarOrders);
        textViewEmptyOrdersList = view.findViewById(R.id.textViewEmptyOrdersList);
        backButtonOrdersList = view.findViewById(R.id.backButtonOrdersList);
        titleTextViewOrders = view.findViewById(R.id.titleTextViewOrders);

        orderAdapter = new CompanyOrderAdapter(requireContext(), orderList, this);

        setupUI();
        fetchOrders();
    }

    private void setupUI() {
        if (titleTextViewOrders != null) {
            titleTextViewOrders.setText(getString(R.string.title_my_orders));
        }

        if (backButtonOrdersList != null) {
            backButtonOrdersList.setContentDescription(getString(R.string.content_description_back_button));
            backButtonOrdersList.setOnClickListener(v -> {
                if (navController != null && navController.getCurrentDestination() != null &&
                        navController.getGraph().findNode(navController.getCurrentDestination().getId()) != null) {
                    navController.popBackStack();
                } else {
                    Log.w(TAG, "Cannot popBackStack, NavController issue or not on expected destination.");
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        if (recyclerViewOrders != null) {
            // ****** CAMBIO PRINCIPAL AQUÍ ******
            // Cambiado de GridLayoutManager a LinearLayoutManager
            recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
            // recyclerViewOrders.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Línea anterior
            recyclerViewOrders.setAdapter(orderAdapter);
            recyclerViewOrders.setHasFixedSize(true); // Bueno si los ítems no cambian de tamaño
        } else {
            Log.e(TAG, "recyclerViewOrders is null in setupUI");
        }

        if (textViewEmptyOrdersList != null) {
            textViewEmptyOrdersList.setText(getString(R.string.no_orders_yet_company));
        }
    }

    private void fetchOrders() {
        showLoading(true);
        if (textViewEmptyOrdersList != null) textViewEmptyOrdersList.setVisibility(View.GONE);
        if (recyclerViewOrders != null) recyclerViewOrders.setVisibility(View.GONE);

        db.collection("orders")
                .whereEqualTo("sellerId", TARGET_SELLER_ID)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded()) {
                        return;
                    }
                    showLoading(false);
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            List<Order> fetchedOrders = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Order order = document.toObject(Order.class);
                                    order.setDocumentId(document.getId());
                                    fetchedOrders.add(order);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to Order: ID=" + document.getId(), e);
                                }
                            }
                            orderAdapter.updateOrders(fetchedOrders);
                            if (fetchedOrders.isEmpty()) {
                                if (textViewEmptyOrdersList != null) {
                                    textViewEmptyOrdersList.setText(getString(R.string.no_orders_yet_company));
                                    textViewEmptyOrdersList.setVisibility(View.VISIBLE);
                                }
                                if (recyclerViewOrders != null) recyclerViewOrders.setVisibility(View.GONE);
                            } else {
                                if (textViewEmptyOrdersList != null) textViewEmptyOrdersList.setVisibility(View.GONE);
                                if (recyclerViewOrders != null) recyclerViewOrders.setVisibility(View.VISIBLE);
                            }
                        } else {
                            handleFetchError("Error: Resultado nulo al cargar órdenes.");
                        }
                    } else {
                        String errorMessage = getString(R.string.error_loading_orders);
                        if (task.getException() != null) {
                            errorMessage += "\nDetalle: " + task.getException().getMessage();
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                        handleFetchError(errorMessage);
                    }
                });
    }

    private void handleFetchError(String message) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        if (textViewEmptyOrdersList != null) {
            textViewEmptyOrdersList.setText(message);
            textViewEmptyOrdersList.setVisibility(View.VISIBLE);
        }
        if (recyclerViewOrders != null) recyclerViewOrders.setVisibility(View.GONE);
    }

    private void showLoading(boolean isLoading) {
        if (progressBarOrders != null) {
            progressBarOrders.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        if (getContext() == null) return;
        Log.d(TAG, "Order clicked: DocID=" + order.getDocumentId() + ", UserID: " + order.getUserId());
        Toast.makeText(getContext(), "Pedido clickeado: " + order.getDocumentId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetailsClick(Order order) {
        if (getContext() == null || !isAdded()) { // Añadir chequeo !isAdded()
            Log.w(TAG, "Context is null or fragment not added in onViewDetailsClick");
            return;
        }

        String orderId = order.getDocumentId();

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.error_invalid_order_id_details), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Order ID is null or empty, cannot navigate to details.");
            return;
        }

        Log.d(TAG, "Navigating to details for order: " + orderId);
        // Quitar el Toast anterior que era solo para debugging si quieres
        // Toast.makeText(getContext(), String.format(getString(R.string.view_order_details_for), order.getDocumentId()), Toast.LENGTH_SHORT).show();

        if (navController != null) {
            // Crear un Bundle para pasar los argumentos
            Bundle bundle = new Bundle();
            // La clave "order_id" DEBE COINCIDIR con el nombre del <argument>
            // definido en tu grafo de navegación para OrderDetailsFragment.
            bundle.putString("order_id", orderId);

            try {
                // Navegar usando la acción definida en el grafo de navegación
                // y pasar el bundle con los argumentos.
                // Asegúrate de que R.id.action_companyOrderListFragment_to_orderDetailsFragment
                // es el ID correcto de la acción en tu nav_graph.xml
                navController.navigate(R.id.action_companyOrderListFragment_to_orderDetailsFragment, bundle);
            } catch (IllegalArgumentException e) {
                // Esto puede suceder si el ID de la acción es incorrecto o el destino no se encuentra.
                Log.e(TAG, "Navigation action/destination not found or other navigation error.", e);
                Toast.makeText(getContext(), getString(R.string.error_navigation_details), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "NavController is null in onViewDetailsClick. Cannot navigate.");
            Toast.makeText(getContext(), getString(R.string.error_navigation_controller_null), Toast.LENGTH_SHORT).show();
        }
    }}