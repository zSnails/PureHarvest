package cr.ac.itcr.zsnails.pureharvest.ui.Profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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

import java.util.UUID;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentAccountBinding;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentProfileBinding;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private ImageView imageView;

    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected void openGallery(){
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
        String fileName = "1.jpg"; // Unique filename
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Load image into ImageView
                        Glide.with(requireActivity()).load(downloadUri).into(imageView);
                        Toast.makeText(requireActivity(), "Upload Successful", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadImageFromFirebase() {
        String fileName = "1.jpg"; // Same name you used during upload
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    Glide.with(requireActivity())
                            .load(downloadUri)
                            .into(imageView);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Image not found or failed to load.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteImageFromFirebase() {
        String fileName = "1.jpg"; // Same file ID used when uploading
        StorageReference fileRef = storageReference.child("companyImages/" + fileName);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Clear the ImageView
                    imageView.setImageDrawable(null); // or imageView.setImageResource(R.drawable.placeholder);
                    Toast.makeText(requireActivity(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Button addPhotoBtn = root.findViewById(R.id.addPhotoBtn);
        Button deletePhotoBtn = root.findViewById(R.id.deletePhotoBtn);

        imageView = root.findViewById(R.id.profileImagePlaceholder);

        addPhotoBtn.setOnClickListener(v->openGallery());
        deletePhotoBtn.setOnClickListener(v->deleteImageFromFirebase());

        loadImageFromFirebase();

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