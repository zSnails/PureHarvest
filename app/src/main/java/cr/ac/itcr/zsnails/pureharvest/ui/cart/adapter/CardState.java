package cr.ac.itcr.zsnails.pureharvest.ui.cart.adapter;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public abstract class CardState {
    protected WeakReference<Card> vh;

    public CardState(@NonNull Card vh) {
        this.vh = new WeakReference<>(vh);
    }

    public abstract void onClick();
}
