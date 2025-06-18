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

import cr.ac.itcr.zsnails.pureharvest.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
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
        // Inicializar binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar ViewModel
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
        // Email/Password login
        binding.btnSignIn.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String pass  = binding.etPassword.getText().toString();
            authViewModel.login(email, pass);
        });

        // Go to Register screen (puedes reemplazar con tu Activity/Fragment)
        binding.tvRegisterNow.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // Google Sign-In
        binding.btnGoogle.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(intent);
        });

        // Facebook Login
        binding.btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance()
                    .logInWithReadPermissions(this, Arrays.asList("email","public_profile"));
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
                            Toast.makeText(LoginActivity.this,
                                    "FB error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });
    }

    private void observeViewModel() {
        authViewModel.getLoginResult().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error en login", Toast.LENGTH_SHORT).show();
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
