package cr.ac.itcr.zsnails.pureharvest;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import cr.ac.itcr.zsnails.pureharvest.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
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

        binding.tvHaveAccount.setOnClickListener(v -> {
            // Regresa al login
            finish();
        });
    }

    private void observeViewModel() {
        authViewModel.getRegisterResult().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish(); // vuelve a LoginActivity
            } else {
                Toast.makeText(this, "Error in registration", Toast.LENGTH_SHORT).show();
            }
        });
        authViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
