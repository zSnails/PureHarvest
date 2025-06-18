package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import static androidx.lifecycle.Lifecycle.State.RESUMED;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.LoginActivity;
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentShoppingCartBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.entities.CartDisplayItem;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter.Card;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter.ShoppingCartAdapter;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;
import dagger.hilt.android.AndroidEntryPoint;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

@AndroidEntryPoint
public final class ShoppingCartFragment extends Fragment
        implements MenuProvider,
        Card.AmountTapListener,
        UpdateItemAmountDialog.ItemAmountAcceptListener,
        Card.CouponApplyListener {

    @Inject
    public FirebaseAuth auth;
    private FragmentShoppingCartBinding binding;
    private ShoppingCartViewModel shoppingCart;
    private AlertDialog deletionDialog;
    private ShoppingCartAdapter adapter;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        this.shoppingCart = new ViewModelProvider(requireActivity()).get(ShoppingCartViewModel.class);
        this.binding = FragmentShoppingCartBinding.inflate(inflater, container, false);
        this.adapter = new ShoppingCartAdapter(this, this);
        this.shoppingCart.addItemOperationEventListener(adapter);
        this.binding.shoppingCartRecyclerView.setAdapter(adapter);
        this.binding.shoppingCartRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        this.binding.shoppingCartRecyclerView.addItemDecoration(
                new MarginItemDecoration(
                        (int) getResources().getDimension(R.dimen.list_item_margin)));

        deletionDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.shopping_cart_clear_confirmation_dialog_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes,
                        (dialogInterface, i) ->
                                shoppingCart.removeAllItems())
                .setNegativeButton(android.R.string.no, null)
                .create();
        shoppingCart.items.observe(getViewLifecycleOwner(), it -> {
            adapter.setItems(it);
            this.binding.shoppingCartCheckoutButton.setEnabled(!it.isEmpty());
        });
        this.binding.shoppingCartCheckoutButton.setOnClickListener(this::onCheckout);
        //this.binding.shoppingCartCheckoutButton.setText(getString(R.string.checkout_total, shoppingCart.subtotal.getValue()));
        shoppingCart.subtotal.observe(getViewLifecycleOwner(), total -> {
            Log.d("computing:subtotal", String.format("value of total: %f", total));
            this.binding.shoppingCartCheckoutButton.setText(getString(R.string.checkout_total, total));
        });
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), RESUMED);
        setupSwipeHandler();

        return this.binding.getRoot();
    }

    private void onCheckout(View view) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "You must be logged in to perform this action", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
            return;
        }
        // STATUS: 0 = BODEGA, 1 = TRANSITO, 2 = ENTREGADO
        List<Order.OrderItem> products = shoppingCart.items.getValue()
                .stream()
                .map(
                        p -> new Order.OrderItem(p.productId, p.amount))
                .collect(Collectors.toList());
        Order order = new Order(
                Timestamp.now(),
                user.getUid(),
                "NO APLICABLE",
                products,
                0);
        // TODO: poner un spinner de loaded order
        shoppingCart.createOrder(order, this::onOrderCreated);
    }

    public void onOrderCreated(Order order) {
        Toast.makeText(requireContext(), "The order has been created", Toast.LENGTH_SHORT).show();
        shoppingCart.removeAllItems();
        Bundle b = new Bundle();
        b.putSerializable("order", order);
        Navigation.findNavController(requireView()).navigate(R.id.action_navigation_shopping_cart_to_clientOrderDetailsFragment, b);
    }

    private void setupSwipeHandler() {
        ItemTouchHelper.SimpleCallback swipeHandler = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                long itemId = viewHolder.getItemId();
                shoppingCart.removeById(itemId);
            }
        };
        new ItemTouchHelper(swipeHandler).attachToRecyclerView(this.binding.shoppingCartRecyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shopping_cart_menu, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.shopping_cart_menu_clear_item) {
            if (!deletionDialog.isShowing()) deletionDialog.show();
            return true;
        } else if (menuItem.getItemId() == R.id.shopping_cart_past_orders) {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_shopping_cart_to_clientOrdersFragment2);
            return true;
        }
        return false;
    }

    @Override
    public void onAmountTap(Item item, int position) {
        var dialog = new UpdateItemAmountDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable("item", item);
        bundle.putInt("position", position);
        dialog.setArguments(bundle);
        dialog.setItemAmountAcceptListener(this);
        dialog.show(requireActivity().getSupportFragmentManager(), "ITEM_UPDATER");
    }

    @Override
    public void onAmountAccepted(CartDisplayItem item, int position, int amount) {
        if (amount < 1) return;
        item.setAmount(amount);
        shoppingCart.updateItem(item);
    }
    @Override
    public void onCouponApply(Item item, int position, String couponCode) {
        firestore.collection("coupons")
                .whereEqualTo("code", couponCode)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(requireContext(), "Cupón no encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = query.getDocuments().get(0);

                    // Validaciones
                    Long rawTs = doc.getLong("expirationTimestamp");
                    if (rawTs == null) {
                        Toast.makeText(requireContext(), "El cupón no tiene fecha de expiración válida", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    java.util.Date expirationDate = new java.util.Date(rawTs);
                    boolean isExpired = expirationDate.before(new java.util.Date());
                    Long maxUses = doc.getLong("maxUses");
                    Long uses = doc.getLong("uses");
                    List<String> applicableProducts = (List<String>) doc.get("applicableProductIds");

                    if (isExpired) {
                        Toast.makeText(requireContext(), "Cupón expirado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (uses != null && maxUses != null && uses >= maxUses) {
                        Toast.makeText(requireContext(), "Este cupón ya fue utilizado muchas veces", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d("CouponDebug", "item.getProductId(): " + item.getProductId());
                    Log.d("CouponDebug", "applicableProducts: " + applicableProducts);
                    if (applicableProducts != null && !applicableProducts.contains(item.getProductId())) {
                        Toast.makeText(requireContext(), "Este cupón no aplica a este producto", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Double discountPercentage = doc.getDouble("discountPercentage");
                    if (discountPercentage == null) {
                        Toast.makeText(requireContext(), "Cupón inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Aplicar descuento
                    double price = item.getPrice();
                    double discounted = price * (1 - discountPercentage / 100);
                    if (item instanceof CartDisplayItem) {
                        ((CartDisplayItem) item).setDiscountPercentage(discountPercentage);
                    }

                    int cantidad = item.getAmount();

                    long usosDisponibles = maxUses - uses;
                    int usosAplicables = (int) Math.min(usosDisponibles, cantidad);

                    if (usosAplicables <= 0) {
                        Toast.makeText(requireContext(), "Ya se usaron todos los usos disponibles del cupón", Toast.LENGTH_SHORT).show();
                        return;
                    }

                                        ((CartDisplayItem) item).setDiscountPercentage(discountPercentage);
                                        doc.getReference().update("uses", uses + usosAplicables);

                                        shoppingCart.updateItem((CartDisplayItem) item);
                                        Toast.makeText(requireContext(), String.format("Cupón aplicado a %d unidad(es)", usosAplicables), Toast.LENGTH_SHORT).show();


                    shoppingCart.updateItem((CartDisplayItem) item);
                    Toast.makeText(requireContext(), "Cupón aplicado correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al validar cupón", Toast.LENGTH_SHORT).show();
                    Log.e("CouponError", "Error al consultar cupón", e);
                });
    }
}