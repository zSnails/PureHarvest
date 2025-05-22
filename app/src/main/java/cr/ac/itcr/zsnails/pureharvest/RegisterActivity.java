package cr.ac.itcr.zsnails.pureharvest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import cr.ac.itcr.zsnails.pureharvest.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            AuthCredential cred = GoogleAuthProvider
                                    .getCredential(account.getIdToken(), null);
                            // Reutilizamos el mismo método para login con credential
                            authViewModel.loginWithCredential(cred);
                        } catch (ApiException e) {
                            Toast.makeText(this,
                                    "Error Google sign-in: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar Facebook
        callbackManager = CallbackManager.Factory.create();

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Registro tradicional con email/contraseña
        binding.btnSignUp.setOnClickListener(v -> {
            String name    = binding.etName.getText().toString();
            String email   = binding.etEmailReg.getText().toString();
            String phone   = binding.etPhone.getText().toString();
            String pass    = binding.etPasswordReg.getText().toString();
            String confirm = binding.etConfirmReg.getText().toString();
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.register(name, email, phone, pass);
        });

        // Volver al login
        binding.tvHaveAccount.setOnClickListener(v -> finish());

        // Google Sign-In para registro
        binding.btnGoogle.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(intent);
        });

        // Facebook Login para registro
        binding.btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance()
                    .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(
                    callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override public void onSuccess(LoginResult result) {
                            AuthCredential cred = FacebookAuthProvider
                                    .getCredential(result.getAccessToken().getToken());
                            authViewModel.loginWithCredential(cred);
                        }
                        @Override public void onCancel() { }
                        @Override public void onError(FacebookException error) {
                            Toast.makeText(RegisterActivity.this,
                                    "FB error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });
    }

    private void observeViewModel() {
        // Observamos tanto el resultado de register como de loginWithCredential
        authViewModel.getRegisterResult().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish(); // vuelve a LoginActivity
            }
        });
        authViewModel.getLoginResult().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Registro vía social exitoso", Toast.LENGTH_SHORT).show();
                // TODO: navegar a la siguiente pantalla en caso de registro social
            }
        });
        authViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Necesario para Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
