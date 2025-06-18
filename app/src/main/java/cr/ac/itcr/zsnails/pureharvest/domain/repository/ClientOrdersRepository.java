package cr.ac.itcr.zsnails.pureharvest.domain.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;

public class ClientOrdersRepository {
    private final FirebaseFirestore db;

    private final MutableLiveData<List<Order>> ordersLiveData = new MutableLiveData<>(new ArrayList<>());

    public ClientOrdersRepository(
            @NonNull final FirebaseFirestore firebase
    ) {
        this.db = firebase;
    }

    public MutableLiveData<List<Order>> getClientOrders(String clientId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Order> orders = new ArrayList<>();

            db.collection("orders").whereEqualTo("userId", clientId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        var documents = querySnapshot.getDocuments();
                        documents.stream().forEach(document -> {
                            Order order = document.toObject(Order.class);
                            orders.add(order);
                        });
                        ordersLiveData.postValue(orders);
                    });
        });
        return ordersLiveData;
    }

}
