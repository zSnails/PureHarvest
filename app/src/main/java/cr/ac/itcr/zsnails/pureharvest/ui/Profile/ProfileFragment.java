package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentAccountBinding;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentProfileBinding;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button editProfileBtn = root.findViewById(R.id.editProfileBtn);

        editProfileBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment));

        return root;
    }
}