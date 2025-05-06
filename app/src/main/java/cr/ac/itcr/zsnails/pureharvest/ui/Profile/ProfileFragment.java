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
import android.util.Log; // Import Log
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

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment"; // Added TAG for logging
    private FragmentProfileBinding binding;
    private ImageView imageView;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // This ID will be used for Firestore document and image name
    private static final String COMPANY_ID = "2"; // Variable as originally requested

    // Declare all relevant TextViews as class members
    private TextView nameT;
    private TextView sloganT;
    private TextView phoneT;
    private TextView emailLabelT;
    private TextView emailT;
    private TextView adressT;
    private TextView mapAdressT;

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
        if (getContext() == null || !isAdded()) return;
        // Use COMPANY_ID for the image file name
        String fileName = COMPANY_ID + ".jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);
        Log.d(TAG, "Uploading image: " + fileName + " to companyImages/");

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    if (getContext() != null && isAdded()) {
                        Glide.with(this).load(downloadUri).circleCrop().into(imageView); // Added circleCrop
                        Toast.makeText(requireContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Image upload successful: " + downloadUri.toString());
                    }
                }))
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(requireContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Image upload failed", e);
                    }
                });
    }

    private void loadImageFromFirebase() {
        if (getContext() == null || !isAdded()) return;
        // Use COMPANY_ID for the image file name
        String fileName = COMPANY_ID + ".jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);
        Log.d(TAG, "Loading image: " + fileName + " from companyImages/");

        fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    if (getContext() != null && isAdded()) {
                        Glide.with(this)
                                .load(downloadUri)
                                .placeholder(R.drawable.circle_mask)
                                .error(R.drawable.circle_mask)
                                .circleCrop() // Apply circle crop
                                .into(imageView);
                        Log.d(TAG, "Image loaded successfully: " + downloadUri.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    if(getContext() != null && isAdded()){
                        Glide.with(this)
                                .load(R.drawable.circle_mask)
                                .circleCrop()
                                .into(imageView);
                    }
                    Log.w(TAG, "Failed to load profile image " + fileName + ": " + e.getMessage());
                });
    }

    private void deleteImageFromFirebase() {
        if (getContext() == null || !isAdded()) return;
        // Use COMPANY_ID for the image file name
        String fileName = COMPANY_ID + ".jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);
        Log.d(TAG, "Deleting image: " + fileName + " from companyImages/");

        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null && isAdded()) {
                        Glide.with(this)
                                .load(R.drawable.circle_mask)
                                .circleCrop()
                                .into(imageView);
                        Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Image " + fileName + " deleted successfully.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) {
                        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("object does not exist")) {
                            Glide.with(this)
                                    .load(R.drawable.circle_mask)
                                    .circleCrop()
                                    .into(imageView);
                            Toast.makeText(requireContext(), "Image not found to delete.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Image " + fileName + " not found to delete.");
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        loadImageFromFirebase();
        loadProfileData();

        return root;
    }

    private void loadProfileData() {
        // Use COMPANY_ID to fetch the Firestore document
        Log.d(TAG, "Loading profile data for company ID: " + COMPANY_ID);
        db.collection("Company").document(COMPANY_ID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null || !isAdded()) return;

                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Document " + COMPANY_ID + " found.");
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
                        Log.w(TAG, "Document " + COMPANY_ID + " not found.");
                        handleProfileDataNotFound(); // Make sure this method uses string resources
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile data for " + COMPANY_ID, e);
                    if (getContext() != null && isAdded()) {
                        handleProfileDataLoadError(e); // Make sure this method uses string resources
                    }
                });
    }

    private void handleProfileDataNotFound() {
        if(getContext() == null || !isAdded()) return;
        // Using the original string resource names as provided in the previous code
        nameT.setText(R.string.placeholder_name);
        phoneT.setText(R.string.placeholder_phone);
        adressT.setText(R.string.placeholder_address);
        mapAdressT.setText(R.string.placeholder_map);
        sloganT.setVisibility(View.GONE);
        emailLabelT.setVisibility(View.GONE);
        emailT.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "Profile data not found.", Toast.LENGTH_SHORT).show();
    }

    private void handleProfileDataLoadError(Exception e) {
        if(getContext() == null || !isAdded()) return;
        Toast.makeText(requireContext(), "Error loading profile data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        // Using the original string resource names as provided in the previous code
        nameT.setText(R.string.placeholder_error);
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