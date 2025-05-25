package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.databinding.FragmentCompanyBuyersListBinding;

public class CompanyBuyersListFragment extends Fragment {

    private FragmentCompanyBuyersListBinding binding;
    private CompanyBuyersAdapter adapter;
    private List<CompanyBuyer> companyBuyerList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompanyBuyersListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.recyclerViewCompanyBuyers;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        companyBuyerList = new ArrayList<>();
        loadDummyData();

        adapter = new CompanyBuyersAdapter(companyBuyerList);
        recyclerView.setAdapter(adapter);

    }

    private void loadDummyData() {
        companyBuyerList.add(new CompanyBuyer("CB001", "Comprador Alfa", 15));
        companyBuyerList.add(new CompanyBuyer("CB002", "Comprador Beta", 8));
        companyBuyerList.add(new CompanyBuyer("CB003", "Comprador Gamma", 22));
        companyBuyerList.add(new CompanyBuyer("CB004", "Comprador Delta", 5));
        companyBuyerList.add(new CompanyBuyer("CB005", "Comprador Epsilon", 30));
        companyBuyerList.add(new CompanyBuyer("CB006", "Comprador Zeta", 2));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}