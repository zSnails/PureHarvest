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
    EditText productName, productType, productPrice, productAcidity, productDescription;
    Button saveButton;

    FragmentCompanyProductsBinding binding;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCompanyProductsBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        productName = root.findViewById(R.id.productName);
        productType = root.findViewById(R.id.productType);
        productPrice = root.findViewById(R.id.productPrice);
        productAcidity = root.findViewById(R.id.productAcidity);
        productDescription = root.findViewById(R.id.productDescription);
        saveButton = root.findViewById(R.id.button);

        saveButton.setOnClickListener(v -> {

            String name = productName.getText().toString();
            String type = productType.getText().toString();
            double price = Double.parseDouble(productPrice.getText().toString());
            double acidity = Double.parseDouble(productAcidity.getText().toString());
            String description = productDescription.getText().toString();


            ProductM product = new ProductM(name, type, price, acidity, description);


            addProductToFirestore(product);
        });

        return root;
    }

    private void addProductToFirestore(ProductM product) {
        CollectionReference productsRef = db.collection("products");


        productsRef.add(product)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(getContext(), "Product added successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(getContext(), "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}