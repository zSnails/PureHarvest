package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.textViewBuyerEmail.setText(buyer.getEmail() != null ? buyer.getEmail() : "N/A");
        holder.textViewBuyerPhone.setText(buyer.getPhone() != null ? buyer.getPhone() : "N/A");
        holder.textViewItemsBought.setText(String.valueOf(buyer.getItemsBought()));
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

        public BuyerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBuyerId = itemView.findViewById(R.id.textViewBuyerId);
            textViewBuyerName = itemView.findViewById(R.id.textViewBuyerName);
            textViewBuyerEmail = itemView.findViewById(R.id.textViewBuyerEmail);
            textViewBuyerPhone = itemView.findViewById(R.id.textViewBuyerPhone);
            textViewItemsBought = itemView.findViewById(R.id.textViewItemsBought);
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