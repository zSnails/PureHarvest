package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompnayProductsListBinding;

public class CompanyProductsListFragment extends Fragment {

    FragmentCompnayProductsListBinding binding;
    private ProductAdapter productAdapter;

    private final List<Product> productList = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final List<String> allowedIds = Arrays.asList("1", "2");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCompnayProductsListBinding.inflate(inflater, container, false);

        View root = binding.getRoot();


        fetchProductsFromFirestore();
        setupRecyclerView();

        return root;
    }

    private void setupRecyclerView(){
        //List<Product> productList = new ArrayList<>();
        //productList.add(new Product("Tomates Cherry", 1200, "https://via.placeholder.com/150"));
        //productList.add(new Product("Lechuga", 2100, "https://via.placeholder.com/150"));
        //productList.add(new Product("Arroz", 4000, "https://via.placeholder.com/150"));

        productAdapter = new ProductAdapter(productList);

        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewProducts.setAdapter(productAdapter);

    }

    private void fetchProductsFromFirestore() {
        firestore.collection("products")
                .whereEqualTo("sellerId", "1")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    if (total == 0) return;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId(); // O usa doc.getString("id") si el campo es explícito
                        String name = doc.getString("name");
                        Long priceLong = doc.getLong("price");

                        if (name == null || priceLong == null) continue;

                        int price = priceLong.intValue();

                        StorageReference imageRef = storage.getReference()
                                .child("product_images/" + id + ".jpg");

                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            Product product = new Product(name, price, imageUrl);
                            productList.add(product);

                            // Solo actualizar una vez todos estén listos
                            if (productList.size() == total) {
                                productAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(e -> {
                            // Si no hay imagen, igual mostrar producto con imagen por defecto (opcional)
                            Product product = new Product(name, price, null); // o una imagen por defecto
                            productList.add(product);

                            if (productList.size() == total) {
                                productAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }


}