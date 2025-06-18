package cr.ac.itcr.zsnails.pureharvest.ui.orders.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentClientOrdersBinding;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.client.adapter.ClientOrdersAdapter;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientOrdersFragment extends Fragment {

    @Inject
    public FirebaseFirestore db;
    @Inject
    public FirebaseAuth auth;
    public ClientOrdersAdapter adapter;
    private ClientOrdersViewModel mViewModel;
    private FragmentClientOrdersBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.mViewModel = new ViewModelProvider(this).get(ClientOrdersViewModel.class);
        this.binding = FragmentClientOrdersBinding.inflate(inflater, container, false);
        mViewModel.loadOrders(auth.getCurrentUser().getUid());
        this.adapter = new ClientOrdersAdapter();

        this.binding.clientOrdersRecyclerView.setAdapter(adapter);
        this.binding.clientOrdersRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        mViewModel.orders.observe(getViewLifecycleOwner(), orders -> {
            adapter.setOrders(orders);
        });
        return binding.getRoot();
    }
}