package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompnayProductsListBinding;

public class CompanyProductsListFragment extends Fragment {

    FragmentCompnayProductsListBinding binding;
    private ProductAdapter productAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCompnayProductsListBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        setupRecyclerView();

        return root;
    }

    private void setupRecyclerView(){
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Tomates Cherry", 1200, "https://via.placeholder.com/150"));
        productList.add(new Product("Lechuga", 2100, "https://via.placeholder.com/150"));
        productList.add(new Product("Arroz", 4000, "https://via.placeholder.com/150"));

        productAdapter = new ProductAdapter(productList);

        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewProducts.setAdapter(productAdapter);

    }
}