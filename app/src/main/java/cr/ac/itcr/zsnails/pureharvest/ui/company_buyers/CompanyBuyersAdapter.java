package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.R;

public class CompanyBuyersAdapter extends RecyclerView.Adapter<CompanyBuyersAdapter.BuyerViewHolder> {

    private List<CompanyBuyer> buyerList;

    public CompanyBuyersAdapter(List<CompanyBuyer> buyerList) {
        this.buyerList = buyerList;
    }

    @NonNull
    @Override
    public BuyerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company_buyer, parent, false);
        return new BuyerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuyerViewHolder holder, int position) {
        CompanyBuyer buyer = buyerList.get(position);
        holder.textViewBuyerId.setText(buyer.getId());
        holder.textViewBuyerName.setText(buyer.getName());

        String displayEmail = (buyer.getEmail() != null && !buyer.getEmail().equals("N/A") && !buyer.getEmail().isEmpty())
                ? buyer.getEmail() : "No disponible";
        String displayPhone = (buyer.getPhone() != null && !buyer.getPhone().equals("N/A") && !buyer.getPhone().isEmpty())
                ? buyer.getPhone() : "No disponible";

        holder.textViewBuyerEmail.setText(displayEmail);
        holder.textViewBuyerPhone.setText(displayPhone);
        holder.textViewItemsBought.setText(String.valueOf(buyer.getItemsBought()));

        holder.buttonContactBuyer.setOnClickListener(v -> {
            showContactOptions(holder.itemView.getContext(), buyer);
        });
    }

    private void showContactOptions(Context context, CompanyBuyer buyer) {
        List<String> options = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        String phone = buyer.getPhone();
        String email = buyer.getEmail();

        boolean phoneAvailable = phone != null && !phone.isEmpty() && !phone.equals("N/A");
        boolean emailAvailable = email != null && !email.isEmpty() && !email.equals("N/A");

        if (phoneAvailable) {
            options.add("Llamar");
            actions.add(() -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                context.startActivity(intent);
            });

            options.add("Enviar SMS");
            actions.add(() -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
                context.startActivity(intent);
            });

            options.add("Enviar WhatsApp");
            actions.add(() -> {
                try {
                    String formattedPhone = phone.replaceAll("[^0-9+]", "");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + formattedPhone));
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "WhatsApp no está instalado.", Toast.LENGTH_SHORT).show();
                        try {
                            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.whatsapp"));
                            if (playStoreIntent.resolveActivity(context.getPackageManager()) != null) {
                                context.startActivity(playStoreIntent);
                            }
                        } catch (Exception eMarket) {
                            Toast.makeText(context, "No se pudo abrir Play Store.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Error al intentar abrir WhatsApp.", Toast.LENGTH_SHORT).show();
                    Log.e("CompanyBuyerAdapter", "WhatsApp Error", e);
                }
            });
        }

        if (emailAvailable) {
            options.add("Enviar Email");
            actions.add(() -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No hay aplicación de correo instalada.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (options.isEmpty()) {
            Toast.makeText(context, "No hay información de contacto disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Contactar a " + buyer.getName());
        builder.setItems(options.toArray(new CharSequence[0]), (dialog, which) -> {
            if (which >= 0 && which < actions.size()) {
                actions.get(which).run();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    @Override
    public int getItemCount() {
        return buyerList == null ? 0 : buyerList.size();
    }

    static class BuyerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBuyerId;
        TextView textViewBuyerName;
        TextView textViewBuyerEmail;
        TextView textViewBuyerPhone;
        TextView textViewItemsBought;
        MaterialButton buttonContactBuyer;

        public BuyerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBuyerId = itemView.findViewById(R.id.textViewBuyerId);
            textViewBuyerName = itemView.findViewById(R.id.textViewBuyerName);
            textViewBuyerEmail = itemView.findViewById(R.id.textViewBuyerEmail);
            textViewBuyerPhone = itemView.findViewById(R.id.textViewBuyerPhone);
            textViewItemsBought = itemView.findViewById(R.id.textViewItemsBought);
            buttonContactBuyer = itemView.findViewById(R.id.buttonContactBuyer);
        }
    }

    public void updateData(List<CompanyBuyer> newBuyerList) {
        this.buyerList.clear();
        if (newBuyerList != null) {
            this.buyerList.addAll(newBuyerList);
        }
        notifyDataSetChanged();
    }
}