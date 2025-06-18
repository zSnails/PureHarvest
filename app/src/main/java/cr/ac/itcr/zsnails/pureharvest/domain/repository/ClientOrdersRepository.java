package cr.ac.itcr.zsnails.pureharvest.domain.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

public class ClientOrdersRepository {
    private final FirebaseFirestore db;

    private final MutableLiveData<List<Order>> ordersLiveData = new MutableLiveData<>(new ArrayList<>());

    public static class OrderDisplayItem {
        public String id;
        public String productId;
        public String name;
        public int amount;
        public String type;
        public double price;
        public String imageUrl;
    }

    private final MutableLiveData<List<OrderDisplayItem>> orderDisplayItems = new MutableLiveData<>(new ArrayList<>());

    public MutableLiveData<List<OrderDisplayItem>> getOrderDisplayItems(Order order) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Order.OrderItem> cartItems = order.getProductsBought();
            List<OrderDisplayItem> displayItems = new ArrayList<>();

            for (Order.OrderItem cartItem : cartItems) {
                db.collection("products").document(cartItem.id)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) return;

                            Product product = documentSnapshot.toObject(Product.class);
                            OrderDisplayItem displayItem = new OrderDisplayItem();
                            displayItem.id = cartItem.id;
                            displayItem.productId = product.getId();
                            displayItem.name = product.getName();
                            displayItem.amount = cartItem.amount;
                            displayItem.type = product.getType();
                            displayItem.price = product.getPrice();
                            displayItem.imageUrl = product.getFirstImageUrl();
                            displayItems.add(displayItem);

                            if (displayItems.size() == cartItems.size()) {
                                orderDisplayItems.postValue(displayItems);
                            }
                        });
            }
        });
        return orderDisplayItems;
    }

    public ClientOrdersRepository(
            @NonNull final FirebaseFirestore firebase
    ) {
        this.db = firebase;
    }

    public MutableLiveData<List<Order>> getClientOrders(String clientId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Order> orders = new ArrayList<>();

            db.collection("orders").whereEqualTo("userId", clientId).orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        var documents = querySnapshot.getDocuments();
                        documents.forEach(document -> {
                            Order order = document.toObject(Order.class);
                            orders.add(order);
                        });
                        ordersLiveData.postValue(orders);
                    })
                    .addOnFailureListener(a -> {
                        Log.e("orders:repository", a.getLocalizedMessage());
                    });
        });
        return ordersLiveData;
    }

}
