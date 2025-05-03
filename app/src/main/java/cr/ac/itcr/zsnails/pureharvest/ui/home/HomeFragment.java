package cr.ac.itcr.zsnails.pureharvest.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;

import cr.ac.itcr.zsnails.pureharvest.R;
import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentHomeBinding;
import cr.ac.itcr.zsnails.pureharvest.decoration.MarginItemDecoration;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        shoppingCart = new ViewModelProvider(requireActivity()).get(ShoppingCartViewModel.class);

        final ProductAdapter adapter = new ProductAdapter(new ArrayList<>(), this);
        this.binding.recyclerView.addItemDecoration(
                new MarginItemDecoration(
                        (int) getResources().getDimension(R.dimen.random_item_list_margin)));
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        viewModel.getProducts().observe(getViewLifecycleOwner(), adapter::updateData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}