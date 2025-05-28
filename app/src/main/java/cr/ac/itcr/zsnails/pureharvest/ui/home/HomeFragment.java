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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentHomeBinding;
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

        int sectionSpacing = (int) getResources().getDimension(R.dimen.section_spacing);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        shoppingCart = new ViewModelProvider(requireActivity()).get(ShoppingCartViewModel.class);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = sectionSpacing;

        final ProductAdapter topAdapter = new ProductAdapter(new ArrayList<>(), this, this, true, false);
        TopSoldSectionView topSoldSection = new TopSoldSectionView(requireContext());
        topSoldSection.setTitle(getString(R.string.carousel_top_sold));
        topSoldSection.setAdapter(topAdapter);
        topSoldSection.getRecyclerView().addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        topSoldSection.setLayoutParams(layoutParams);
        binding.containerSections.addView(topSoldSection);

        final ProductAdapter adapter = new ProductAdapter(new ArrayList<>(), this, this, false, false);
        ProductSectionView section = new ProductSectionView(requireContext());
        section.setTitle(getString(R.string.products_list_home));
        section.setAdapter(adapter);
        section.getRecyclerView().addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        binding.containerSections.addView(section);

        final ProductAdapter specialOffersAdapter = new ProductAdapter(new ArrayList<>(), this, this, false, true);
        SpecialOffersSectionView specialOffersSection = new SpecialOffersSectionView(requireContext());
        specialOffersSection.setTitle(getString(R.string.carousel_special_offers));
        specialOffersSection.setAdapter(specialOffersAdapter);
        specialOffersSection.getRecyclerView().addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        specialOffersSection.setLayoutParams(layoutParams);

        viewModel.getProducts().observe(getViewLifecycleOwner(), allProducts -> {
            if (allProducts == null) return;

            List<Product> sortedRandomList = new ArrayList<>(allProducts);
            Collections.sort(sortedRandomList, (p1, p2) -> {
                boolean p1Paid = false;
                com.google.firebase.Timestamp p1Timestamp = null;
                List<Object> p1StandOut = p1.getStandOutPayment();
                if (p1StandOut != null && p1StandOut.size() == 2 && p1StandOut.get(0) instanceof Boolean && (Boolean) p1StandOut.get(0)) {
                    p1Paid = true;
                    if (p1StandOut.get(1) instanceof com.google.firebase.Timestamp) {
                        p1Timestamp = (com.google.firebase.Timestamp) p1StandOut.get(1);
                    } else if (p1StandOut.get(1) instanceof java.util.Date) {
                        p1Timestamp = new com.google.firebase.Timestamp((java.util.Date) p1StandOut.get(1));
                    }
                }

                boolean p2Paid = false;
                com.google.firebase.Timestamp p2Timestamp = null;
                List<Object> p2StandOut = p2.getStandOutPayment();
                if (p2StandOut != null && p2StandOut.size() == 2 && p2StandOut.get(0) instanceof Boolean && (Boolean) p2StandOut.get(0)) {
                    p2Paid = true;
                    if (p2StandOut.get(1) instanceof com.google.firebase.Timestamp) {
                        p2Timestamp = (com.google.firebase.Timestamp) p2StandOut.get(1);
                    } else if (p2StandOut.get(1) instanceof java.util.Date) {
                        p2Timestamp = new com.google.firebase.Timestamp((java.util.Date) p2StandOut.get(1));
                    }
                }

                if (p1Paid && !p2Paid) return -1;
                if (!p1Paid && p2Paid) return 1;
                if (p1Paid && p2Paid) {
                    if (p1Timestamp != null && p2Timestamp != null) {
                        return p1Timestamp.compareTo(p2Timestamp);
                    } else if (p1Timestamp != null) {
                        return -1;
                    } else if (p2Timestamp != null) {
                        return 1;
                    }
                    return 0;
                }
                return 0;
            });
            adapter.updateData(sortedRandomList);

            List<Product> topSold = allProducts.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getTotalUnitsSold(), p1.getTotalUnitsSold()))
                    .limit(10)
                    .collect(Collectors.toList());
            topAdapter.updateData(topSold);

            List<Product> discountedProducts = allProducts.stream()
                    .filter(p -> p.getSaleDiscount() != null && p.getSaleDiscount() > 0)
                    .collect(Collectors.toList());

            boolean specialOffersSectionExists = binding.containerSections.indexOfChild(specialOffersSection) != -1;

            if (!discountedProducts.isEmpty()) {
                specialOffersAdapter.updateData(discountedProducts);
                if (!specialOffersSectionExists) {
                    binding.containerSections.addView(specialOffersSection, 1);
                }
            } else {
                if (specialOffersSectionExists) {
                    binding.containerSections.removeView(specialOffersSection);
                }
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