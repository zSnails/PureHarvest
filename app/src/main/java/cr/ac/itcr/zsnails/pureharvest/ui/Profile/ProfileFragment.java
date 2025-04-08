package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentAccountBinding;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentProfileBinding;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView nameT = root.findViewById(R.id.NameT);
        TextView sloganT = root.findViewById(R.id.sloganT);
        TextView phoneT = root.findViewById(R.id.phoneT);
        TextView emailT = root.findViewById(R.id.emailT);
        TextView adressT = root.findViewById(R.id.adressT);
        TextView mapAdressT = root.findViewById(R.id.mapAdressT);

        db.collection("Company").document("1").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        nameT.setText(documentSnapshot.getString("name"));
                        sloganT.setText(documentSnapshot.getString("slogan"));
                        phoneT.setText(documentSnapshot.getString("number"));
                        emailT.setText(documentSnapshot.getString("email"));
                        adressT.setText(documentSnapshot.getString("adress"));
                        mapAdressT.setText(documentSnapshot.getString("mapAdress"));
                    }
                });

        Button editProfileBtn = root.findViewById(R.id.editProfileBtn);

        editProfileBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment));

        return root;
    }
}