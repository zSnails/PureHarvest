package cr.ac.itcr.zsnails.pureharvest.domain.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.dao.CartDao;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.domain.LocalCartDatabase;
import cr.ac.itcr.zsnails.pureharvest.entities.CartDisplayItem;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;

public class ShoppingCartRepository {

    private final CartDao dao;
    private final FirebaseFirestore db;

    private final MutableLiveData<List<CartDisplayItem>> displayItemsLiveData = new MutableLiveData<>(new ArrayList<>());

    @Inject
    public ShoppingCartRepository(
            @NonNull final LocalCartDatabase db,
            @NonNull final FirebaseFirestore firebase
    ) {
        this.dao = db.cartDao();
        this.db = firebase;
    }

    public void deleteAll() {
        dao.deleteAll();
    }

    public void deleteById(Long id) {
        dao.deleteById(id);
    }

    public long insert(CartItem item) {
        long id = dao.insertAll(item);
        Executors.newSingleThreadExecutor().execute(() -> {
            db.collection("products").document(item.productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) return;
                        Product product = documentSnapshot.toObject(Product.class);
                        CartDisplayItem displayItem = new CartDisplayItem();
                        displayItem.id = id;
                        displayItem.productId = item.productId;
                        displayItem.name = product.getName();
                        displayItem.price = product.getPrice();
                        displayItem.type = product.getType();
                        displayItem.amount = item.amount;
                        displayItemsLiveData.getValue().add(displayItem);
                        displayItemsLiveData.postValue(displayItemsLiveData.getValue());
                    });
        });
        return id;
    }

    public List<CartItem> all() {
        return dao.getAll();
    }

    public MutableLiveData<List<CartDisplayItem>> getCartDisplayItems() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CartItem> cartItems = dao.getAll();
            List<CartDisplayItem> displayItems = new ArrayList<>();

            for (CartItem cartItem : cartItems) {
                db.collection("products").document(cartItem.productId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) return;

                            Product product = documentSnapshot.toObject(Product.class);
                            CartDisplayItem displayItem = new CartDisplayItem();
                            displayItem.id = cartItem.id;
                            displayItem.productId = product.getId();
                            displayItem.name = product.getName();
                            displayItem.amount = cartItem.amount;
                            displayItem.type = product.getType();
                            displayItem.price = product.getPrice();
                            displayItems.add(displayItem);

                            if (displayItems.size() == cartItems.size()) {
                                displayItemsLiveData.postValue(displayItems);
                            }
                        });
            }
        });
        return displayItemsLiveData;
    }


    public void updateAmount(Item item) {
        dao.updateAmount(item.getId(), item.getAmount());
    }
}
