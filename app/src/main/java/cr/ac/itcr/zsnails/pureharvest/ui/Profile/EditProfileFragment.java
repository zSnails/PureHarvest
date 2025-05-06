package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController; // Import NavController
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Import ImageButton
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProfileBinding;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText nameInput;
    private EditText sloganInput;
    private EditText numberInput;
    private EditText emailInput;
    private EditText adressInput;
    private EditText mapAdressInput;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // --- Find Views ---
        ImageButton backButton = root.findViewById(R.id.backButtonEdit); // Find the back button
        nameInput = root.findViewById(R.id.nameInput);
        sloganInput = root.findViewById(R.id.sloganInput);
        numberInput = root.findViewById(R.id.numberInput);
        emailInput = root.findViewById(R.id.emailInput);
        adressInput = root.findViewById(R.id.adressInput);
        mapAdressInput = root.findViewById(R.id.mapAdressInput);
        Button saveChangesBtn = root.findViewById(R.id.saveChangesBtn);
        Button cancelBtn = root.findViewById(R.id.cancelBtn);

        // --- Setup Back Button Listener ---
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp(); // Navigate back up the stack
        });

        // --- Load Existing Data ---
        loadCompanyData(); // Refactored data loading

        // --- Setup Save Button Listener ---
        saveChangesBtn.setOnClickListener(v -> saveCompanyData());

        // --- Setup Cancel Button Listener ---
        // No change needed here, navigateUp() on backButton is usually preferred over a specific action like cancelBtn
        cancelBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_editProfileFragment_to_profileFragment));
        // Consider making Cancel button also just call navigateUp() for consistency:
        // cancelBtn.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return root;
    }

    // --- Helper Method to Load Data ---
    private void loadCompanyData() {
        db.collection("Company").document("1").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null || !isAdded()) return; // Context/Attachment check

                    if (documentSnapshot.exists()) {
                        nameInput.setText(documentSnapshot.getString("name"));
                        sloganInput.setText(documentSnapshot.getString("slogan"));
                        numberInput.setText(documentSnapshot.getString("number"));
                        emailInput.setText(documentSnapshot.getString("email"));
                        adressInput.setText(documentSnapshot.getString("adress"));
                        mapAdressInput.setText(documentSnapshot.getString("mapAdress"));
                    } else {
                        if(getContext() != null) {
                            Toast.makeText(getContext(), "Company data not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), "Error loading profile data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- Helper Method to Save Data ---
    private void saveCompanyData() {
        String name = nameInput.getText().toString().trim();
        String slogan = sloganInput.getText().toString().trim();
        String number = numberInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String address = adressInput.getText().toString().trim();
        String mapAddress = mapAdressInput.getText().toString().trim();

        // --- Input Validation ---
        boolean isValid = true;
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Company Name is required");
            isValid = false;
        } else {
            nameInput.setError(null); // Clear error if valid
        }

        if (TextUtils.isEmpty(number)) {
            numberInput.setError("Phone Number is required");
            isValid = false;
        } else {
            numberInput.setError(null);
        }

        if (TextUtils.isEmpty(address)) {
            adressInput.setError("Address is required");
            isValid = false;
        } else {
            adressInput.setError(null);
        }

        if (TextUtils.isEmpty(mapAddress)) {
            mapAdressInput.setError("Map Address is required");
            isValid = false;
        } else {
            mapAdressInput.setError(null);
        }

        if (!isValid) {
            if(getContext() != null) {
                Toast.makeText(getContext(), "Please fill all required fields (*)", Toast.LENGTH_SHORT).show();
            }
            return; // Stop saving
        }
        // --- End Input Validation ---

        Map<String, Object> companyData = new HashMap<>();
        companyData.put("name", name);
        companyData.put("slogan", slogan);
        companyData.put("number", number);
        companyData.put("email", email);
        companyData.put("adress", address); // Corrected key mapping
        companyData.put("mapAdress", mapAddress);

        // --- Update Firestore ---
        db.collection("Company").document("1").update(companyData)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null && isAdded() && getView() != null) {
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp(); // Navigate back on success
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up binding
    }
}