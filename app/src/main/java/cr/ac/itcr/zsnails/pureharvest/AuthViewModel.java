/* ============================
   AuthViewModel.java
   ============================ */
package cr.ac.itcr.zsnails.pureharvest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

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
     * Email/Password sign in
     */
    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginResult.setValue(true);
                    } else {
                        loginResult.setValue(false);
                        errorMessage.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Error al autenticar"
                        );
                    }
                });
    }

    /**
     * Email/Password registration
     */
    public void register(String fullName, String email, String phone, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullName", fullName);
                        user.put("email", email);
                        user.put("phone", phone);
                        firestore.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> registerResult.setValue(true))
                                .addOnFailureListener(e -> {
                                    registerResult.setValue(false);
                                    errorMessage.setValue(e.getMessage());
                                });
                    } else {
                        registerResult.setValue(false);
                        errorMessage.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Error al registrar usuario"
                        );
                    }
                });
    }

    /**
     * Sign in with Google or Facebook credential
     */
    public void loginWithCredential(AuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginResult.setValue(true);
                    } else {
                        loginResult.setValue(false);
                        errorMessage.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Error con credencial externa"
                        );
                    }
                });
    }
}
