package cr.ac.itcr.zsnails.pureharvest.ui.coupons;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cr.ac.itcr.zsnails.pureharvest.R;
import java.util.Map;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    public interface OnCouponDeleteListener {
        void onDeleteCoupon(int position, Map<String, Object> couponData);
    }

    private final List<Map<String, Object>> couponList;
    private final OnCouponDeleteListener deleteListener;

    public CouponAdapter(List<Map<String, Object>> couponList, OnCouponDeleteListener deleteListener) {
        this.couponList = couponList;
        this.deleteListener = deleteListener;
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
        if (timestamp instanceof Long) {
            Date date = new Date((Long) timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.expiration.setText("Expires: " + sdf.format(date));
        }

        int maxUses = ((Number) coupon.get("maxUses")).intValue();
        int uses = ((Number) coupon.get("uses")).intValue();
        holder.remaining.setText("Remaining uses: " + (maxUses - uses));

        holder.itemView.setOnLongClickListener(v -> {
            deleteListener.onDeleteCoupon(position, coupon);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView code, discount, expiration, remaining;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.text_coupon_code);
            discount = itemView.findViewById(R.id.text_coupon_discount);
            expiration = itemView.findViewById(R.id.text_coupon_expiration);
            remaining = itemView.findViewById(R.id.text_coupon_remaining);
        }
    }
}