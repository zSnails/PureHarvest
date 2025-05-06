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
import androidx.appcompat.app.AlertDialog; // Para el diálogo de opciones
import androidx.core.content.ContextCompat;
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

import java.util.ArrayList; // Para las opciones del diálogo

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyContactBinding;


public class companyContactFragment extends Fragment {

    private static final String TAG = "CompanyContactFragment";

    private FragmentCompanyContactBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String mapAddressValue;
    private String companyEmailAddress;
    private String companyPhoneNumber; // Para almacenar el número de teléfono

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
        Log.d(TAG, "onViewCreated: View created, starting data fetch.");

        setInitialUIState();
        fetchCompanyData();

        if (binding != null) {
            binding.buttonSeeLocation.setOnClickListener(v -> openMapWithChooser());
            // Los listeners para email y teléfono se configuran en fetchCompanyData
        }
    }

    private void setInitialUIState() {
        if (binding == null) return;
        binding.NameT.setText("Loading...");
        binding.sloganT.setText("Loading...");
        binding.phoneT.setText("Loading..."); // Texto inicial para teléfono
        binding.emailT.setText("Loading...");
        binding.adressT.setText("Loading...");
        binding.buttonSeeLocation.setEnabled(false);

        // Email
        binding.emailT.setOnClickListener(null);
        if (getContext() != null) {
            binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        // Teléfono
        binding.phoneT.setOnClickListener(null);
        if (getContext() != null) {
            binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
    }

    private void openMapWithChooser() {
        // ... (sin cambios)
        if (getContext() == null) return;
        if (mapAddressValue == null || mapAddressValue.trim().isEmpty()) {
            Toast.makeText(getContext(), "No address available to open in map.", Toast.LENGTH_SHORT).show();
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
            if (getContext() != null) Toast.makeText(getContext(), getString(R.string.error_preparing_map) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String recipientEmail) {
        // ... (sin cambios)
        if (getContext() == null) return;
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            Toast.makeText(getContext(), "No email address available.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + recipientEmail));
        try {
            if (emailIntent.resolveActivity(getContext().getPackageManager()) != null) {
                Intent chooser = Intent.createChooser(emailIntent, "Enviar correo con...");
                startActivity(chooser);
            } else {
                Toast.makeText(getContext(), "No se encontró una aplicación de correo.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating email intent: ", e);
            Toast.makeText(getContext(), "Error al preparar el correo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- Nuevos métodos para acciones de teléfono ---
    private void showPhoneOptionsDialog(final String phoneNumber) {
        if (getContext() == null || phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_phone_number_available), Toast.LENGTH_SHORT).show();
            return;
        }

        final ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.action_call));
        options.add(getString(R.string.action_sms));
        if (isWhatsAppInstalled()) {
            options.add(getString(R.string.action_whatsapp));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.contact_via_phone_title));
        builder.setItems(options.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options.get(which);
                if (selectedOption.equals(getString(R.string.action_call))) {
                    dialPhoneNumber(phoneNumber);
                } else if (selectedOption.equals(getString(R.string.action_sms))) {
                    sendSms(phoneNumber);
                } else if (selectedOption.equals(getString(R.string.action_whatsapp))) {
                    sendWhatsAppMessage(phoneNumber);
                }
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void dialPhoneNumber(String phoneNumber) {
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
            Toast.makeText(getContext(), getString(R.string.error_opening_dialer) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String phoneNumber) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
            // Opcional: pre-rellenar mensaje
            // smsIntent.putExtra("sms_body", "Hola, ");
            if (smsIntent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(smsIntent);
            } else {
                Toast.makeText(getContext(), getString(R.string.error_opening_sms), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS: ", e);
            Toast.makeText(getContext(), getString(R.string.error_opening_sms) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendWhatsAppMessage(String phoneNumber) {
        // Limpiar el número: quitar espacios, guiones, paréntesis. Wpp necesita solo dígitos.
        // Y a veces el código de país si no está ya. Asumimos que el número de Firebase es correcto para Wpp.
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");

        PackageManager packageManager = getContext().getPackageManager();
        Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
        String url = "https://api.whatsapp.com/send?phone=" + cleanedNumber; // No necesita Uri.encode para este URL de api.whatsapp
        // El código de país debe estar incluido en cleanedNumber

        try {
            // Intentar con WhatsApp normal primero
            whatsappIntent.setPackage(WHATSAPP_PACKAGE_NAME);
            whatsappIntent.setData(Uri.parse(url));
            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent);
                return; // Éxito con WhatsApp normal
            }

            // Si falla, intentar con WhatsApp Business
            whatsappIntent.setPackage(WHATSAPP_BUSINESS_PACKAGE_NAME);
            // No es necesario cambiar el setData(Uri.parse(url)) ya que la URL es la misma
            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent);
                return; // Éxito con WhatsApp Business
            }

            // Si ambos fallan (no debería pasar si isWhatsAppInstalled fue true)
            Toast.makeText(getContext(), getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error sending WhatsApp message: ", e);
            Toast.makeText(getContext(), getString(R.string.error_opening_whatsapp) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isWhatsAppInstalled() {
        if (getContext() == null) return false;
        PackageManager pm = getContext().getPackageManager();
        try {
            pm.getPackageInfo(WHATSAPP_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // WhatsApp normal no instalado, verificar Business
            try {
                pm.getPackageInfo(WHATSAPP_BUSINESS_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e2) {
                return false; // Ninguno instalado
            }
        }
    }


    private void fetchCompanyData() {
        Log.d(TAG, "fetchCompanyData: Attempting to get document Company/1");

        if (binding != null) {
            binding.buttonSeeLocation.setEnabled(false);
        }

        if (db == null) {
            Log.e(TAG, "fetchCompanyData: Firestore db instance is null!");
            if (getContext() != null) Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();
            setErrorUIState("DB Error");
            return;
        }

        db.collection("Company").document("1").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "fetchCompanyData: onSuccess triggered.");
                        if (binding == null) {
                            Log.w(TAG, "fetchCompanyData: Binding became null before success processed.");
                            return;
                        }

                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "fetchCompanyData: Document exists.");

                            String name = documentSnapshot.getString("name");
                            String slogan = documentSnapshot.getString("slogan");
                            companyPhoneNumber = documentSnapshot.getString("number"); // Guardar número
                            companyEmailAddress = documentSnapshot.getString("email");
                            mapAddressValue = documentSnapshot.getString("mapAdress");
                            String address = documentSnapshot.getString("adress");

                            binding.NameT.setText(name != null ? name : "Name N/A");
                            binding.sloganT.setText(slogan != null ? slogan : "Slogan N/A");
                            binding.adressT.setText(address != null ? address : "Address N/A");

                            // Configurar TextView de Email
                            if (companyEmailAddress != null && !companyEmailAddress.trim().isEmpty()) {
                                binding.emailT.setText(companyEmailAddress);
                                binding.emailT.setOnClickListener(v -> sendEmail(companyEmailAddress));
                                if (getContext() != null) {
                                    binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
                                }
                                binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            } else {
                                binding.emailT.setText("Email N/A");
                                binding.emailT.setOnClickListener(null);
                                if (getContext() != null) {
                                    binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
                                }
                                binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                            }

                            // Configurar TextView de Teléfono
                            if (companyPhoneNumber != null && !companyPhoneNumber.trim().isEmpty()) {
                                binding.phoneT.setText(companyPhoneNumber);
                                binding.phoneT.setOnClickListener(v -> showPhoneOptionsDialog(companyPhoneNumber));
                                // Estilo de enlace para el teléfono
                                if (getContext() != null) {
                                    binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
                                }
                                binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            } else {
                                binding.phoneT.setText("Phone N/A");
                                binding.phoneT.setOnClickListener(null);
                                if (getContext() != null) {
                                    binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
                                }
                                binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                            }

                            binding.buttonSeeLocation.setEnabled(mapAddressValue != null && !mapAddressValue.trim().isEmpty());
                            Log.d(TAG, "fetchCompanyData: UI Updated successfully.");

                        } else {
                            Log.w(TAG, "fetchCompanyData: Document Company/1 does not exist.");
                            setErrorUIState("Not Found");
                            if (getContext() != null)
                                Toast.makeText(getContext(), "Company information not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "fetchCompanyData: onFailure triggered.", e);
                        if (binding == null) {
                            Log.w(TAG, "fetchCompanyData: Binding became null before failure processed.");
                            return;
                        }
                        setErrorUIState("Error");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        if (e instanceof FirebaseFirestoreException) {
                            // ... (manejo de error de Firebase)
                            FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) e;
                            Log.e(TAG, "Firestore Error Code: " + firestoreEx.getCode());
                            if (firestoreEx.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE ||
                                    (firestoreEx.getMessage() != null && firestoreEx.getMessage().toLowerCase().contains("client is offline"))) {
                                setErrorUIState("Offline");
                                if (getContext() != null)
                                    Toast.makeText(getContext(), "Error: Client is offline. Check connection.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void setErrorUIState(String errorType) {
        if (binding == null) return;
        binding.NameT.setText(errorType);
        binding.sloganT.setText(errorType);
        binding.phoneT.setText(errorType); // Teléfono
        binding.emailT.setText(errorType);
        binding.adressT.setText(errorType);
        binding.buttonSeeLocation.setEnabled(false);

        // Email no clickeable
        binding.emailT.setOnClickListener(null);
        if (getContext() != null) {
            binding.emailT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.emailT.setPaintFlags(binding.emailT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        // Teléfono no clickeable
        binding.phoneT.setOnClickListener(null);
        if (getContext() != null) {
            binding.phoneT.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
        }
        binding.phoneT.setPaintFlags(binding.phoneT.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Setting binding to null.");
        binding = null;
    }
}