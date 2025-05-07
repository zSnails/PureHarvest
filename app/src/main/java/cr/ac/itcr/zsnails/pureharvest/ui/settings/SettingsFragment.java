package cr.ac.itcr.zsnails.pureharvest.ui.settings;

import static androidx.core.app.ActivityCompat.recreate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.yariksoffice.lingver.Lingver;

import cr.ac.itcr.zsnails.pureharvest.MainActivity;
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentAccountBinding;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentSettingsBinding;


public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private boolean isFirstSelection = true;

    SharedPreferences prefs;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        String currentLang = prefs.getString("language", "en");

        int selectedIndex = 0;


        if(currentLang.equals("en")){
            selectedIndex = 0;
        } else if(currentLang.equals("es")){
            selectedIndex = 1;
        }

        Spinner lSpinner = root.findViewById(R.id.languagesSpinner);

        String [] languageOptionis = {"English", "Español"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, languageOptionis);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        lSpinner.setAdapter(adapter);

        lSpinner.setSelection(selectedIndex);

        lSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(isFirstSelection){
                    isFirstSelection = false;
                    return;
                }

                String choice = parent.getItemAtPosition(position).toString();
                String lang = "en";
                if(choice.equals("English")){
                    lang = "en";
                } else if(choice.equals("Español")){
                    lang = "es";
                }

                if(!lang.equals(currentLang)){

                    saveLanguagePrefs(lang);
                    changeLanguage(lang);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return root;
    }
    private void changeLanguage(String lang){
        Lingver.getInstance().setLocale(requireContext(), lang);
        requireActivity().recreate();
    }

    private void saveLanguagePrefs(String lang){
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        prefs.edit().putString("language", lang).apply();
    }
}