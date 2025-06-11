package cr.ac.itcr.zsnails.pureharvest.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentHomeBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;
import cr.ac.itcr.zsnails.pureharvest.decoration.RandomItemListMarginItemDecoration;
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

        //Section spacing
        int sectionSpacing = (int) getResources().getDimension(R.dimen.section_spacing);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        shoppingCart = new ViewModelProvider(requireActivity()).get(ShoppingCartViewModel.class);
        View searchToolsSection = getLayoutInflater().inflate(R.layout.search_tools, binding.containerSections, false);


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = sectionSpacing;

        // Searchbar and filters
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.section_spacing);
        searchToolsSection.setLayoutParams(layoutParams);
        binding.containerSections.addView(searchToolsSection);

        // Top 10 Best Sellers Section
        final ProductAdapter topAdapter = new ProductAdapter(new ArrayList<>(), this, this, true, false);
        TopSoldSectionView topSoldSection = new TopSoldSectionView(requireContext());
        topSoldSection.setTitle(getString(R.string.carousel_top_sold));
        topSoldSection.setAdapter(topAdapter);
        topSoldSection.getRecyclerView().addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        topSoldSection.setLayoutParams(layoutParams);
        binding.containerSections.addView(topSoldSection);

        // Random List Section
        final ProductAdapter adapter = new ProductAdapter(new ArrayList<>(), this, this, false, false);
        ProductSectionView section = new ProductSectionView(requireContext());

        section.setTitle(getString(R.string.products_list_home));
        section.setAdapter(adapter);
        section.getRecyclerView().addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        binding.containerSections.addView(section);

        // Special Offers Section
        final ProductAdapter specialOffersAdapter = new ProductAdapter(new ArrayList<>(), this, this, false, true);
        SpecialOffersSectionView specialOffersSection = new SpecialOffersSectionView(requireContext());
        specialOffersSection.setTitle(getString(R.string.carousel_special_offers));
        specialOffersSection.setAdapter(specialOffersAdapter);
        specialOffersSection.getRecyclerView().addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        specialOffersSection.setLayoutParams(layoutParams);

        // Observe all products
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.updateData(products);

            // Obtain Top 10 Best Sellers
            List<Product> topSold = products.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getTotalUnitsSold(), p1.getTotalUnitsSold()))
                    .limit(10)
                    .collect(Collectors.toList());

            topAdapter.updateData(topSold);

            // Filter discounted products
            List<Product> discountedProducts = products.stream()
                    .filter(p -> p.getSaleDiscount() > 0)
                    .collect(Collectors.toList());

            if (!discountedProducts.isEmpty()) {
                // Only create the section if there are discounted products
                specialOffersAdapter.updateData(discountedProducts);
                binding.containerSections.addView(specialOffersSection, 1); // Insertar en la posición 1 (entre topSold y randomList)
            }
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