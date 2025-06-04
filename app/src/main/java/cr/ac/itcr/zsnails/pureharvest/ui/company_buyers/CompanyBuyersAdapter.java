package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.content.Context;
// Quitar imports de Intent, Uri, AlertDialog si no se usan aquí
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
// Quitar Toast si no se usa aquí

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.R;

public class CompanyBuyersAdapter extends RecyclerView.Adapter<CompanyBuyersAdapter.BuyerViewHolder> {

    private List<CompanyBuyer> buyerList;
    private OnBuyerClickListener listener; // Nuevo listener

    // Nueva interfaz para el click
    public interface OnBuyerClickListener {
        void onViewDetailsClick(CompanyBuyer buyer);
    }

    public CompanyBuyersAdapter(List<CompanyBuyer> buyerList, OnBuyerClickListener listener) {
        this.buyerList = buyerList;
        this.listener = listener; // Asignar listener
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
        Context context = holder.itemView.getContext();

        holder.textViewBuyerId.setText(buyer.getId());
        holder.textViewBuyerName.setText(buyer.getName());
        holder.textViewItemsBought.setText(String.valueOf(buyer.getItemsBought()));

        // El botón de contactar ya no está aquí, se movió el ID
        holder.buttonViewDetailsBuyer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailsClick(buyer);
            }
        });
    }

    // El método showContactOptions se moverá al CompanyBuyerDetailsFragment

    @Override
    public int getItemCount() {
        return buyerList == null ? 0 : buyerList.size();
    }

    static class BuyerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBuyerId;
        TextView textViewBuyerName;
        // Quitar textViewBuyerEmail y textViewBuyerPhone
        TextView textViewItemsBought;
        MaterialButton buttonViewDetailsBuyer; // Cambiado de buttonContactBuyer

        public BuyerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBuyerId = itemView.findViewById(R.id.textViewBuyerId);
            textViewBuyerName = itemView.findViewById(R.id.textViewBuyerName);
            textViewItemsBought = itemView.findViewById(R.id.textViewItemsBought);
            buttonViewDetailsBuyer = itemView.findViewById(R.id.buttonViewDetailsBuyer); // ID actualizado
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