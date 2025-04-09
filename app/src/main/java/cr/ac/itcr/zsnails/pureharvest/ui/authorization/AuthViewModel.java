// AuthViewModel.java
package cr.ac.itcr.zsnails.pureharvest.ui.authorization;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;  
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.core.SingleObserver;

public class AuthViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> registerResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getRegisterResult() {
        return registerResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Realiza la autenticación con Firebase Auth.
     */
    public void login(final String email, final String password) {
        Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final SingleEmitter<Boolean> emitter) {
                FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                emitter.onSuccess(true);
                            }
                        })
                        .addOnFailureListener(e -> emitter.onError(e));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(Boolean success) {
                loginResult.setValue(success);
            }

            @Override
            public void onError(Throwable e) {
                errorMessage.setValue("Error autenticando: " + e.getMessage());
            }
        });
    }

    /**
     * Realiza el registro de usuario con Firebase Auth y guarda datos adicionales en Firestore.
     */
    public void register(final String fullName, final String email, final String phone, final String password) {
        Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final SingleEmitter<Boolean> emitter) {
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String uid = (authResult.getUser() != null) ? authResult.getUser().getUid() : null;
                                if (uid == null || uid.isEmpty()) {
                                    emitter.onError(new Exception("Error al obtener UID"));
                                    return;
                                }
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("fullName", fullName);
                                userMap.put("email", email);
                                userMap.put("phone", phone);

                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(uid)
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid -> emitter.onSuccess(true))
                                        .addOnFailureListener(e -> emitter.onError(e));
                            }
                        })
                        .addOnFailureListener(e -> emitter.onError(e));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(Boolean success) {
                registerResult.setValue(success);
            }

            @Override
            public void onError(Throwable e) {
                errorMessage.setValue("Error registrando usuario: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
