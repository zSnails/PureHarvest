// File: cr.ac.itcr.zsnails.pureharvest.ui.Profile.ProfileFragment.java
package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

// Importa MainActivity para acceder a la variable estática
import cr.ac.itcr.zsnails.pureharvest.MainActivity; // Asegúrate que la ruta es correcta
import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private ImageView imageView;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static int PICK_IMAGE_REQUEST;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Ya no necesitas COMPANY_ID aquí si vas a usar la variable global de MainActivity
    // private static final String COMPANY_ID = "2";
    private String companyIdToUse; // Variable para almacenar el ID global

    private TextView nameT;
    private TextView sloganT;
    private TextView phoneT;
    private TextView emailLabelT;
    private TextView emailT;
    private TextView adressT;
    private TextView mapAdressT;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { // Mejor obtenerla en onCreate
        super.onCreate(savedInstanceState);
        companyIdToUse = MainActivity.idGlobalUser;
        PICK_IMAGE_REQUEST = Integer.parseInt(companyIdToUse);
    }


    protected void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        if (getContext() == null || !isAdded() || companyIdToUse == null) return;
        String fileName = companyIdToUse + ".jpg"; // Usa la variable obtenida
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);
        Log.d(TAG, "Uploading image: " + fileName + " to companyImages/");

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    if (getContext() != null && isAdded() && imageView != null) {
                        Glide.with(this).load(downloadUri).circleCrop().into(imageView);
                        Toast.makeText(requireContext(), getString(R.string.toast_upload_successful), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Image upload successful: " + downloadUri.toString());
                    }
                }))
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(requireContext(), getString(R.string.toast_upload_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Image upload failed", e);
                    }
                });
    }

    private void loadImageFromFirebase() {
        if (getContext() == null || !isAdded() || imageView == null || companyIdToUse == null) return;
        String fileName = companyIdToUse + ".jpg"; // Usa la variable obtenida
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);
        Log.d(TAG, "Loading image: " + fileName + " from companyImages/");

        fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    if (getContext() != null && isAdded() && imageView != null) {
                        Glide.with(this)
                                .load(downloadUri)
                                .placeholder(R.drawable.circle_mask)
                                .error(R.drawable.circle_mask)
                                .circleCrop()
                                .into(imageView);
                        Log.d(TAG, "Image loaded successfully: " + downloadUri.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    if(getContext() != null && isAdded() && imageView != null){
                        Glide.with(this)
                                .load(R.drawable.circle_mask)
                                .circleCrop()
                                .into(imageView);
                    }
                    Log.w(TAG, "Failed to load profile image " + fileName + ": " + e.getMessage());
                });
    }

    private void deleteImageFromFirebase() {
        if (getContext() == null || !isAdded() || imageView == null || companyIdToUse == null) return;
        String fileName = companyIdToUse + ".jpg"; // Usa la variable obtenida
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);
        Log.d(TAG, "Deleting image: " + fileName + " from companyImages/");

        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null && isAdded() && imageView != null) {
                        Glide.with(this)
                                .load(R.drawable.circle_mask)
                                .circleCrop()
                                .into(imageView);
                        Toast.makeText(requireContext(), getString(R.string.toast_image_deleted_successfully), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Image " + fileName + " deleted successfully.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded() && imageView != null) {
                        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("object does not exist")) {
                            Glide.with(this)
                                    .load(R.drawable.circle_mask)
                                    .circleCrop()
                                    .into(imageView);
                            Toast.makeText(requireContext(), getString(R.string.toast_image_not_found_to_delete), Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Image " + fileName + " not found to delete.");
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.toast_failed_to_delete_image, e.getMessage()), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to delete image " + fileName, e);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageButton backButton = root.findViewById(R.id.backButtonProfile);
        nameT = root.findViewById(R.id.NameT);
        sloganT = root.findViewById(R.id.sloganT);
        phoneT = root.findViewById(R.id.phoneT);
        emailLabelT = root.findViewById(R.id.emailLabelT);
        emailT = root.findViewById(R.id.emailT);
        adressT = root.findViewById(R.id.adressT);
        mapAdressT = root.findViewById(R.id.mapAdressT);
        imageView = root.findViewById(R.id.profileImagePlaceholder);
        Button addPhotoBtn = root.findViewById(R.id.addPhotoBtn);
        Button deletePhotoBtn = root.findViewById(R.id.deletePhotoBtn);
        Button editProfileBtn = root.findViewById(R.id.editProfileBtn);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        addPhotoBtn.setOnClickListener(v -> openGallery());
        deletePhotoBtn.setOnClickListener(v -> deleteImageFromFirebase());
        editProfileBtn.setOnClickListener(v -> {
            if (getView() != null) {
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
            }
        });


        // Asegurarse que companyIdToUse esté disponible antes de llamar a estos métodos
        if (companyIdToUse != null && !companyIdToUse.isEmpty()) {
            loadImageFromFirebase();
            loadProfileData();
        } else {
            Log.e(TAG, "Company ID from MainActivity is null or empty. Cannot load data.");
            handleProfileDataNotFound(); // O algún otro manejo de error
        }


        return root;
    }

    private void loadProfileData() {
        if (companyIdToUse == null || companyIdToUse.isEmpty()) { // Chequeo adicional
            Log.e(TAG, "Cannot load profile data, companyIdToUse is null or empty.");
            handleProfileDataNotFound(); // Ocultar/mostrar error
            return;
        }
        Log.d(TAG, "Loading profile data for company ID: " + companyIdToUse);
        db.collection("Company").document(companyIdToUse).get() // Usa la variable obtenida
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null || !isAdded() || nameT == null) return;

                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Document " + companyIdToUse + " found.");
                        nameT.setText(documentSnapshot.getString("name"));
                        phoneT.setText(documentSnapshot.getString("number"));
                        adressT.setText(documentSnapshot.getString("adress"));
                        mapAdressT.setText(documentSnapshot.getString("mapAdress"));

                        String slogan = documentSnapshot.getString("slogan");
                        String email = documentSnapshot.getString("email");

                        sloganT.setVisibility(TextUtils.isEmpty(slogan) ? View.GONE : View.VISIBLE);
                        if (!TextUtils.isEmpty(slogan)) {
                            sloganT.setText(slogan);
                        }

                        boolean emailVisible = !TextUtils.isEmpty(email);
                        emailLabelT.setVisibility(emailVisible ? View.VISIBLE : View.GONE);
                        emailT.setVisibility(emailVisible ? View.VISIBLE : View.GONE);
                        if (emailVisible) {
                            emailT.setText(email);
                        }
                    } else {
                        Log.w(TAG, "Document " + companyIdToUse + " not found.");
                        handleProfileDataNotFound();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile data for " + companyIdToUse, e);
                    if (getContext() != null && isAdded()) {
                        handleProfileDataLoadError(e);
                    }
                });
    }

    private void handleProfileDataNotFound() {
        if(getContext() == null || !isAdded() || nameT == null ) return;

        nameT.setText(getString(R.string.placeholder_name_default));
        phoneT.setText(getString(R.string.placeholder_phone_default));
        adressT.setText(getString(R.string.placeholder_address_default));
        mapAdressT.setText(getString(R.string.placeholder_map_default));

        sloganT.setVisibility(View.VISIBLE);
        sloganT.setText(getString(R.string.placeholder_slogan_default));
        emailLabelT.setVisibility(View.VISIBLE);
        emailT.setVisibility(View.VISIBLE);
        emailT.setText(getString(R.string.placeholder_email_default));

        Toast.makeText(requireContext(), getString(R.string.toast_profile_data_not_found_profile), Toast.LENGTH_SHORT).show();
    }

    private void handleProfileDataLoadError(Exception e) {
        if(getContext() == null || !isAdded() || nameT == null) return;
        Toast.makeText(requireContext(), getString(R.string.toast_error_loading_profile_data, e.getMessage()), Toast.LENGTH_LONG).show();

        nameT.setText(getString(R.string.placeholder_error_loading));
        phoneT.setText("");
        adressT.setText("");
        mapAdressT.setText("");
        sloganT.setVisibility(View.GONE);
        emailLabelT.setVisibility(View.GONE);
        emailT.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}