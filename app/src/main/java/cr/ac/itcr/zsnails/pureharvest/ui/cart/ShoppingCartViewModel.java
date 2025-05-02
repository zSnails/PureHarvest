package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.domain.repository.ShoppingCartRepository;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ShoppingCartViewModel extends ViewModel {

    private final ShoppingCartRepository repo;
    private final ExecutorService executor;
    private final ArrayList<CartItem> seedData = new ArrayList<>(Arrays.asList(
            new CartItem("aaa", 1),
            new CartItem("aab", 2),
            new CartItem("aac", 3),
            new CartItem("aad", 4),
            new CartItem("aae", 5)
    ));
    private final List<ItemOperationEventListener> operationListeners = new LinkedList<>();
    public MutableLiveData<List<Item>> items = new MutableLiveData<>();
    public MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);

    @Inject
    public ShoppingCartViewModel(
            @NonNull final ShoppingCartRepository repo,
            @NonNull final ExecutorService executor
    ) {
        this.repo = repo;
        this.executor = executor;
    }

    public void addItemOperationEventListener(ItemOperationEventListener cb) {
        this.operationListeners.add(cb);
    }

    public void loadAllItems() {
        executor.execute(() -> {
            var it = repo.all().stream().map(a -> (Item) a).collect(Collectors.toList());
            items.postValue(it);
            computeSubTotal(it);
        });
    }


    private void computeSubTotal(List<Item> it) {
        Double total = it.stream().mapToDouble(i -> i.getPrice() * i.getAmount()).sum();
        subtotal.postValue(total);
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
            value.clear();
            items.postValue(value);
            computeSubTotal();
        });
    }

    public void insertItem(CartItem item) {
        repo.insert(item);
        for (ItemOperationEventListener operationListener : operationListeners) {
            operationListener.onItemCreated(item);
        }
    }

    public void seedDatabase(OnCompleteCallback cb) {
        executor.execute(() -> {
            for (CartItem seedDatum : seedData) {
                repo.insert(seedDatum);
            }
            cb.onComplete();
        });
    }

    public void updateItem(Item item) {
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
        subtotal.postValue(subtotal.getValue() - item.getPrice() * item.getAmount());
        items.getValue().remove(item);
        for (ItemOperationEventListener operationListener : operationListeners) {
            operationListener.onItemRemoved(item, idx);
        }
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