package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentEditProfileBinding;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Inflate the layout for this fragment

        EditText nameInput = root.findViewById(R.id.nameInput);
        EditText sloganInput = root.findViewById(R.id.sloganInput);
        EditText numberInput = root.findViewById(R.id.numberInput);
        EditText emailInput = root.findViewById(R.id.emailInput);
        EditText adressInput = root.findViewById(R.id.adressInput);
        EditText mapAdressInput = root.findViewById(R.id.mapAdressInput);
        Button saveChangesBtn = root.findViewById(R.id.saveChangesBtn);


        db.collection("Company").document("1").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        nameInput.setText(documentSnapshot.getString("name"));
                        sloganInput.setText(documentSnapshot.getString("slogan"));
                        numberInput.setText(documentSnapshot.getString("number"));
                        emailInput.setText(documentSnapshot.getString("email"));
                        adressInput.setText(documentSnapshot.getString("adress"));
                        mapAdressInput.setText(documentSnapshot.getString("mapAdress"));
                    }
                });



        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameInput.getText().toString();
                String slogan = sloganInput.getText().toString();
                String number = numberInput.getText().toString();
                String email = emailInput.getText().toString();
                String adress = adressInput.getText().toString();
                String mapAdress = mapAdressInput.getText().toString();

                Map<String, Object> companyData = new HashMap<>();

                companyData.put("name", name);
                companyData.put("slogan", slogan);
                companyData.put("number", number);
                companyData.put("email", email);
                companyData.put("adress", adress);
                companyData.put("mapAdress", mapAdress);

                db.collection("Company").document("1").set(companyData);

            }
        });

        return root;
    }
}