package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import static androidx.lifecycle.Lifecycle.State.RESUMED;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentShoppingCartBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter.ShoppingCartAdapter;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class ShoppingCartFragment extends Fragment implements MenuProvider {

    private FragmentShoppingCartBinding binding;

    private ShoppingCartViewModel shoppingCart;
    private AlertDialog deletionDialog;
    private ShoppingCartAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        this.shoppingCart = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        this.binding = FragmentShoppingCartBinding.inflate(inflater, container, false);
        this.adapter = new ShoppingCartAdapter();
        this.binding.shoppingCartRecyclerView.setAdapter(adapter);
        this.binding.shoppingCartRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        this.binding.shoppingCartRecyclerView.addItemDecoration(
                new MarginItemDecoration(
                        (int) getResources().getDimension(R.dimen.list_item_margin)));
        this.binding.seedDataButton.setOnClickListener(this::onSeedClick);
        this.binding.resetDataButton.setOnClickListener(this::onResetDatabaseClick);

        deletionDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.shopping_cart_clear_confirmation_dialog_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes,
                        (dialogInterface, i) ->
                                shoppingCart.removeAllItems(shoppingCart::loadAllItems))
                .setNegativeButton(android.R.string.no, null)
                .create();
        shoppingCart.loadAllItems();
        shoppingCart.items.observe(getViewLifecycleOwner(), adapter::setItems);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), RESUMED);
        setupSwipeHandler();

        return this.binding.getRoot();
    }

    private void setupSwipeHandler() {
        ItemTouchHelper.SimpleCallback swipeHandler = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                int itemId = (int) viewHolder.getItemId();
                shoppingCart.deleteById(itemId);
                adapter.removeAt(position);
            }
        };
        new ItemTouchHelper(swipeHandler).attachToRecyclerView(this.binding.shoppingCartRecyclerView);
    }

    public void onResetDatabaseClick(View view) {
        shoppingCart.removeAllItems(shoppingCart::loadAllItems);
    }

    public void onSeedClick(View view) {
        shoppingCart.seedDatabase(shoppingCart::loadAllItems);
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
        }
        return false;
    }
}