package cr.ac.itcr.zsnails.pureharvest.ui.home;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.ArrayList;
import cr.ac.itcr.zsnails.pureharvest.data.model.Product;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Product>> products = new MutableLiveData<>();

    public HomeViewModel() {
        loadProducts();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    private void loadProducts() {
        // Simulate fetching products (replace with repository call later)
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("1", "Organic Coffee", 12.99, "https://example.com/coffee.jpg"));
        productList.add(new Product("2", "Honey Jar", 9.99, "https://example.com/honey.jpg"));
        productList.add(new Product("3", "Green Tea", 7.99, "https://example.com/tea.jpg"));
        productList.add(new Product("4", "Almond Milk", 5.99, "https://example.com/milk.jpg"));

        products.setValue(productList);
    }
}