package cr.ac.itcr.zsnails.pureharvest.ui.companyContact;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyContactBinding;


public class companyContactFragment extends Fragment {

    private static final String TAG = "CompanyContactFragment";

    // Declare binding variable, make it nullable for onDestroyView
    private FragmentCompanyContactBinding binding;

    // Initialize Firestore instance directly
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Store the map address for later use with the button
    private String mapAddressValue;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate using View Binding
        binding = FragmentCompanyContactBinding.inflate(inflater, container, false);
        // Return the root view from binding
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created, starting data fetch.");

        // Set initial loading text (optional, improves UX)
        setLoadingText();

        // Fetch data from Firestore
        fetchCompanyData();

        // Set up the button click listener to open Google Maps
        if (binding != null) {
            binding.mapAdressT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGoogleMaps();
                }
            });
        }
    }

    private void setLoadingText() {
        if (binding == null) return; // View already destroyed
        binding.NameT.setText("Loading...");
        binding.sloganT.setText("Loading...");
        binding.phoneT.setText("Loading...");
        binding.emailT.setText("Loading...");
        binding.adressT.setText("Loading...");
        binding.mapAdressT.setText("Loading...");
    }

    // Method to open Google Maps with the stored address
    private void openGoogleMaps() {
        if (mapAddressValue != null && !mapAddressValue.isEmpty()) {
            try {
                // Create a Uri with the address
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(mapAddressValue));

                // Create an Intent to open Google Maps
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Check if Google Maps is installed
                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // If Google Maps is not installed, open in browser
                    Uri browserUri = Uri.parse("https://maps.google.com/maps?q=" + Uri.encode(mapAddressValue));
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                    startActivity(browserIntent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening map: ", e);
                Toast.makeText(getContext(), "Error opening map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No address available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCompanyData() {
        Log.d(TAG, "fetchCompanyData: Attempting to get document Company/1");

        if (db == null) {
            Log.e(TAG, "fetchCompanyData: Firestore db instance is null!");
            if(getContext() != null) Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();
            setErrorText("DB Error");
            return;
        }


        db.collection("Company").document("1").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "fetchCompanyData: onSuccess triggered.");
                        // Ensure binding is still valid (fragment might be destroyed quickly)
                        if (binding == null) {
                            Log.w(TAG, "fetchCompanyData: Binding became null before success processed.");
                            return;
                        }

                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "fetchCompanyData: Document exists.");

                            // Extract data safely
                            String name = documentSnapshot.getString("name");
                            String slogan = documentSnapshot.getString("slogan");
                            String number = documentSnapshot.getString("number");
                            String email = documentSnapshot.getString("email");
                            mapAddressValue = documentSnapshot.getString("mapAdress"); // Store the map address
                            String address = documentSnapshot.getString("adress");

                            // Update UI using binding
                            binding.NameT.setText(name != null ? name : "Name N/A");
                            binding.sloganT.setText(slogan != null ? slogan : "Slogan N/A");
                            binding.phoneT.setText(number != null ? number : "Phone N/A");
                            binding.emailT.setText(email != null ? email : "Email N/A");
                            binding.adressT.setText(address != null ? address : "Address N/A");
                            binding.mapAdressT.setText(mapAddressValue != null ? mapAddressValue : "Map N/A");

                            // Enable or disable the map button based on address availability
                            binding.mapAdressT.setEnabled(mapAddressValue != null && !mapAddressValue.isEmpty());

                            Log.d(TAG, "fetchCompanyData: UI Updated successfully.");

                        } else {
                            Log.w(TAG, "fetchCompanyData: Document Company/1 does not exist.");
                            setErrorText("Not Found");
                            if(getContext() != null) Toast.makeText(getContext(), "Company information not found.", Toast.LENGTH_SHORT).show();
                            binding.mapAdressT.setEnabled(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log the specific error
                        Log.e(TAG, "fetchCompanyData: onFailure triggered.", e);

                        // Ensure binding is still valid
                        if (binding == null) {
                            Log.w(TAG, "fetchCompanyData: Binding became null before failure processed.");
                            return;
                        }

                        // Show error message to user
                        setErrorText("Error");
                        binding.mapAdressT.setEnabled(false);

                        if(getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        // You might want to check specific exception types
                        if (e instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) e;
                            Log.e(TAG, "Firestore Error Code: " + firestoreEx.getCode());
                            // Specific handling for offline?
                            if (firestoreEx.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE ||
                                    firestoreEx.getMessage().toLowerCase().contains("client is offline")) {
                                setErrorText("Offline");
                                if(getContext() != null) Toast.makeText(getContext(), "Error: Client is offline. Check connection.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    // Helper method to set error text in all fields
    private void setErrorText(String errorType) {
        if (binding == null) return;
        binding.NameT.setText(errorType);
        binding.sloganT.setText(errorType);
        binding.phoneT.setText(errorType);
        binding.emailT.setText(errorType);
        binding.adressT.setText(errorType);
        binding.mapAdressT.setText(errorType);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set binding to null to avoid memory leaks when the Fragment's view is destroyed
        Log.d(TAG, "onDestroyView: Setting binding to null.");
        binding = null;
    }

    //9.911345,-84.152982
}