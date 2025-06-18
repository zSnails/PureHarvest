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
public class ClientOrdersViewModel extends ViewModel {

    private final ClientOrdersRepository repo;
    public MutableLiveData<List<Order>> orders;

    @Inject
    public ClientOrdersViewModel(@NonNull ClientOrdersRepository repo) {
        this.repo = repo;
    }

    public void loadOrders(String clientId) {
        this.orders = repo.getClientOrders(clientId);
    }
}