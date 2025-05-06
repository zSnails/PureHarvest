package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController; // Import NavController
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton; // Import ImageButton
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

    private FragmentProfileBinding binding;
    private ImageView imageView;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        if (getContext() == null || !isAdded()) return; // Prevent crash if fragment is detached/context null
        String fileName = "1.jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    if (getContext() != null && isAdded()) { // Check context/attachment again
                        Glide.with(this).load(downloadUri).into(imageView);
                        Toast.makeText(requireContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
                    }
                }))
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(requireContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadImageFromFirebase() {
        if (getContext() == null || !isAdded()) return;
        String fileName = "1.jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    if (getContext() != null && isAdded()) {
                        Glide.with(this)
                                .load(downloadUri)
                                .placeholder(R.drawable.circle_mask)
                                .error(R.drawable.circle_mask)
                                .into(imageView);
                    }
                })
                .addOnFailureListener(e -> {
                    if(getContext() != null && isAdded()){
                        imageView.setImageResource(R.drawable.circle_mask);
                    }
                    // Only log error, don't toast unless it's unexpected
                    // Log.e("ProfileFragment", "Failed to load profile image", e);
                });
    }

    private void deleteImageFromFirebase() {
        if (getContext() == null || !isAdded()) return;
        String fileName = "1.jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null && isAdded()) {
                        imageView.setImageResource(R.drawable.circle_mask);
                        Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null && isAdded()) {
                        if (e.getMessage() != null && e.getMessage().contains("Object does not exist")) {
                            imageView.setImageResource(R.drawable.circle_mask);
                            Toast.makeText(requireContext(), "Image not found to delete.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // --- Find Views ---
        ImageButton backButton = root.findViewById(R.id.backButtonProfile); // Find Back Button
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

        // --- Initialize Firebase ---
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // --- Setup Button Listeners ---
        backButton.setOnClickListener(v -> {
            // Use Navigation Component to navigate back
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        addPhotoBtn.setOnClickListener(v -> openGallery());
        deletePhotoBtn.setOnClickListener(v -> deleteImageFromFirebase());
        editProfileBtn.setOnClickListener(v -> {
            // Check if view is still available before navigating
            if (getView() != null) {
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
            }
        });

        // --- Load Data ---
        loadImageFromFirebase(); // Load profile image
        loadProfileData(); // Load text data (Refactored)

        return root;
    }

    // --- Refactored Method to Load Profile Data ---
    private void loadProfileData() {
        db.collection("Company").document("1").get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check context and fragment attachment before updating UI
                    if (getContext() == null || !isAdded()) return;

                    if (documentSnapshot.exists()) {
                        // Set mandatory fields
                        nameT.setText(documentSnapshot.getString("name"));
                        phoneT.setText(documentSnapshot.getString("number"));
                        adressT.setText(documentSnapshot.getString("adress"));
                        mapAdressT.setText(documentSnapshot.getString("mapAdress"));

                        // --- Handle Optional Fields (Slogan and Email) ---
                        String slogan = documentSnapshot.getString("slogan");
                        String email = documentSnapshot.getString("email");

                        // Slogan Visibility
                        sloganT.setVisibility(TextUtils.isEmpty(slogan) ? View.GONE : View.VISIBLE);
                        if (!TextUtils.isEmpty(slogan)) {
                            sloganT.setText(slogan);
                        }

                        // Email Visibility (Label and Text)
                        boolean emailVisible = !TextUtils.isEmpty(email);
                        emailLabelT.setVisibility(emailVisible ? View.VISIBLE : View.GONE);
                        emailT.setVisibility(emailVisible ? View.VISIBLE : View.GONE);
                        if (emailVisible) {
                            emailT.setText(email);
                        }
                        // --- End Optional Fields Handling ---

                    } else {
                        // Handle case where the document doesn't exist
                        handleProfileDataNotFound();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to load Firestore data
                    if (getContext() != null && isAdded()) {
                        handleProfileDataLoadError(e);
                    }
                });
    }

    // --- Helper Method for Data Not Found ---
    private void handleProfileDataNotFound() {
        // Ensure context/fragment still valid before accessing resources/views
        if(getContext() == null || !isAdded()) return;
        // Use string resources for placeholders (ensure these exist in strings.xml)
        nameT.setText(R.string.placeholder_name);
        phoneT.setText(R.string.placeholder_phone);
        adressT.setText(R.string.placeholder_address);
        mapAdressT.setText(R.string.placeholder_map);
        sloganT.setVisibility(View.GONE);
        emailLabelT.setVisibility(View.GONE);
        emailT.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "Profile data not found.", Toast.LENGTH_SHORT).show();
    }

    // --- Helper Method for Data Load Error ---
    private void handleProfileDataLoadError(Exception e) {
        // Ensure context/fragment still valid before accessing resources/views
        if(getContext() == null || !isAdded()) return;
        Toast.makeText(requireContext(), "Error loading profile data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        // Use string resource (ensure it exists in strings.xml)
        nameT.setText(R.string.placeholder_error);
        phoneT.setText(""); // Clear potentially sensitive data on error
        adressT.setText("");
        mapAdressT.setText("");
        sloganT.setVisibility(View.GONE);
        emailLabelT.setVisibility(View.GONE);
        emailT.setVisibility(View.GONE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up view binding
    }
}