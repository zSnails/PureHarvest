package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentProductsOptionsBinding;


public class ProductsOptionsFragment extends Fragment {

    FragmentProductsOptionsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProductsOptionsBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        Button cProductsBtn = root.findViewById(R.id.cProductsBtn);

        Button addProductBtn = root.findViewById(R.id.addProductBtn);

        cProductsBtn.setOnClickListener(v-> Navigation.findNavController(v).navigate(R.id.action_productsOptionsFragment_to_companyProductsListFragment));

        addProductBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_productsOptionsFragment_to_CompanyProductsFragment));


        return root;
    }
}