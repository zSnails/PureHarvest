package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.view.View;

import androidx.annotation.NonNull;

public class CardExpandedState extends CardState {

    public CardExpandedState(@NonNull Card vh) {
        super(vh);
    }

    @Override
    public void onClick() {
        this.vh.get().setState(new CardClosedState(this.vh.get()));
        this.vh.get().binding.extendedDetailsFrame.setVisibility(View.GONE);
        this.vh.get().binding.productImageSmall.setVisibility(View.VISIBLE);
    }

}
