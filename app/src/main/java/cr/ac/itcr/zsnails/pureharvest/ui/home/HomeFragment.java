package cr.ac.itcr.zsnails.pureharvest.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentHomeBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.domain.repository.ShoppingCartRepository;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.ShoppingCartViewModel;
import cr.ac.itcr.zsnails.pureharvest.ui.client.ViewProductActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements ProductAdapter.AddToCartListener, ProductAdapter.OnProductClickListener {

    @Inject
    public ShoppingCartRepository repo = null;
    @Inject
    public ExecutorService executor;
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ShoppingCartViewModel shoppingCart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        shoppingCart = new ViewModelProvider(requireActivity()).get(ShoppingCartViewModel.class);

        final ProductAdapter adapter = new ProductAdapter(new ArrayList<>(), this, this);

        ProductSectionView section = new ProductSectionView(requireContext());
        section.setTitle(getString(R.string.products_list_home));
        section.setAdapter(adapter);

        section.getRecyclerView().addItemDecoration(
                new MarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );

        binding.containerSections.addView(section);

        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.updateData(products);
            section.getRecyclerView().getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
            section.getRecyclerView().requestLayout();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(Product product) {
        var item = new CartItem();
        item.productId = product.getId();
        item.amount = 1;
        shoppingCart.insertItem(item);
        Toast.makeText(requireContext(), "Se ha agregado el producto al carrito de compras", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(requireContext(), ViewProductActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}