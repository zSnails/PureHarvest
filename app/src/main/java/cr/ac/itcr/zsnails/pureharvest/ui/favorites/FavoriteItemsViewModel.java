package cr.ac.itcr.zsnails.pureharvest.ui.favorites;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.domain.repository.ProductsRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FavoriteItemsViewModel extends ViewModel {
    private final ProductsRepository repo;
    public MutableLiveData<List<Product>> products;

    @Inject
    public FavoriteItemsViewModel(
            @NonNull ProductsRepository repo
    ) {
        this.repo = repo;
    }

    public void loadFavoriteItems(String clientId) {
        this.products = repo.getFavoriteProducts(clientId);
    }
}