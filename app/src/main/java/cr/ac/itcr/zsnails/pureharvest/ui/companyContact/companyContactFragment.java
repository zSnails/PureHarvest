package cr.ac.itcr.zsnails.pureharvest.ui.companyContact;

import android.content.Context; // Importado
import android.content.Intent;
import android.content.pm.PackageManager; // Importado
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

    private FragmentCompanyContactBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String mapAddressValue;

    // Define package names as constants
    private static final String GOOGLE_MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final String WAZE_PACKAGE_NAME = "com.waze";


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

        setLoadingText(); // Manejará ambos botones
        fetchCompanyData(); // Manejará ambos botones

        if (binding != null) {
            // Listener para el botón de Google Maps (mapAdressT)
            binding.mapAdressT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGoogleMaps(); // Método original
                }
            });

            // Listener para el nuevo botón de Waze (buttonOpenInWaze)
            binding.buttonOpenInWaze.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWaze(); // Nuevo método
                }
            });
        }
    }

    private void setLoadingText() {
        if (binding == null) return;
        binding.NameT.setText("Loading...");
        binding.sloganT.setText("Loading...");
        binding.phoneT.setText("Loading...");
        binding.emailT.setText("Loading...");
        binding.adressT.setText("Loading...");

        // Establecer texto de carga para ambos botones
        binding.mapAdressT.setText("Loading...");
        binding.buttonOpenInWaze.setText("Loading...");
    }

    // Método para abrir Google Maps (tu versión original)
    private void openGoogleMaps() {
        if (mapAddressValue != null && !mapAddressValue.isEmpty()) {
            try {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(mapAddressValue));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage(GOOGLE_MAPS_PACKAGE_NAME); // Clave para Google Maps

                // Usar requireActivity() como en tu original
                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Fallback al navegador (como en tu original)
                    // Se recomienda añadir un Toast aquí para informar al usuario
                    Toast.makeText(getContext(), "Google Maps no está instalado. Intentando abrir en el navegador...", Toast.LENGTH_LONG).show();
                    Uri browserUri = Uri.parse("https://maps.google.com/maps?q=" + Uri.encode(mapAddressValue));
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                    // Es buena práctica verificar también el intent del navegador
                    if (browserIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(getContext(), "No se encontró una aplicación para abrir la ubicación.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening Google Maps: ", e);
                if (getContext() != null) Toast.makeText(getContext(), "Error al abrir Google Maps: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (getContext() != null) Toast.makeText(getContext(), "Dirección no disponible para Google Maps", Toast.LENGTH_SHORT).show();
        }
    }

    // Nuevo método para abrir Waze
    private void openWaze() {
        if (getContext() == null) return; // Siempre verificar contexto

        if (mapAddressValue != null && !mapAddressValue.isEmpty()) {
            if (!isPackageInstalled(WAZE_PACKAGE_NAME, getContext())) {
                Toast.makeText(getContext(), "Waze no está instalado.", Toast.LENGTH_LONG).show();
                // Opcional: dirigir al Play Store
                // try {
                //     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + WAZE_PACKAGE_NAME)));
                // } catch (android.content.ActivityNotFoundException anfe) {
                //     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + WAZE_PACKAGE_NAME)));
                // }
                return;
            }
            try {
                String wazeUriString = "waze://?q=" + Uri.encode(mapAddressValue) + "&navigate=yes";
                Intent wazeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wazeUriString));
                // No es estrictamente necesario setPackage para esquemas de URI personalizados si la app está instalada
                // pero no hace daño si se quiere ser explícito.
                // wazeIntent.setPackage(WAZE_PACKAGE_NAME);

                if (wazeIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(wazeIntent);
                } else {
                    // Esto no debería ocurrir si isPackageInstalled() fue verdadero y el esquema es correcto
                    Toast.makeText(getContext(), "No se pudo iniciar Waze.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening Waze: ", e);
                Toast.makeText(getContext(), "Error al abrir Waze: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Dirección no disponible para Waze", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper para verificar si un paquete está instalado
    private boolean isPackageInstalled(String packageName, Context context) {
        if (context == null) return false;
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES); // GET_ACTIVITIES es común, 0 también funciona
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void fetchCompanyData() {
        Log.d(TAG, "fetchCompanyData: Attempting to get document Company/1");

        if (db == null) {
            Log.e(TAG, "fetchCompanyData: Firestore db instance is null!");
            if (getContext() != null) Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();
            setErrorText("DB Error");
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
                            String number = documentSnapshot.getString("number");
                            String email = documentSnapshot.getString("email");
                            mapAddressValue = documentSnapshot.getString("mapAdress");
                            String address = documentSnapshot.getString("adress");

                            binding.NameT.setText(name != null ? name : "Name N/A");
                            binding.sloganT.setText(slogan != null ? slogan : "Slogan N/A");
                            binding.phoneT.setText(number != null ? number : "Phone N/A");
                            binding.emailT.setText(email != null ? email : "Email N/A");
                            binding.adressT.setText(address != null ? address : "Address N/A");

                            String displayMapAddress = (mapAddressValue != null && !mapAddressValue.isEmpty()) ? mapAddressValue : "Map N/A";
                            binding.mapAdressT.setText(displayMapAddress);
                            binding.buttonOpenInWaze.setText(displayMapAddress); // Aplicar a ambos

                            boolean hasMapAddress = mapAddressValue != null && !mapAddressValue.isEmpty();
                            binding.mapAdressT.setEnabled(hasMapAddress);
                            binding.buttonOpenInWaze.setEnabled(hasMapAddress); // Aplicar a ambos

                            Log.d(TAG, "fetchCompanyData: UI Updated successfully.");

                        } else {
                            Log.w(TAG, "fetchCompanyData: Document Company/1 does not exist.");
                            setErrorText("Not Found");
                            if (getContext() != null)
                                Toast.makeText(getContext(), "Company information not found.", Toast.LENGTH_SHORT).show();
                            if (binding != null) { // Asegurar que binding no es null
                                binding.mapAdressT.setEnabled(false);
                                binding.buttonOpenInWaze.setEnabled(false);
                            }
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
                        setErrorText("Error");
                        if (binding != null) { // Asegurar que binding no es null
                            binding.mapAdressT.setEnabled(false);
                            binding.buttonOpenInWaze.setEnabled(false);
                        }

                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if (e instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) e;
                            Log.e(TAG, "Firestore Error Code: " + firestoreEx.getCode());
                            if (firestoreEx.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE ||
                                    (firestoreEx.getMessage() != null && firestoreEx.getMessage().toLowerCase().contains("client is offline"))) {
                                setErrorText("Offline");
                                if (getContext() != null)
                                    Toast.makeText(getContext(), "Error: Client is offline. Check connection.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void setErrorText(String errorType) {
        if (binding == null) return;
        binding.NameT.setText(errorType);
        binding.sloganT.setText(errorType);
        binding.phoneT.setText(errorType);
        binding.emailT.setText(errorType);
        binding.adressT.setText(errorType);

        // Establecer texto de error para ambos botones
        binding.mapAdressT.setText(errorType);
        binding.buttonOpenInWaze.setText(errorType);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Setting binding to null.");
        binding = null;
    }
}