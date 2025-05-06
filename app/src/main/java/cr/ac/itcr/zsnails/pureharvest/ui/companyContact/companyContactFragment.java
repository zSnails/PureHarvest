package cr.ac.itcr.zsnails.pureharvest.ui.companyContact;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyContactBinding;


public class companyContactFragment extends Fragment {

    private static final String TAG = "CompanyContactFragment";

    private FragmentCompanyContactBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private String mapAddressValue;
    private String companyEmailAddress;
    private String companyPhoneNumber;


    private static final String COMPANY_ID_FOR_IMAGE = "2";
    private static final String COMPANY_IMAGE_FOLDER_IN_STORAGE = "companyImages";
    private static final String COMPANY_IMAGE_FILE_EXTENSION = ".jpg";

    private static final String WHATSAPP_PACKAGE_NAME = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE_NAME = "com.whatsapp.w4b";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompanyContactBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created, starting data fetch for company ID: " + COMPANY_ID_FOR_IMAGE);

        setInitialUIState();
        fetchCompanyDataAndImage();

        if (binding != null) {
            binding.buttonSeeLocation.setOnClickListener(v -> openMapWithChooser());
        }
    }

    private void setInitialUIState() {
        if (binding == null) return;
        binding.NameT.setText("Loading...");
        binding.sloganT.setText("Loading...");
        binding.phoneT.setText("Loading...");
        binding.emailT.setText("Loading...");
        binding.adressT.setText("Loading...");
        binding.buttonSeeLocation.setEnabled(false);

        binding.sloganT.setVisibility(View.VISIBLE);
        binding.emailT.setVisibility(View.VISIBLE);

        binding.emailT.setOnClickListener(null);
        if (getContext() != null) {
            binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        binding.phoneT.setOnClickListener(null);
        if (getContext() != null) {
            binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        if (getContext() != null && binding.profileImagePlaceholder != null) {
            Glide.with(requireContext())
                    .load(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(binding.profileImagePlaceholder);
        }
    }

    private void openMapWithChooser() {
        if (getContext() == null) return;
        if (mapAddressValue == null || mapAddressValue.trim().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_address_available_map), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(mapAddressValue));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                Intent chooser = Intent.createChooser(mapIntent, getString(R.string.open_location_with));
                startActivity(chooser);
            } else {
                Toast.makeText(getContext(), getString(R.string.no_map_app_found), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating map intent: ", e);
            if (getContext() != null) Toast.makeText(getContext(), getString(R.string.error_preparing_map_detail, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String recipientEmail) {
        if (getContext() == null) return;
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_email_address_available), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + recipientEmail));
        try {
            if (emailIntent.resolveActivity(getContext().getPackageManager()) != null) {
                Intent chooser = Intent.createChooser(emailIntent, getString(R.string.send_email_with_chooser_title));
                startActivity(chooser);
            } else {
                Toast.makeText(getContext(), getString(R.string.no_email_app_found), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating email intent: ", e);
            Toast.makeText(getContext(), getString(R.string.error_preparing_email_detail, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhoneOptionsDialog(final String phoneNumber) {
        if (getContext() == null || phoneNumber == null || phoneNumber.trim().isEmpty()) {
            // Ya usa getString para el Toast, asegurándose de que está traducido.
            Toast.makeText(getContext(), getString(R.string.no_phone_number_available), Toast.LENGTH_SHORT).show();
            return;
        }

        final ArrayList<String> options = new ArrayList<>();
        // Obtener opciones de strings.xml para permitir la localización
        final String callOptionText = getString(R.string.action_call);
        final String smsOptionText = getString(R.string.action_sms);

        options.add(callOptionText);
        options.add(smsOptionText);

        if (isWhatsAppInstalled()) {
            // Esta opción ya se obtiene de strings.xml
            options.add(getString(R.string.action_whatsapp));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        // El título del diálogo se obtiene de strings.xml
        builder.setTitle(getString(R.string.contact_via_phone_title));

        builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
            String selectedOption = options.get(which);
            // La comparación también usa las cadenas obtenidas de getString
            if (selectedOption.equals(callOptionText)) {
                dialPhoneNumber(phoneNumber);
            } else if (selectedOption.equals(smsOptionText)) {
                sendSms(phoneNumber);
            } else if (isWhatsAppInstalled() && selectedOption.equals(getString(R.string.action_whatsapp))) {
                // getString(R.string.action_whatsapp) fue lo que se añadió a 'options'
                sendWhatsAppMessage(phoneNumber);
            }
        });
        // El botón negativo ya se obtiene de strings.xml
        builder.setNegativeButton(getString(R.string.dialog_action_cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void dialPhoneNumber(String phoneNumber) {
        if (getContext() == null) return;
        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
            if (dialIntent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(dialIntent);
            } else {
                Toast.makeText(getContext(), getString(R.string.error_opening_dialer), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error dialing phone: ", e);
            if (getContext() != null) Toast.makeText(getContext(), getString(R.string.error_opening_dialer_detail, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String phoneNumber) {
        if (getContext() == null) return;
        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
            if (smsIntent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(smsIntent);
            } else {
                Toast.makeText(getContext(), getString(R.string.error_opening_sms), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS: ", e);
            if (getContext() != null) Toast.makeText(getContext(), getString(R.string.error_opening_sms_detail, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendWhatsAppMessage(String phoneNumber) {
        if (getContext() == null) return;
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");
        PackageManager packageManager = getContext().getPackageManager();
        Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
        String url = "https://api.whatsapp.com/send?phone=" + cleanedNumber;
        try {
            whatsappIntent.setPackage(WHATSAPP_PACKAGE_NAME);
            whatsappIntent.setData(Uri.parse(url));
            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent);
                return;
            }
            whatsappIntent.setPackage(WHATSAPP_BUSINESS_PACKAGE_NAME);
            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent);
                return;
            }
            Toast.makeText(getContext(), getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error sending WhatsApp message: ", e);
            Toast.makeText(getContext(), getString(R.string.error_opening_whatsapp_detail, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isWhatsAppInstalled() {
        if (getContext() == null) return false;
        PackageManager pm = getContext().getPackageManager();
        try {
            pm.getPackageInfo(WHATSAPP_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            try {
                pm.getPackageInfo(WHATSAPP_BUSINESS_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e2) {
                return false;
            }
        }
    }

    private void loadCompanyImageFromFirebase() {
        if (getContext() == null || !isAdded() || binding == null || binding.profileImagePlaceholder == null) {
            Log.w(TAG, "loadCompanyImageFromFirebase: Cannot load image - context, fragment not added, or view is null.");
            return;
        }

        String fileNameWithExtension = COMPANY_ID_FOR_IMAGE + COMPANY_IMAGE_FILE_EXTENSION;
        StorageReference fileRef = storage.getReference().child(COMPANY_IMAGE_FOLDER_IN_STORAGE + "/" + fileNameWithExtension);

        Log.d(TAG, "Attempting to load image from Storage path: " + fileRef.getPath());

        fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    if (getContext() != null && isAdded() && binding != null && binding.profileImagePlaceholder != null) {
                        Glide.with(companyContactFragment.this)
                                .load(downloadUri)
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher_round)
                                .circleCrop()
                                .into(binding.profileImagePlaceholder);
                        Log.d(TAG, "Image loaded successfully from URL: " + downloadUri.toString());
                    } else {
                        Log.w(TAG, "loadCompanyImageFromFirebase: onSuccess - Context, fragment, or view became null.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load company image from " + fileRef.getPath(), e);
                    if (getContext() != null && isAdded() && binding != null && binding.profileImagePlaceholder != null) {
                        Glide.with(companyContactFragment.this)
                                .load(R.mipmap.ic_launcher_round) // Fallback image
                                .circleCrop()
                                .into(binding.profileImagePlaceholder);
                    }
                });
    }

    private void fetchCompanyDataAndImage() {
        Log.d(TAG, "fetchCompanyDataAndImage: Starting data fetch for company ID: " + COMPANY_ID_FOR_IMAGE);
        if (binding != null) {
            binding.buttonSeeLocation.setEnabled(false);
        }

        loadCompanyImageFromFirebase();

        if (db == null) {
            Log.e(TAG, "fetchCompanyDataAndImage: Firestore db instance is null!");
            if (getContext() != null) Toast.makeText(getContext(), getString(R.string.database_error), Toast.LENGTH_SHORT).show();
            setErrorUIState("DB Error");
            return;
        }

        db.collection("Company").document(COMPANY_ID_FOR_IMAGE).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "fetchCompanyDataAndImage: Firestore onSuccess triggered for company ID: " + COMPANY_ID_FOR_IMAGE);
                    if (binding == null || !isAdded() || getContext() == null) {
                        Log.w(TAG, "fetchCompanyDataAndImage: Binding, Context became null or Fragment not added.");
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "fetchCompanyDataAndImage: Document 'Company/" + COMPANY_ID_FOR_IMAGE + "' exists.");
                        String name = documentSnapshot.getString("name");
                        String slogan = documentSnapshot.getString("slogan");
                        companyPhoneNumber = documentSnapshot.getString("number");
                        mapAddressValue = documentSnapshot.getString("mapAdress");
                        String address = documentSnapshot.getString("adress");

                        String rawEmailFromFirestore = documentSnapshot.getString("email");
                        Log.d(TAG, "Raw companyEmailAddress from Firestore: [" + rawEmailFromFirestore + "]");
                        companyEmailAddress = rawEmailFromFirestore;

                        binding.NameT.setText(name != null ? name : "Name N/A");
                        binding.adressT.setText(address != null ? address : "Address N/A");

                        if (slogan != null && !slogan.trim().isEmpty()) {
                            binding.sloganT.setText(slogan);
                            binding.sloganT.setVisibility(View.VISIBLE);
                        } else {
                            binding.sloganT.setText("");
                            binding.sloganT.setVisibility(View.GONE);
                        }

                        Log.d(TAG, "Processing Email. Current value of companyEmailAddress: [" + companyEmailAddress + "]");
                        if (companyEmailAddress != null && !companyEmailAddress.trim().isEmpty()) {
                            Log.d(TAG, "Email has data. Setting VISIBLE.");
                            binding.emailT.setText(companyEmailAddress);
                            binding.emailT.setOnClickListener(v -> sendEmail(companyEmailAddress));
                            binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), R.color.link_blue));
                            binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            binding.emailT.setVisibility(View.VISIBLE);
                        } else {
                            Log.d(TAG, "Email is null or empty. Setting GONE.");
                            binding.emailT.setText("");
                            binding.emailT.setOnClickListener(null);
                            if (isAdded() && getContext() != null) {
                                binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
                                binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                            }
                            binding.emailT.setVisibility(View.GONE);
                        }

                        if (companyPhoneNumber != null && !companyPhoneNumber.trim().isEmpty()) {
                            binding.phoneT.setText(companyPhoneNumber);
                            binding.phoneT.setOnClickListener(v -> showPhoneOptionsDialog(companyPhoneNumber));
                            binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), R.color.link_blue));
                            binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            binding.phoneT.setVisibility(View.VISIBLE);
                        } else {
                            binding.phoneT.setText("Phone N/A");
                            binding.phoneT.setOnClickListener(null);
                            binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
                            binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                            binding.phoneT.setVisibility(View.VISIBLE);
                        }
                        binding.buttonSeeLocation.setEnabled(mapAddressValue != null && !mapAddressValue.trim().isEmpty());
                        Log.d(TAG, "fetchCompanyDataAndImage: Firestore UI Updated successfully for company ID: " + COMPANY_ID_FOR_IMAGE);
                    } else {
                        Log.w(TAG, "fetchCompanyDataAndImage: Document 'Company/" + COMPANY_ID_FOR_IMAGE + "' does not exist.");
                        setErrorUIState("Not Found");
                        if (getContext() != null)
                            Toast.makeText(getContext(), getString(R.string.company_info_not_found), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchCompanyDataAndImage: Firestore onFailure triggered for company ID: " + COMPANY_ID_FOR_IMAGE, e);
                    if (binding == null) {
                        Log.w(TAG, "fetchCompanyDataAndImage: Binding became null on failure.");
                        return;
                    }
                    setErrorUIState("Error");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.error_fetching_data_detail, e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) e;
                        Log.e(TAG, "Firestore Error Code: " + firestoreEx.getCode());
                        if (firestoreEx.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE ||
                                (firestoreEx.getMessage() != null && firestoreEx.getMessage().toLowerCase().contains("client is offline"))) {
                            setErrorUIState("Offline");
                            if (getContext() != null)
                                Toast.makeText(getContext(), getString(R.string.client_offline_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setErrorUIState(String errorType) {
        if (binding == null) return;
        String errorMsg = errorType + " (ID: " + COMPANY_ID_FOR_IMAGE + ")";
        binding.NameT.setText(errorMsg);
        binding.sloganT.setText(errorMsg);
        binding.phoneT.setText(errorMsg);
        binding.emailT.setText(errorMsg);
        binding.adressT.setText(errorMsg);
        binding.buttonSeeLocation.setEnabled(false);

        binding.sloganT.setVisibility(View.VISIBLE);
        binding.emailT.setVisibility(View.VISIBLE);
        binding.phoneT.setVisibility(View.VISIBLE);

        binding.emailT.setOnClickListener(null);
        if (getContext() != null) {
            binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        binding.phoneT.setOnClickListener(null);
        if (getContext() != null) {
            binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        if (getContext() != null && binding.profileImagePlaceholder != null) {
            Glide.with(requireContext())
                    .load(R.mipmap.ic_launcher_round)
                    .circleCrop()
                    .into(binding.profileImagePlaceholder);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Setting binding to null.");
        if (binding != null && binding.profileImagePlaceholder != null && getContext() != null) {
            if(isAdded() && getActivity() != null && !getActivity().isFinishing()){
                try {
                    Glide.with(requireContext()).clear(binding.profileImagePlaceholder);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Glide clear failed in onDestroyView, context might be invalid or fragment detached.", e);
                }
            }
        }
        binding = null;
    }
}