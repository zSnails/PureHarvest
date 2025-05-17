package cr.ac.itcr.zsnails.pureharvest.ui.client;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.data.model.FavoriteProduct;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ViewProductViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    public MutableLiveData<Boolean> favorite = new MutableLiveData<>(false);
    public String productId;

    @Inject
    public ViewProductViewModel(@NonNull final FirebaseFirestore firestore,
                                @NonNull final FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
    }

    public void toggleFavorite() {
        // I'll be checking if the user is logged in
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new NullPointerException(
                    "@Mathew: mae aquí usted tiene que mandar al usuario a que inicie sesión y luego regresar aquí para poder agregar el coso este a favoritos, y no le voy a ayudar");
        }
        this.firestore
                .collection("favorites")
                .document(String.format("%s%s", productId, user.getUid())).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        documentSnapshot
                                .getReference()
                                .delete()
                                .addOnSuccessListener(a -> favorite.postValue(false));
                    } else {
                        documentSnapshot
                                .getReference()
                                .set(new FavoriteProduct(productId, user.getUid()))
                                .addOnSuccessListener(a -> favorite.postValue(true));
                    }
                }).addOnFailureListener(exception -> {
                    Log.e("favorite-product", exception.getLocalizedMessage());
                });
    }

}
