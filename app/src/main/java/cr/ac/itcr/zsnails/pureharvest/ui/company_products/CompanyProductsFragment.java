package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyProductsBinding;

public class CompanyProductsFragment extends Fragment {

    FirebaseFirestore db;

    FragmentCompanyProductsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCompanyProductsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();


        binding.button.setOnClickListener(v -> {
            // Accede a los EditTexts a través del binding
            String name = binding.productName.getText().toString();
            String type = binding.productType.getText().toString();
            String priceStr = binding.productPrice.getText().toString();
            String acidityStr = binding.productAcidity.getText().toString();
            String description = binding.productDescription.getText().toString();

            // Validación básica (puedes expandirla)
            if (name.isEmpty() || type.isEmpty() || priceStr.isEmpty() || acidityStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), R.string.error_fields_cannot_be_empty, Toast.LENGTH_SHORT).show(); // Necesitarás añadir este string
                return;
            }

            double price;
            double acidity;

            try {
                price = Double.parseDouble(priceStr);
                acidity = Double.parseDouble(acidityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), R.string.error_invalid_number_format, Toast.LENGTH_SHORT).show(); // Necesitarás añadir este string
                return;
            }

            ProductM product = new ProductM(name, type, price, acidity, description);
            addProductToFirestore(product);
        });

        return root;
    }

    private void addProductToFirestore(ProductM product) {
        CollectionReference productsRef = db.collection("products");

        productsRef.add(product)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(getContext(), getString(R.string.toast_product_added_successfully), Toast.LENGTH_SHORT).show();

                    clearInputFields();
                })
                .addOnFailureListener(e -> {

                    String errorMessage = getString(R.string.toast_error_adding_product, e.getMessage());
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    private void clearInputFields() {
        if (binding != null) {
            binding.productName.setText("");
            binding.productType.setText("");
            binding.productPrice.setText("");
            binding.productAcidity.setText("");
            binding.productDescription.setText("");
            binding.productName.requestFocus();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}