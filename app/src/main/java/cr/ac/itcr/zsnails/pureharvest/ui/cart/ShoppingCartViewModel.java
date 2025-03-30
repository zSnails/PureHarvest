package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.domain.LocalCartDatabase;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ShoppingCartViewModel extends ViewModel {
    private final LocalCartDatabase db;
    private final ExecutorService databaseExecutor;
    private final ArrayList<CartItem> seedData = new ArrayList<>(Arrays.asList(
            new CartItem("aaa"),
            new CartItem("aab"),
            new CartItem("aac"),
            new CartItem("aad"),
            new CartItem("aae")
    ));

    public MutableLiveData<List<CartItem>> items = new MutableLiveData<>();

    @Inject
    public ShoppingCartViewModel(
            @NonNull final LocalCartDatabase db,
            @NonNull final ExecutorService databaseExecutor) {
        this.db = db;
        this.databaseExecutor = databaseExecutor;

    }

    public void loadAllItems() {
        databaseExecutor.execute(() -> {
            items.postValue(db.cartDao().getAll());
        });
    }

    public void insertItem(CartItem item) {
        db.cartDao().insertAll(item);
    }

    public void seedDatabase() {
        databaseExecutor.execute(() -> {
            for (CartItem seedDatum : seedData) {
                insertItem(seedDatum);
            }
        });
    }
}