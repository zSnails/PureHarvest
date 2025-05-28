package cr.ac.itcr.zsnails.pureharvest.ui.coupons;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cr.ac.itcr.zsnails.pureharvest.R;

public class ManageCouponsFragment extends Fragment {

    public ManageCouponsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_coupons, container, false);
    }
}