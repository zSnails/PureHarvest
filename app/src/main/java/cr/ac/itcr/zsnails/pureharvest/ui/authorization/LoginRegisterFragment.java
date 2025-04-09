// LoginRegisterFragment.java
package cr.ac.itcr.zsnails.pureharvest.ui.authorization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentLoginBinding;

public class LoginRegisterFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;
    private boolean showingLogin = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Instanciar el ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Mostrar inicialmente la sección de Login
        showLogin();

        // Observar LiveData del ViewModel
        observeViewModel();

        // Alternar entre Login y Registro
        binding.tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegister();
            }
        });
        binding.tvHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogin();
            }
        });

        // Botón de Login: delega en el ViewModel
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.etEmailLogin.getText().toString().trim();
                String password = binding.etPasswordLogin.getText().toString().trim();
                authViewModel.login(email, password);
            }
        });

        // Botón de Registro: delega en el ViewModel
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = binding.etFullNameRegister.getText().toString().trim();
                String email = binding.etEmailRegister.getText().toString().trim();
                String phone = binding.etPhoneRegister.getText().toString().trim();
                String password = binding.etPasswordRegister.getText().toString().trim();
                String confirm = binding.etConfirmPasswordRegister.getText().toString().trim();

                if (!password.equals(confirm)) {
                    Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }
                authViewModel.register(fullName, email, phone, password);
            }
        });

        // Ejemplo para botones de autenticación social
        binding.btnGoogleLogin.setOnClickListener(v ->
                Toast.makeText(getContext(), "Login con Google", Toast.LENGTH_SHORT).show());
        binding.btnFacebookLogin.setOnClickListener(v ->
                Toast.makeText(getContext(), "Login con Facebook", Toast.LENGTH_SHORT).show());
        binding.btnGoogleRegister.setOnClickListener(v ->
                Toast.makeText(getContext(), "Registro con Google", Toast.LENGTH_SHORT).show());
        binding.btnFacebookRegister.setOnClickListener(v ->
                Toast.makeText(getContext(), "Registro con Facebook", Toast.LENGTH_SHORT).show());
    }

    private void observeViewModel() {
        authViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success != null && success) {
                    Toast.makeText(getContext(), "Login exitoso", Toast.LENGTH_SHORT).show();
                    // Aquí puedes navegar a otra pantalla o realizar otras acciones.
                } else {
                    Toast.makeText(getContext(), "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            }
        });

        authViewModel.getRegisterResult().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success != null && success) {
                    Toast.makeText(getContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                    showLogin();  // Una vez registrado, volvemos al login.
                } else {
                    Toast.makeText(getContext(), "Error en el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogin() {
        showingLogin = true;
        // Sección de Login: se hacen visibles los componentes de login.
        binding.tvLoginTitle.setVisibility(View.VISIBLE);
        binding.tvLoginSubtitle.setVisibility(View.VISIBLE);
        binding.etEmailLogin.setVisibility(View.VISIBLE);
        binding.etPasswordLogin.setVisibility(View.VISIBLE);
        binding.tvForgotPassword.setVisibility(View.VISIBLE);
        binding.btnSignIn.setVisibility(View.VISIBLE);
        binding.tvRegisterLink.setVisibility(View.VISIBLE);
        binding.tvOrContinueWithLogin.setVisibility(View.VISIBLE);
        binding.btnGoogleLogin.setVisibility(View.VISIBLE);
        binding.btnFacebookLogin.setVisibility(View.VISIBLE);

        // Sección de Registro: se ocultan los componentes.
        binding.tvRegisterTitle.setVisibility(View.GONE);
        binding.tvRegisterSubtitle.setVisibility(View.GONE);
        binding.etFullNameRegister.setVisibility(View.GONE);
        binding.etEmailRegister.setVisibility(View.GONE);
        binding.etPhoneRegister.setVisibility(View.GONE);
        binding.etPasswordRegister.setVisibility(View.GONE);
        binding.etConfirmPasswordRegister.setVisibility(View.GONE);
        binding.btnSignUp.setVisibility(View.GONE);
        binding.tvHaveAccount.setVisibility(View.GONE);
        binding.tvOrContinueWithRegister.setVisibility(View.GONE);
        binding.llRegisterSocials.setVisibility(View.GONE);
    }

    private void showRegister() {
        showingLogin = false;
        // Sección de Login: se ocultan los componentes.
        binding.tvLoginTitle.setVisibility(View.GONE);
        binding.tvLoginSubtitle.setVisibility(View.GONE);
        binding.etEmailLogin.setVisibility(View.GONE);
        binding.etPasswordLogin.setVisibility(View.GONE);
        binding.tvForgotPassword.setVisibility(View.GONE);
        binding.btnSignIn.setVisibility(View.GONE);
        binding.tvRegisterLink.setVisibility(View.GONE);
        binding.tvOrContinueWithLogin.setVisibility(View.GONE);
        binding.btnGoogleLogin.setVisibility(View.GONE);
        binding.btnFacebookLogin.setVisibility(View.GONE);

        // Sección de Registro: se hacen visibles los componentes.
        binding.tvRegisterTitle.setVisibility(View.VISIBLE);
        binding.tvRegisterSubtitle.setVisibility(View.VISIBLE);
        binding.etFullNameRegister.setVisibility(View.VISIBLE);
        binding.etEmailRegister.setVisibility(View.VISIBLE);
        binding.etPhoneRegister.setVisibility(View.VISIBLE);
        binding.etPasswordRegister.setVisibility(View.VISIBLE);
        binding.etConfirmPasswordRegister.setVisibility(View.VISIBLE);
        binding.btnSignUp.setVisibility(View.VISIBLE);
        binding.tvHaveAccount.setVisibility(View.VISIBLE);
        binding.tvOrContinueWithRegister.setVisibility(View.VISIBLE);
        binding.llRegisterSocials.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
