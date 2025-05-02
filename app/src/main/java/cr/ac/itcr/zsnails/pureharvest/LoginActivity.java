/* ============================
   LoginActivity.java
   ============================ */
package cr.ac.itcr.zsnails.pureharvest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> task =
                                    GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                AuthCredential cred = GoogleAuthProvider
                                        .getCredential(account.getIdToken(), null);
                                authViewModel.loginWithCredential(cred);
                            } catch (ApiException e) {
                                Toast.makeText(LoginActivity.this,
                                        "Error Google sign‑in: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        binding = FragmentLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        callbackManager = CallbackManager.Factory.create();

        setupListeners();
        observeViewModel();
        showLogin();
    }

    private void setupListeners() {
        // Email/Password login
        binding.btnSignIn.setOnClickListener(v -> {
            String email = binding.etEmailLogin.getText().toString();
            String pass  = binding.etPasswordLogin.getText().toString();
            authViewModel.login(email, pass);
        });

        // Email/Password registration
        binding.btnSignUp.setOnClickListener(v -> {
            String name     = binding.etFullNameRegister.getText().toString();
            String email    = binding.etEmailRegister.getText().toString();
            String phone    = binding.etPhoneRegister.getText().toString();
            String pass     = binding.etPasswordRegister.getText().toString();
            String confirm  = binding.etConfirmPasswordRegister.getText().toString();
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.register(name, email, phone, pass);
        });

        // Google Sign-In
        binding.btnGoogleLogin.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(intent);
        });
        binding.btnGoogleRegister.setOnClickListener(v -> binding.btnGoogleLogin.performClick());

        // Facebook Login
        binding.btnFacebookLogin.setOnClickListener(v -> {
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
                        @Override public void onCancel() {}
                        @Override public void onError(FacebookException error) {
                            Toast.makeText(LoginActivity.this,
                                    "FB error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });
        binding.btnFacebookRegister.setOnClickListener(v -> binding.btnFacebookLogin.performClick());

        binding.tvRegisterLink.setOnClickListener(v -> showRegister());
        binding.tvHaveAccount.setOnClickListener(v -> showLogin());
    }

    private void observeViewModel() {
        authViewModel.getLoginResult().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();
                // TODO: navegar a la siguiente Activity
            } else {
                Toast.makeText(this, "Error en login", Toast.LENGTH_SHORT).show();
            }
        });
        authViewModel.getRegisterResult().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                showLogin();
            } else {
                Toast.makeText(this, "Error en registro", Toast.LENGTH_SHORT).show();
            }
        });
        authViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLogin() {
        binding.loginSection.setVisibility(View.VISIBLE);
        binding.registerSection.setVisibility(View.GONE);
    }

    private void showRegister() {
        binding.loginSection.setVisibility(View.GONE);
        binding.registerSection.setVisibility(View.VISIBLE);
    }
}