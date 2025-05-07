package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.domain.repository.ShoppingCartRepository;
import cr.ac.itcr.zsnails.pureharvest.entities.CartDisplayItem;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ShoppingCartViewModel extends ViewModel {

    private final ShoppingCartRepository repo;
    private final ExecutorService executor;
    private final FirebaseFirestore db;
    private final ArrayList<CartItem> seedData = new ArrayList<>(List.of());
    private final List<ItemOperationEventListener> operationListeners = new LinkedList<>();
    public MutableLiveData<List<CartDisplayItem>> items;
    public MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);

    @Inject
    public ShoppingCartViewModel(
            @NonNull final ShoppingCartRepository repo,
            @NonNull final ExecutorService executor,
            @NonNull final FirebaseFirestore db
    ) {
        this.repo = repo;
        this.executor = executor;
        this.db = db;
        getCartDisplayItems();
        items.observeForever(cartDisplayItems -> {
            computeSubTotal();
        });
    }

    public void addItemOperationEventListener(ItemOperationEventListener cb) {
        this.operationListeners.add(cb);
    }

    public void getCartDisplayItems() {
        if (this.items == null) this.items = repo.getCartDisplayItems();
    }

    private void computeSubTotal() {
        var it = items.getValue();
        if (it == null) return;
        double total = it.stream().mapToDouble(i -> i.getPrice() * i.getAmount()).sum();
        subtotal.postValue(Double.max(total, 0.0));
    }

    public void removeAllItems() {
        executor.execute(() -> {
            repo.deleteAll();
            var value = items.getValue();
            if (value != null) {
                value.clear();
                items.postValue(value);
                computeSubTotal();
            }
        });
    }

    public void insertItem(CartItem item) {
        // TODO: refactor this POS, hacky AF last minute code, I'll find some time to refactor this or not, whatever comes first
        executor.execute(() -> {
            CartItem found = repo.find(item.productId);
            if (found != null) {
                CartDisplayItem local = items.getValue()
                        .stream()
                        .filter(a -> a.productId.compareTo(item.productId) == 0)
                        .findFirst().get();
                int newAmount = item.amount + found.amount;
                local.setAmount(newAmount);
                repo.setAmount(found.id, newAmount);
            } else {
                repo.insert(item);
            }
            computeSubTotal();
        });
    }

    public void seedDatabase(OnCompleteCallback cb) {
        executor.execute(() -> {
            for (CartItem seedDatum : seedData) {
                repo.insert(seedDatum);
            }
            cb.onComplete();
        });
    }

    public void updateItem(CartDisplayItem item) {
        if (item.getAmount() < 0)
            throw new IllegalStateException("an item's amount cannot be negative");
        int idx = items.getValue().indexOf(item);
        items.getValue().set(idx, item);
        for (ItemOperationEventListener operationListener : operationListeners) {
            operationListener.onItemUpdated(item);
        }
        executor.execute(() -> {
            repo.updateAmount(item);
            computeSubTotal();
        });
    }

    public void removeById(Long id) {
        executor.execute(() -> {
            repo.deleteById(id);
        });
        Item item = items.getValue()
                .stream()
                .filter((it) -> it.getId().equals(id)).collect(Collectors.toList()).get(0);
        int idx = items.getValue().indexOf(item);
        items.getValue().remove(item);
        for (ItemOperationEventListener operationListener : operationListeners) {
            operationListener.onItemRemoved(item, idx);
        }
        computeSubTotal();
    }

    public interface ItemOperationEventListener {
        void onItemCreated(Item item);

        void onItemRemoved(Item item, int position);

        void onItemUpdated(Item item);
    }

    @FunctionalInterface
    public interface OnCompleteCallback {
        void onComplete();
    }

}