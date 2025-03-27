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

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentShoppingCartBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter.ShoppingCartAdapter;

public final class ShoppingCartFragment extends Fragment {

    private FragmentShoppingCartBinding binding;
    private ShoppingCartViewModel shoppingCart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        shoppingCart = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false);
        // TODO: remove hard coded margin size, this must also use a resource, for the time being I'll use this POS
        binding.shoppingCartRecyclerView.addItemDecoration(new MarginItemDecoration(2));
        binding.shoppingCartRecyclerView.setAdapter(new ShoppingCartAdapter());
        binding.shoppingCartRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.shoppingCartRecyclerView.setVisibility(View.VISIBLE);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}