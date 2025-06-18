package cr.ac.itcr.zsnails.pureharvest.ui.account;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.data.model.FavoriteProduct;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.entities.FavoriteDisplayProduct;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FavoritesViewModel extends ViewModel {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    public MutableLiveData<List<FavoriteDisplayProduct>> displayProducts = new MutableLiveData<>();
    public MutableLiveData<Boolean> loggedIn = new MutableLiveData<>(false);

    @Inject
    public FavoritesViewModel(
            @NonNull final FirebaseFirestore firestore,
            @NonNull final FirebaseAuth auth
    ) {
        this.firestore = firestore;
        this.auth = auth;
    }

    public void checkLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            loggedIn.postValue(false);
        } else {
            this.user.postValue(user);
            loggedIn.postValue(true);
        }
    }

    public void loadFavorites() {
        if (!loggedIn.getValue()) return;
        FirebaseUser user = this.user.getValue();
        firestore.collection("favorites").whereEqualTo("ownerId", user.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    var favorites = queryDocumentSnapshots.getDocuments().stream().map(a -> a.toObject(FavoriteProduct.class).productId).collect(Collectors.toList());
                    List<FavoriteDisplayProduct> dp = new ArrayList<>();
                    if (favorites.isEmpty()) {
                        displayProducts.postValue(dp);
                        return;
                    }

                    firestore.collection("products").whereIn("id", favorites).get()
                            .addOnSuccessListener(querySnapshot -> {
                                querySnapshot.getDocuments().forEach(doc -> {
                                    Product prod = doc.toObject(Product.class);
                                    dp.add(FavoriteDisplayProduct.from(prod));
                                });
                                Log.d("loaded", "loaded the products");
                                displayProducts.postValue(dp);
                            });
                });
    }

}
