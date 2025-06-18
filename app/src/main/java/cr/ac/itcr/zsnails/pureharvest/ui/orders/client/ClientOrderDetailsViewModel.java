package cr.ac.itcr.zsnails.pureharvest.ui.orders.client;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.domain.repository.ClientOrdersRepository;
import cr.ac.itcr.zsnails.pureharvest.ui.orders.Order;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ClientOrderDetailsViewModel extends ViewModel {
    public MutableLiveData<List<ClientOrdersRepository.OrderDisplayItem>> displayItems;
    private ClientOrdersRepository repo;

    @Inject
    public ClientOrderDetailsViewModel(@NonNull ClientOrdersRepository repo) {
        this.repo = repo;
    }

    public void loadDisplayItems(Order order) {
        displayItems = this.repo.getOrderDisplayItems(order);
    }
}