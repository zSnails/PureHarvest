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

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentClientOrderDetailsBinding;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.client.adapter.ClientOrderItemsAdapter;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientOrderDetailsFragment extends Fragment {

    private ClientOrderDetailsViewModel mViewModel;
    private FragmentClientOrderDetailsBinding binding;

    private ClientOrderItemsAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.mViewModel = new ViewModelProvider(this).get(ClientOrderDetailsViewModel.class);
        this.binding = FragmentClientOrderDetailsBinding.inflate(inflater, container, false);
        this.adapter = new ClientOrderItemsAdapter();
        Order order = (Order)getArguments().getSerializable("order");
        this.mViewModel.loadDisplayItems(order);
        mViewModel.displayItems.observe(getViewLifecycleOwner(), data -> {
            adapter.setItems(data);
        });
        this.binding.clientOrderItemsRecyclerView.setAdapter(adapter);
        this.binding.clientOrderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return binding.getRoot();
    }

}