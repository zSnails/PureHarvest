package cr.ac.itcr.zsnails.pureharvest.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;

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
import cr.ac.itcr.zsnails.pureharvest.util.ProductFilterUtils;
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

    private final List<Product> allProducts = new ArrayList<>();

    private TopSoldSectionView topSoldSection;
    private SpecialOffersSectionView specialOffersSection;
    private ProductAdapter specialOffersAdapter;

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

        //Section spacing
        int sectionSpacing = (int) getResources().getDimension(R.dimen.section_spacing);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = sectionSpacing;

        // Searchbar and filters
        View searchToolsSection = getLayoutInflater().inflate(R.layout.search_tools, binding.containerSections, false);
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.section_spacing);
        searchToolsSection.setLayoutParams(layoutParams);
        binding.containerSections.addView(searchToolsSection);

        EditText searchEditText = searchToolsSection.findViewById(R.id.searchEditText);
        Button btnAdvanced = searchToolsSection.findViewById(R.id.btnAdvancedSearch);
        View filtersLayout = searchToolsSection.findViewById(R.id.advancedFiltersLayout);
        Slider priceSlider = searchToolsSection.findViewById(R.id.priceSlider);
        TextView priceValue = searchToolsSection.findViewById(R.id.priceValue);

        Chip chipType = searchToolsSection.findViewById(R.id.chipFilterType);
        Chip chipAcidity = searchToolsSection.findViewById(R.id.chipFilterAcidity);
        Chip chipName = searchToolsSection.findViewById(R.id.chipFilterName);
        // Set initial chip selection
        chipName.setChecked(true);

        // Set initial slider value to max
        priceSlider.setValue(priceSlider.getValueTo()); // this is 20000f

        // Update the price label accordingly
        priceValue.setText("₡" + String.format("%,d", (int) priceSlider.getValue()));

        btnAdvanced.setOnClickListener(v -> {
            if (filtersLayout.getVisibility() == View.GONE) {
                filtersLayout.setVisibility(View.VISIBLE);
                filtersLayout.setAlpha(0f);
                filtersLayout.animate().alpha(1f).setDuration(300).start();
            } else {
                filtersLayout.animate().alpha(0f).setDuration(300).withEndAction(() -> filtersLayout.setVisibility(View.GONE)).start();
            }
        });

        // Update price label
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            int rounded = (int) value;
            priceValue.setText("₡" + String.format("%,d", rounded));
        });

        // Top 10 Best Sellers Section
        final ProductAdapter topAdapter = new ProductAdapter(new ArrayList<>(), this, this, true, false);
        topSoldSection = new TopSoldSectionView(requireContext());
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
        specialOffersAdapter = new ProductAdapter(new ArrayList<>(), this, this, false, true);
        specialOffersSection = new SpecialOffersSectionView(requireContext());
        specialOffersSection.setTitle(getString(R.string.carousel_special_offers));
        specialOffersSection.setAdapter(specialOffersAdapter);
        specialOffersSection.getRecyclerView().addItemDecoration(
                new RandomItemListMarginItemDecoration((int) getResources().getDimension(R.dimen.random_item_list_margin))
        );
        specialOffersSection.setLayoutParams(layoutParams);

        // Observe all products
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            allProducts.clear();
            allProducts.addAll(products);
            adapter.updateData(products);
            // Create a mutable copy of the products list for the random section, to sort it
            // without affecting the original list used for other sections.
            List<Product> productsForRandomList = new ArrayList<>(products);

            // Sort the list for the random section based on standOutPayment.
            // Products with standOutPayment true come first, ordered by timestamp.
            Collections.sort(productsForRandomList, (p1, p2) -> {
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

            adapter.updateData(productsForRandomList); // Use the sorted list for the random products adapter

            // Obtain Top 10 Best Sellers
            List<Product> topSold = products.stream() // Use the original 'products' list here
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
                binding.containerSections.addView(specialOffersSection, 2); // Insertar en la posición 1 (entre topSold y randomList)
            }
        });

        // Search Filtering
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();

                if (query.isEmpty()) {
                    // Show all sections again
                    if (binding.containerSections.indexOfChild(topSoldSection) == -1) {
                        binding.containerSections.addView(topSoldSection, 1); // at the top
                    }
                    if (specialOffersAdapter.getItemCount() > 0 &&
                            binding.containerSections.indexOfChild(specialOffersSection) == -1) {
                        binding.containerSections.addView(specialOffersSection, 2); // after topSold
                    }
                    adapter.updateData(allProducts); // restore full product list
                } else {
                    // Hide top sellers and special offers while searching
                    binding.containerSections.removeView(topSoldSection);
                    binding.containerSections.removeView(specialOffersSection);

                    // Show only filtered products
                    float selectedPrice = priceSlider.getValue();
                    applyFilters(query, allProducts, adapter, chipName, chipType, chipAcidity, selectedPrice);
                }
            }
        });

        View.OnClickListener filterChangeListener = v -> {
            String currentQuery = searchEditText.getText().toString().trim().toLowerCase();
            float selectedPrice = priceSlider.getValue();
            applyFilters(currentQuery, allProducts, adapter, chipName, chipType, chipAcidity, selectedPrice);
        };

        chipType.setOnClickListener(filterChangeListener);
        chipAcidity.setOnClickListener(filterChangeListener);

        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            String currentQuery = searchEditText.getText().toString().trim().toLowerCase();
            applyFilters(currentQuery, allProducts, adapter, chipName, chipType, chipAcidity, value);
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

    private void applyFilters(String searchQuery, List<Product> products, ProductAdapter adapter,
                              Chip chipName, Chip chipType, Chip chipAcidity, float maxPrice) {

        List<Product> filtered = ProductFilterUtils.filterProducts(
                searchQuery,
                products,
                chipName.isChecked(),
                chipType.isChecked(),
                chipAcidity.isChecked(),
                maxPrice
        );

        adapter.updateData(filtered);
    }
}
