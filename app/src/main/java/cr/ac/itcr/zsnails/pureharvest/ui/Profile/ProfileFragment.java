package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull; // Import NonNull
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils; // Import TextUtils
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

// Remove unused import if FragmentAccountBinding is not used here
// import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentAccountBinding;
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

    // Declare TextViews for optional fields here for easier access
    private TextView sloganT;
    private TextView emailLabelT;
    private TextView emailT;

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
        if (getContext() == null) return; // Prevent crash if fragment is detached
        String fileName = "1.jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    if (getContext() != null) { // Check context again for safety
                        Glide.with(this).load(downloadUri).into(imageView);
                        Toast.makeText(requireContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
                    }
                }))
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadImageFromFirebase() {
        if (getContext() == null) return;
        String fileName = "1.jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    if (getContext() != null) {
                        Glide.with(this)
                                .load(downloadUri)
                                .placeholder(R.drawable.circle_mask) // Optional: Add a placeholder
                                .error(R.drawable.circle_mask) // Optional: Add an error image
                                .into(imageView);
                    }
                })
                .addOnFailureListener(e -> {
                    // Don't show a toast if the image simply doesn't exist yet
                    // Only log or show toast for actual errors if needed
                    imageView.setImageResource(R.drawable.circle_mask); // Set placeholder if load fails
                    // Toast.makeText(requireContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteImageFromFirebase() {
        if (getContext() == null) return;
        String fileName = "1.jpg";
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        imageView.setImageResource(R.drawable.circle_mask); // Set placeholder after deletion
                        Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        // Handle case where file might not exist (maybe already deleted)
                        if (e.getMessage() != null && e.getMessage().contains("Object does not exist")) {
                            imageView.setImageResource(R.drawable.circle_mask); // Still set placeholder
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
        TextView nameT = root.findViewById(R.id.NameT);
        sloganT = root.findViewById(R.id.sloganT); // Assign to class variable
        TextView phoneT = root.findViewById(R.id.phoneT);
        emailLabelT = root.findViewById(R.id.emailLabelT); // Assign to class variable
        emailT = root.findViewById(R.id.emailT); // Assign to class variable
        TextView adressT = root.findViewById(R.id.adressT);
        TextView mapAdressT = root.findViewById(R.id.mapAdressT);
        imageView = root.findViewById(R.id.profileImagePlaceholder);
        Button addPhotoBtn = root.findViewById(R.id.addPhotoBtn);
        Button deletePhotoBtn = root.findViewById(R.id.deletePhotoBtn);
        Button editProfileBtn = root.findViewById(R.id.editProfileBtn);

        // --- Initialize Firebase ---
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // --- Setup Button Listeners ---
        addPhotoBtn.setOnClickListener(v -> openGallery());
        deletePhotoBtn.setOnClickListener(v -> deleteImageFromFirebase());
        editProfileBtn.setOnClickListener(v -> {
            if (getView() != null) { // Ensure view is available for navigation
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
            }
        });

        // --- Load Data ---
        loadImageFromFirebase(); // Load profile image

        db.collection("Company").document("1").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null) return; // Check context before accessing views

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
                        if (TextUtils.isEmpty(slogan)) {
                            sloganT.setVisibility(View.GONE); // Hide if empty
                        } else {
                            sloganT.setText(slogan);
                            sloganT.setVisibility(View.VISIBLE); // Show if not empty
                        }

                        // Email Visibility (Label and Text)
                        if (TextUtils.isEmpty(email)) {
                            emailLabelT.setVisibility(View.GONE); // Hide label if empty
                            emailT.setVisibility(View.GONE);     // Hide text if empty
                        } else {
                            emailT.setText(email);
                            emailLabelT.setVisibility(View.VISIBLE); // Show label if not empty
                            emailT.setVisibility(View.VISIBLE);     // Show text if not empty
                        }
                        // --- End Optional Fields Handling ---

                    } else {
                        // Handle case where the document doesn't exist (e.g., show placeholder text or error)
                        nameT.setText(R.string.placeholder_name); // Use string resources
                        phoneT.setText(R.string.placeholder_phone);
                        adressT.setText(R.string.placeholder_address);
                        mapAdressT.setText(R.string.placeholder_map);
                        sloganT.setVisibility(View.GONE);
                        emailLabelT.setVisibility(View.GONE);
                        emailT.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Profile data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    // Handle failure to load Firestore data
                    Toast.makeText(requireContext(), "Error loading profile data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Optionally hide or show placeholder text on failure too
                    nameT.setText(R.string.placeholder_error);
                    phoneT.setText("");
                    adressT.setText("");
                    mapAdressT.setText("");
                    sloganT.setVisibility(View.GONE);
                    emailLabelT.setVisibility(View.GONE);
                    emailT.setVisibility(View.GONE);
                });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up view binding
    }
}