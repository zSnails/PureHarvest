package cr.ac.itcr.zsnails.pureharvest.ui.orders; // Correct package

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
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
// import com.google.firebase.firestore.Query; // Ya no es necesario para Query.Direction
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
// Si vas a ordenar en el cliente, necesitarás estas:
// import java.util.Collections;
// import java.util.Comparator;


import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

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

        navController = Navigation.findNavController(view);

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
            backButtonOrdersList.setOnClickListener(v -> navController.popBackStack());
        }

        recyclerViewOrders.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewOrders.setAdapter(orderAdapter);
        recyclerViewOrders.setHasFixedSize(true);

        if (textViewEmptyOrdersList != null) {
            textViewEmptyOrdersList.setText(getString(R.string.no_orders_yet_company));
        }
    }

    private void fetchOrders() {
        showLoading(true);
        textViewEmptyOrdersList.setVisibility(View.GONE);
        recyclerViewOrders.setVisibility(View.GONE);

        db.collection("orders")
                .whereEqualTo("sellerId", TARGET_SELLER_ID)
                // .orderBy("date", Query.Direction.DESCENDING) // LÍNEA ELIMINADA/COMENTADA
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            List<Order> fetchedOrders = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Order order = document.toObject(Order.class);
                                    order.setDocumentId(document.getId());
                                    fetchedOrders.add(order);
                                    Log.d(TAG, "Fetched order: " + document.getId() + " => " + document.getData());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to Order object: ID=" + document.getId() + ", Data=" + document.getData(), e);
                                }
                            }

                            // Si decides ordenar en el cliente en el futuro, aquí es donde lo harías.
                            // Ejemplo (necesitarías descomentar los imports de Collections y Comparator):
                            /*
                            if (!fetchedOrders.isEmpty() && fetchedOrders.get(0).getDate() != null) {
                                Collections.sort(fetchedOrders, new Comparator<Order>() {
                                    @Override
                                    public int compare(Order o1, Order o2) {
                                        if (o1.getDate() == null && o2.getDate() == null) return 0;
                                        if (o1.getDate() == null) return 1;
                                        if (o2.getDate() == null) return -1;
                                        return o2.getDate().compareTo(o1.getDate()); // Descendente
                                    }
                                });
                            }
                            */

                            orderAdapter.updateOrders(fetchedOrders);

                            if (fetchedOrders.isEmpty()) {
                                Log.d(TAG, "No orders found for sellerId: " + TARGET_SELLER_ID);
                                textViewEmptyOrdersList.setText(getString(R.string.no_orders_yet_company));
                                textViewEmptyOrdersList.setVisibility(View.VISIBLE);
                                recyclerViewOrders.setVisibility(View.GONE);
                            } else {
                                textViewEmptyOrdersList.setVisibility(View.GONE);
                                recyclerViewOrders.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.w(TAG, "Task successful but result is null.");
                            Toast.makeText(getContext(), "Error: Resultado nulo al cargar órdenes.", Toast.LENGTH_SHORT).show();
                            textViewEmptyOrdersList.setText("Error: Resultado nulo.");
                            textViewEmptyOrdersList.setVisibility(View.VISIBLE);
                            recyclerViewOrders.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        String errorMessage = getString(R.string.error_loading_orders);
                        if (task.getException() != null) {
                            errorMessage += "\nDetalle: " + task.getException().getMessage();
                            task.getException().printStackTrace();
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        if (textViewEmptyOrdersList != null) {
                            textViewEmptyOrdersList.setText(errorMessage);
                            textViewEmptyOrdersList.setVisibility(View.VISIBLE);
                        }
                        recyclerViewOrders.setVisibility(View.GONE);
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (progressBarOrders != null) {
            progressBarOrders.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Log.d(TAG, "Order clicked: " + order.getDocumentId());
        Toast.makeText(getContext(), "Order item clicked: " + order.getProductId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetailsClick(Order order) {
        Log.d(TAG, "View details for order: " + order.getDocumentId());
        Toast.makeText(getContext(), String.format(getString(R.string.view_order_details_for), order.getDocumentId()), Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación a la pantalla de detalles de la orden
    }
}