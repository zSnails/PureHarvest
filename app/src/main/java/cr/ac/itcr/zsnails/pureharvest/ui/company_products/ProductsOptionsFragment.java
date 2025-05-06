package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import cr.ac.itcr.zsnails.pureharvest.R;

public class ProductsOptionsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_products_options, container, false);

        ImageButton backButton = root.findViewById(R.id.backButtonOptions);
        Button cProductsBtn = root.findViewById(R.id.cProductsBtn);
        Button addProductBtn = root.findViewById(R.id.addProductBtn);

        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        cProductsBtn.setOnClickListener(v -> {
            if(getView() != null) {
                Navigation.findNavController(v).navigate(R.id.action_productsOptionsFragment_to_companyProductsListFragment);
            }
        });

        addProductBtn.setOnClickListener(v -> {
            if(getView() != null) {
                Navigation.findNavController(v).navigate(R.id.action_productsOptionsFragment_to_companyOrderListFragment);
            }
        });

        return root;
    }
}
