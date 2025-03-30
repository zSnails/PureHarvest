package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentShoppingCartBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter.ShoppingCartAdapter;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class ShoppingCartFragment extends Fragment {

    private FragmentShoppingCartBinding binding;

    private ShoppingCartViewModel shoppingCart;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        this.shoppingCart = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        this.binding = FragmentShoppingCartBinding.inflate(inflater, container, false);
        ShoppingCartAdapter adapter = new ShoppingCartAdapter();
        this.binding.shoppingCartRecyclerView.setAdapter(adapter);
        this.binding.shoppingCartRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.binding.shoppingCartRecyclerView.addItemDecoration(new MarginItemDecoration(
                (int) getResources().getDimension(R.dimen.list_item_margin)));
        this.binding.seedDataButton.setOnClickListener(this::onSeedClick);

        shoppingCart.loadAllItems();
        shoppingCart.items.observe(getViewLifecycleOwner(), adapter::setItems);

        return this.binding.getRoot();

    }

    public void onSeedClick(View view) {
        shoppingCart.seedDatabase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}