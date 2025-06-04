package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.R;

public class CompanyBuyerDetailsFragment extends Fragment {

    private static final String ARG_BUYER_ID = "buyer_id";
    private static final String ARG_ITEMS_BOUGHT = "items_bought";
    private static final String TAG = "BuyerDetailsFragment";

    private String buyerId;
    private int itemsBought;
    private FirebaseFirestore db;
    private NavController navController;

    private MaterialToolbar toolbar;
    private TextView tvBuyerDetailId, tvBuyerDetailName, tvBuyerDetailItemsBought, tvBuyerDetailEmail, tvBuyerDetailPhone;
    private TextView tvError;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;
    private MaterialButton buttonContactBuyer;

    // Para almacenar temporalmente la info del comprador recuperada
    private String buyerNameValue, buyerEmailValue, buyerPhoneValue;


    public CompanyBuyerDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            buyerId = getArguments().getString(ARG_BUYER_ID);
            itemsBought = getArguments().getInt(ARG_ITEMS_BOUGHT, 0); // Default 0
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_buyer_details, container, false);

        toolbar = view.findViewById(R.id.toolbarBuyerDetails);
        tvBuyerDetailId = view.findViewById(R.id.tvBuyerDetailId);
        tvBuyerDetailName = view.findViewById(R.id.tvBuyerDetailName);
        tvBuyerDetailItemsBought = view.findViewById(R.id.tvBuyerDetailItemsBought);
        tvBuyerDetailEmail = view.findViewById(R.id.tvBuyerDetailEmail);
        tvBuyerDetailPhone = view.findViewById(R.id.tvBuyerDetailPhone);
        buttonContactBuyer = view.findViewById(R.id.buttonContactBuyerDetails);
        progressBar = view.findViewById(R.id.progressBarBuyerDetails);
        contentLayout = view.findViewById(R.id.buyerDetailsContentLayout);
        tvError = view.findViewById(R.id.textViewBuyerDetailsError);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            navController = Navigation.findNavController(view);
        } catch (IllegalStateException e) {
            Log.e(TAG, "NavController not found for this view.", e);
        }

        setupToolbar();

        if (buyerId != null && !buyerId.isEmpty()) {
            fetchBuyerDetails();
        } else {
            //showError(getString(R.string.error_invalid_buyer_id_display));
        }

        buttonContactBuyer.setOnClickListener(v -> {
            if (buyerNameValue != null && (buyerEmailValue != null || buyerPhoneValue != null) ) {
                showContactOptions(requireContext(), buyerNameValue, buyerEmailValue, buyerPhoneValue);
            } else {
                //Toast.makeText(getContext(), getString(R.string.toast_no_contact_info_for_options), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> navigateBack());
    }

    private void fetchBuyerDetails() {
        showLoading(true);
        tvError.setVisibility(View.GONE);

        db.collection("users").document(buyerId).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) return;
                    showLoading(false);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            buyerNameValue = document.getString("fullName");
                            buyerEmailValue = document.getString("email");
                            buyerPhoneValue = document.getString("phone");
                            displayBuyerDetails();
                        } else {
                            Log.w(TAG, "No such buyer document with ID: " + buyerId);
                            //(getString(R.string.error_buyer_not_found));
                        }
                    } else {
                        Log.e(TAG, "Error fetching buyer details", task.getException());
                        //String errorMsg = getString(R.string.error_fetching_buyer_details_generic);
                        //if (task.getException() != null) errorMsg += ": " + task.getException().getMessage();
                        //showError(errorMsg);
                    }
                });
    }

    private void displayBuyerDetails() {
        contentLayout.setVisibility(View.VISIBLE);
        String naText = getString(R.string.text_not_available);

        tvBuyerDetailId.setText(buyerId != null ? buyerId : naText);
        tvBuyerDetailName.setText(buyerNameValue != null && !buyerNameValue.equals("N/A") ? buyerNameValue : naText);
        tvBuyerDetailItemsBought.setText(String.valueOf(itemsBought));
        tvBuyerDetailEmail.setText(buyerEmailValue != null && !buyerEmailValue.equals("N/A") ? buyerEmailValue : naText);
        tvBuyerDetailPhone.setText(buyerPhoneValue != null && !buyerPhoneValue.equals("N/A") ? buyerPhoneValue : naText);

        // Habilitar el botón de contacto solo si hay información de contacto
        boolean hasContactInfo = (buyerEmailValue != null && !buyerEmailValue.isEmpty() && !buyerEmailValue.equals("N/A")) ||
                (buyerPhoneValue != null && !buyerPhoneValue.isEmpty() && !buyerPhoneValue.equals("N/A"));
        buttonContactBuyer.setEnabled(hasContactInfo);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(isLoading ? View.GONE : contentLayout.getVisibility()); // No ocultar si ya es visible
    }

    private void showError(String message) {
        contentLayout.setVisibility(View.GONE);
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void navigateBack() {
        if (navController != null && navController.getCurrentDestination() != null &&
                navController.getGraph().findNode(navController.getCurrentDestination().getId()) != null) {
            navController.popBackStack();
        } else if (getActivity() != null && !getActivity().isFinishing()) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                getActivity().onBackPressed(); // Fallback
            }
        }
    }

    // Copiado y adaptado de CompanyBuyersAdapter
    private void showContactOptions(Context context, String name, String email, String phone) {
        List<String> options = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        boolean phoneAvailable = phone != null && !phone.isEmpty() && !phone.equals("N/A");
        boolean emailAvailable = email != null && !email.isEmpty() && !email.equals("N/A");

        if (phoneAvailable) {
            options.add(context.getString(R.string.action_call));
            actions.add(() -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                context.startActivity(intent);
            });

            options.add(context.getString(R.string.action_send_sms));
            actions.add(() -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
                context.startActivity(intent);
            });

            options.add(context.getString(R.string.action_send_whatsapp));
            actions.add(() -> {
                try {
                    String formattedPhone = phone.replaceAll("[^0-9+]", "");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + formattedPhone));
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, context.getString(R.string.toast_whatsapp_not_installed), Toast.LENGTH_SHORT).show();
                        try { // Intenta abrir Play Store
                            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.whatsapp"));
                            if (playStoreIntent.resolveActivity(context.getPackageManager()) != null) {
                                context.startActivity(playStoreIntent);
                            }
                        } catch (Exception eMarket) {
                            Toast.makeText(context, context.getString(R.string.toast_play_store_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, context.getString(R.string.toast_whatsapp_error), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "WhatsApp Error", e);
                }
            });
        }

        if (emailAvailable) {
            options.add(context.getString(R.string.action_send_email));
            actions.add(() -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_no_email_app), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (options.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.toast_no_contact_info), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
       // builder.setTitle(String.format(context.getString(R.string.dialog_title_contact_prefix_details), name != null ? name : buyerId));
        builder.setItems(options.toArray(new CharSequence[0]), (dialog, which) -> {
            if (which >= 0 && which < actions.size()) {
                actions.get(which).run();
            }
        });
        builder.setNegativeButton(context.getString(R.string.action_cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}