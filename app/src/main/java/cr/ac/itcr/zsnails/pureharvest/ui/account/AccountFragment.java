package cr.ac.itcr.zsnails.pureharvest.ui.account;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import cr.ac.itcr.zsnails.pureharvest.LoginActivity;
import cr.ac.itcr.zsnails.pureharvest.R;

public class AccountFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnLogout = view.findViewById(R.id.btn_logout);
        Button btnEditInfo = view.findViewById(R.id.btn_edit_user_info);
        Button btnViewFavoriteItems = view.findViewById(R.id.btn_view_favorite_items);

        if (user != null) {
            btnLogin.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
            btnEditInfo.setVisibility(View.VISIBLE);
            btnViewFavoriteItems.setVisibility(View.VISIBLE);

            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                requireActivity().recreate();
            });

            btnEditInfo.setOnClickListener(v -> {
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                        String name = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        // Show a simple dialog to edit info (name, email, phone)
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Editar información");
                        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user_info, null);
                        EditText etName = dialogView.findViewById(R.id.et_edit_name);
                        EditText etEmail = dialogView.findViewById(R.id.et_edit_email);
                        EditText etPhone = dialogView.findViewById(R.id.et_edit_phone);
                        etName.setText(name != null ? name : "");
                        etEmail.setText(email != null ? email : "");
                        etPhone.setText(phone != null ? phone : "");
                        builder.setView(dialogView);
                        builder.setPositiveButton("Guardar", (dialog, which) -> {
                            String newName = etName.getText().toString().trim();
                            String newEmail = etEmail.getText().toString().trim();
                            String newPhone = etPhone.getText().toString().trim();
                            db.collection("users").document(user.getUid())
                                    .update("fullName", newName, "email", newEmail, "phone", newPhone)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Información actualizada", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show());
                        });
                        builder.setNegativeButton("Cancelar", null);
                        builder.show();
                    });
                }
            });

            btnViewFavoriteItems.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_navigation_account_to_favoriteItemsFragment);
            });
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            btnEditInfo.setVisibility(View.GONE);
            btnViewFavoriteItems.setVisibility(View.GONE);

            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
            });
        }
    }
}
