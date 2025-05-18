package cr.ac.itcr.zsnails.pureharvest.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import cr.ac.itcr.zsnails.pureharvest.LoginActivity;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentAccountBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import cr.ac.itcr.zsnails.pureharvest.entities.FavoriteDisplayProduct;
import cr.ac.itcr.zsnails.pureharvest.ui.account.adapter.Card;
import cr.ac.itcr.zsnails.pureharvest.ui.account.adapter.FavoriteItemsAdapter;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.ShoppingCartViewModel;
import cr.ac.itcr.zsnails.pureharvest.ui.client.ViewProductActivity;

public class AccountFragment extends Fragment implements Card.AddToCartListener, Card.ItemClickListener {

    private FragmentAccountBinding binding;
    private FavoritesViewModel favoritesViewModel;
    private ShoppingCartViewModel shoppingCart;
    private ActivityResultLauncher<Intent> activityLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.favoritesViewModel = provider.get(FavoritesViewModel.class);
        this.shoppingCart = provider.get(ShoppingCartViewModel.class);

        this.activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), a -> {
            favoritesViewModel.loadFavorites();
        });

        this.favoritesViewModel.loggedIn.observe(getViewLifecycleOwner(), loggedIn -> {
            if (loggedIn) {
                binding.loginNoticeGroup.setVisibility(View.GONE);
                binding.favoriteListRecyclerView.setVisibility(View.VISIBLE);
                this.favoritesViewModel.loadFavorites();
            } else {
                binding.loginNoticeGroup.setVisibility(View.VISIBLE);
                binding.favoriteListRecyclerView.setVisibility(View.GONE);
            }
        });
        this.favoritesViewModel.checkLoggedIn();
        this.binding.favoriteListRecyclerView.addItemDecoration(new MarginItemDecoration(20));

        FavoriteItemsAdapter adapter = new FavoriteItemsAdapter(this, this);
        this.binding.favoriteListRecyclerView.setAdapter(adapter);
        this.binding.favoriteListRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        this.favoritesViewModel.displayProducts.observe(getViewLifecycleOwner(), adapter::setItems);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up the Log In button to navigate to LoginActivity
        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onAddToCart(FavoriteDisplayProduct product, int position) {
        var item = new CartItem();
        item.productId = product.productId;
        item.amount = 1;
        shoppingCart.insertItem(item);
        Toast.makeText(requireContext(), "Se ha agregado el producto al carrito de compras", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(FavoriteDisplayProduct product, int position) {
        Intent intent = new Intent(requireContext(), ViewProductActivity.class);
        intent.putExtra("product_id", product.productId);
        activityLauncher.launch(intent);
    }
}