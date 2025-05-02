package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import android.view.View;

public class CardClosedState extends CardState {

    public CardClosedState(Card vh) {
        super(vh);
    }

    @Override
    public void onClick() {
        this.vh.get().setState(new CardExpandedState(this.vh.get()));
        this.vh.get().binding.extendedDetailsFrame.setVisibility(View.VISIBLE);
        this.vh.get().binding.productImageSmall.setVisibility(View.GONE);
    }

}
