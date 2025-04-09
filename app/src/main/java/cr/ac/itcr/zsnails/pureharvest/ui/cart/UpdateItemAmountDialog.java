package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

import cr.ac.itcr.zsnails.pureharvest.R;

public final class UpdateItemAmountDialog extends DialogFragment {

    private ItemAmountAcceptListener cb;
    private DialogInterface a;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int position = getArguments().getInt("position");
        var inflater = getLayoutInflater();
        final Item item = getBundleItem();
        View inflated = inflater.inflate(R.layout.cart_item_amount_edit_dialog, null);
        AppCompatEditText edit = inflated.findViewById(R.id.cart_item_amount_edit);
        edit.setText(String.format(Locale.getDefault(), "%d", item.getAmount()));
        builder.setView(inflated);
        builder.setPositiveButton(android.R.string.yes, (DialogInterface a, int b) -> {
            Editable text = edit.getText();
            int amount = Integer.parseInt(text.toString());
            cb.onAmountAccepted(item, position, amount);
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private Item getBundleItem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getArguments().getSerializable("item", Item.class);
        } else {
            return (Item) getArguments().getSerializable("item");
        }
    }

    public void setItemAmountAcceptListener(ItemAmountAcceptListener cb) {
        this.cb = cb;
    }

    public interface ItemAmountAcceptListener {
        void onAmountAccepted(Item item, int position, int amount);
    }
}
