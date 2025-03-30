package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

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

    public MutableLiveData<List<CartItem>> items = new MutableLiveData<>();

    @Inject
    public ShoppingCartViewModel(
            @NonNull final ShoppingCartRepository repo,
            @NonNull final ExecutorService executor
    ) {
        this.repo = repo;
        this.executor = executor;
    }

    public void loadAllItems() {
        executor.execute(() -> {
            items.postValue(repo.all());
        });
    }

    public void removeAllItems(OnCompleteCallback cb) {
        executor.execute(() -> {
            repo.deleteAll();
            cb.onComplete();
        });
    }

    public void insertItem(CartItem item) {
        repo.insert(item);
    }

    public void seedDatabase(OnCompleteCallback cb) {
        executor.execute(() -> {
            for (CartItem seedDatum : seedData) {
                insertItem(seedDatum);
            }
            cb.onComplete();
        });
    }

    public void deleteById(Integer id) {
        executor.execute(() -> {
            repo.deleteById(id);
        });
    }

    @FunctionalInterface
    public interface OnCompleteCallback {
        void onComplete();
    }

}