package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

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
        holder.textViewItemsBought.setText(String.valueOf(buyer.getItemsBought()));

        holder.buttonSeeDetails.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Details for " + buyer.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return buyerList == null ? 0 : buyerList.size();
    }

    static class BuyerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBuyerId;
        TextView textViewBuyerName;
        TextView textViewItemsBought;
        MaterialButton buttonSeeDetails;

        public BuyerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBuyerId = itemView.findViewById(R.id.textViewBuyerId);
            textViewBuyerName = itemView.findViewById(R.id.textViewBuyerName);
            textViewItemsBought = itemView.findViewById(R.id.textViewItemsBought);
            buttonSeeDetails = itemView.findViewById(R.id.buttonSeeDetails);
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