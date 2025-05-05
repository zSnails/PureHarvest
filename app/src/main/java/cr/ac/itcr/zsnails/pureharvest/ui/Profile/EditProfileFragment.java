package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils; // Import TextUtils
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast; // Import Toast for user feedback

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

    // Declare EditText fields at the class level for easier access
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

        // Initialize EditText fields using binding (if using view binding correctly)
        // If not using binding fully, keep findViewById
        nameInput = root.findViewById(R.id.nameInput);
        sloganInput = root.findViewById(R.id.sloganInput);
        numberInput = root.findViewById(R.id.numberInput);
        emailInput = root.findViewById(R.id.emailInput);
        adressInput = root.findViewById(R.id.adressInput); // Corrected ID if necessary
        mapAdressInput = root.findViewById(R.id.mapAdressInput);
        Button saveChangesBtn = root.findViewById(R.id.saveChangesBtn);
        Button cancelBtn = root.findViewById(R.id.cancelBtn);

        // Fetch existing data
        db.collection("Company").document("1").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && getContext() != null) { // Check context != null
                        nameInput.setText(documentSnapshot.getString("name"));
                        sloganInput.setText(documentSnapshot.getString("slogan"));
                        numberInput.setText(documentSnapshot.getString("number"));
                        emailInput.setText(documentSnapshot.getString("email"));
                        adressInput.setText(documentSnapshot.getString("adress")); // Check field name in Firestore
                        mapAdressInput.setText(documentSnapshot.getString("mapAdress")); // Check field name in Firestore
                    }
                }).addOnFailureListener(e -> {
                    // Handle failure to load data, e.g., show a toast
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
                    }
                });

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // --- Input Validation ---
                String name = nameInput.getText().toString().trim();
                String slogan = sloganInput.getText().toString().trim(); // Optional
                String number = numberInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim(); // Optional
                String address = adressInput.getText().toString().trim(); // Correct variable name
                String mapAddress = mapAdressInput.getText().toString().trim(); // Correct variable name

                boolean isValid = true;
                if (TextUtils.isEmpty(name)) {
                    nameInput.setError("Company Name is required");
                    isValid = false;
                }
                if (TextUtils.isEmpty(number)) {
                    numberInput.setError("Phone Number is required");
                    isValid = false;
                }
                if (TextUtils.isEmpty(address)) {
                    adressInput.setError("Address is required"); // Use correct EditText variable
                    isValid = false;
                }
                if (TextUtils.isEmpty(mapAddress)) {
                    mapAdressInput.setError("Map Address is required"); // Use correct EditText variable
                    isValid = false;
                }


                // If any required field is empty, stop processing
                if (!isValid) {
                    Toast.makeText(getContext(), "Please fill all required fields (*)", Toast.LENGTH_SHORT).show();
                    return; // Stop the onClick method here
                }
                // --- End Input Validation ---


                // --- Proceed with saving data if valid ---
                Map<String, Object> companyData = new HashMap<>();
                companyData.put("name", name);
                companyData.put("slogan", slogan); // Save even if empty (optional)
                companyData.put("number", number);
                companyData.put("email", email);   // Save even if empty (optional)
                companyData.put("adress", address); // Correct key and variable
                companyData.put("mapAdress", mapAddress); // Correct key and variable

                // Update Firestore and navigate on success
                db.collection("Company").document("1").update(companyData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                // Navigate back only after successful update
                                Navigation.findNavController(v).navigate(R.id.action_editProfileFragment_to_profileFragment);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        cancelBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_editProfileFragment_to_profileFragment));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up binding
    }
}