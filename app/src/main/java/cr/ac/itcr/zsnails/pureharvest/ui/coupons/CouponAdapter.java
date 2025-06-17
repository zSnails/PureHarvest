package cr.ac.itcr.zsnails.pureharvest.ui.coupons;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cr.ac.itcr.zsnails.pureharvest.R;
public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    public interface OnCouponEditListener {
        void onEditCoupon(int position, Map<String, Object> couponData);
    }

    private final List<Map<String, Object>> couponList;
    private final OnCouponEditListener editListener;

    public CouponAdapter(List<Map<String, Object>> couponList, OnCouponEditListener editListener) {
        this.couponList = couponList;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Map<String, Object> coupon = couponList.get(position);

        holder.code.setText(String.valueOf(coupon.get("code")));
        holder.discount.setText("Discount: " + coupon.get("discountPercentage") + "%");

        Object timestamp = coupon.get("expirationTimestamp");
        long now = System.currentTimeMillis();
        boolean expired = false;
        if (timestamp instanceof Long) {
            Date date = new Date((Long) timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.expiration.setText("Expires: " + sdf.format(date));
            expired = (Long) timestamp < now;
        }

        int maxUses = ((Number) coupon.get("maxUses")).intValue();
        int uses = ((Number) coupon.get("uses")).intValue();
        int remaining = maxUses - uses;
        holder.remaining.setText("Remaining uses: " + remaining);

        if (expired || remaining <= 0) {
            holder.status.setVisibility(View.VISIBLE);
            if (expired) {
                holder.status.setText("Status: expired");
            } else {
                holder.status.setText("Status: used up");
            }
        } else {
            holder.status.setVisibility(View.GONE);
        }

        holder.editButton.setOnClickListener(v -> {
            editListener.onEditCoupon(position, coupon);
        });
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView code, discount, expiration, remaining, status;
        Button editButton;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.text_coupon_code);
            discount = itemView.findViewById(R.id.text_coupon_discount);
            expiration = itemView.findViewById(R.id.text_coupon_expiration);
            remaining = itemView.findViewById(R.id.text_coupon_remaining);
            status = itemView.findViewById(R.id.text_coupon_status);
            editButton = itemView.findViewById(R.id.button_edit_coupon);
        }
    }
}