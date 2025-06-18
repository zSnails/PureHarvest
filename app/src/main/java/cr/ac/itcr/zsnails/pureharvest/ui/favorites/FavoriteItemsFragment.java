package cr.ac.itcr.zsnails.pureharvest.ui.favorites;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentFavoriteItemsBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.RandomItemListMarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.ShoppingCartViewModel;
import cr.ac.itcr.zsnails.pureharvest.ui.favorites.adapter.FavoriteProductViewHolder;
import cr.ac.itcr.zsnails.pureharvest.ui.favorites.adapter.FavoritesAdapter;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoriteItemsFragment extends Fragment implements FavoriteProductViewHolder.OnAddToCartListener {

    @Inject
    public FirebaseAuth auth;
    private FavoriteItemsViewModel mViewModel;
    private FavoritesAdapter adapter;
    private ShoppingCartViewModel shoppingCartViewModel;

    private FragmentFavoriteItemsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteItemsBinding.inflate(inflater, container, false);
        var provider = new ViewModelProvider(this);
        mViewModel = provider.get(FavoriteItemsViewModel.class);
        shoppingCartViewModel = provider.get(ShoppingCartViewModel.class);

        this.adapter = new FavoritesAdapter(this);
        this.binding.favoriteItemsRecyclerView.setAdapter(this.adapter);
        this.binding.favoriteItemsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        this.binding.favoriteItemsRecyclerView.addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        mViewModel.loadFavoriteItems(auth.getCurrentUser().getUid());
        mViewModel.products.observe(getViewLifecycleOwner(), products -> {
            Log.d("items", String.format("Got %d items", products.size()));
            this.adapter.setItems(products);
        });
        return binding.getRoot();
    }

    @Override
    public void onAddToCart(Product product) {
        CartItem cartItem = new CartItem();
        cartItem.productId = product.getId();
        cartItem.amount = 1;
        shoppingCartViewModel.insertItem(cartItem);
        Toast.makeText(requireContext(), "Product added to cart", Toast.LENGTH_SHORT).show();
    }
}