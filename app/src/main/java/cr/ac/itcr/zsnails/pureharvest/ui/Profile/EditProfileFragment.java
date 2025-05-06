package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProfileBinding;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private static final String COMPANY_ID = "2";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButtonEdit.setOnClickListener(v -> {

            Navigation.findNavController(v).navigateUp();
        });

        loadCompanyData();

        binding.saveChangesBtn.setOnClickListener(v -> saveCompanyData());

        binding.cancelBtn.setOnClickListener(v -> {

            Navigation.findNavController(v).navigate(R.id.action_editProfileFragment_to_profileFragment);
        });
    }


    private void loadCompanyData() {
        db.collection("Company").document(COMPANY_ID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null || !isAdded() || binding == null) return;

                    if (documentSnapshot.exists()) {
                        binding.nameInput.setText(documentSnapshot.getString("name"));
                        binding.sloganInput.setText(documentSnapshot.getString("slogan"));
                        binding.numberInput.setText(documentSnapshot.getString("number"));
                        binding.emailInput.setText(documentSnapshot.getString("email"));
                        binding.adressInput.setText(documentSnapshot.getString("adress")); // 'address' con dos 'd' en Firestore?
                        binding.mapAdressInput.setText(documentSnapshot.getString("mapAdress")); // 'mapAddress' con dos 'd' en Firestore?
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), getString(R.string.toast_company_data_not_found), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) { // Doble chequeo
                        Toast.makeText(getContext(), getString(R.string.toast_error_loading_profile, e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveCompanyData() {
        if (binding == null) return;

        String name = binding.nameInput.getText().toString().trim();
        String slogan = binding.sloganInput.getText().toString().trim();
        String number = binding.numberInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String address = binding.adressInput.getText().toString().trim();
        String mapAddress = binding.mapAdressInput.getText().toString().trim();


        boolean isValid = true;
        if (TextUtils.isEmpty(name)) {
            binding.nameInput.setError(getString(R.string.error_company_name_required));
            isValid = false;
        } else {
            binding.nameInput.setError(null);
        }


        if (TextUtils.isEmpty(number)) {
            binding.numberInput.setError(getString(R.string.error_phone_number_required));
            isValid = false;
        } else {
            binding.numberInput.setError(null);
        }


        if (TextUtils.isEmpty(address)) {
            binding.adressInput.setError(getString(R.string.error_address_required));
            isValid = false;
        } else {
            binding.adressInput.setError(null);
        }

        if (TextUtils.isEmpty(mapAddress)) {
            binding.mapAdressInput.setError(getString(R.string.error_map_address_required));
            isValid = false;
        } else {
            binding.mapAdressInput.setError(null);
        }

        if (!isValid) {
            if (getContext() != null) { // Doble chequeo
                Toast.makeText(getContext(), getString(R.string.error_fill_required_fields), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Map<String, Object> companyData = new HashMap<>();
        companyData.put("name", name);
        companyData.put("slogan", slogan);
        companyData.put("number", number);
        companyData.put("email", email);
        companyData.put("adress", address);
        companyData.put("mapAdress", mapAddress);

        db.collection("Company").document(COMPANY_ID).update(companyData)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null && isAdded() && getView() != null) {
                        Toast.makeText(getContext(), getString(R.string.toast_profile_updated_successfully), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) { // Doble chequeo
                        Toast.makeText(getContext(), getString(R.string.toast_error_updating_profile, e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}