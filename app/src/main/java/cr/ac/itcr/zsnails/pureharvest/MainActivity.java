package cr.ac.itcr.zsnails.pureharvest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;

import cr.ac.itcr.zsnails.pureharvest.databinding.ActivityMainBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    public static String idGlobalUser = "1";
    private ActivityMainBinding binding;


    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        handleIntentExtras(getIntent());


        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_account, R.id.navigation_shopping_cart)
                .build();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();

            if (destId == R.id.companyProductsListFragment) {
                binding.fabMenu.setVisibility(View.VISIBLE);
                binding.fabMenu.show();
            } else {
                binding.fabMenu.setVisibility(View.GONE);
                binding.fabMenu.hide();
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                NavOptions.Builder builder = new NavOptions.Builder();
                builder.setLaunchSingleTop(true);

                if ((item.getOrder() & Menu.CATEGORY_SECONDARY) == 0) {
                    int startDestinationId = navController.getGraph().getStartDestinationId();
                    if (startDestinationId != 0) {
                        boolean inclusive = false;
                        boolean saveState = true;
                        builder.setPopUpTo(startDestinationId, inclusive, saveState);
                    }
                }

                builder.setRestoreState(false);

                NavOptions navOptions = builder.build();
                try {
                    navController.navigate(item.getItemId(), null, navOptions);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });

        binding.fabMenu.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, view);
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(popupMenuItem -> {
                if (popupMenuItem.getItemId() == R.id.action_create_product) {
                    startActivity(new Intent(MainActivity.this, cr.ac.itcr.zsnails.pureharvest.ui.seller.CreateProductActivity.class));
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras(intent);
    }


    private void handleIntentExtras(Intent intent) {
        if (intent != null && intent.hasExtra("navigate_to") && navController != null) {
            String destination = intent.getStringExtra("navigate_to");
            Bundle args = new Bundle();
            Log.d("MainActivity", "handleIntentExtras: Destino = " + destination);

            if ("company_contact".equals(destination)) {
                if (intent.hasExtra("company_id")) {
                    String companyId = intent.getStringExtra("company_id");
                    args.putString("company_id", companyId);
                    Log.d("MainActivity", "company_id: " + companyId);
                }
                try {
                    navController.navigate(R.id.companyContactFragment, args);
                } catch (Exception e) {
                    Log.e("MainActivity", "Error al navegar a company_contact: " + e.getMessage(), e);
                }
            } else if ("stand_out_payment".equals(destination)) {
                if (intent.hasExtra("productId")) {
                    String productId = intent.getStringExtra("productId");
                    args.putString("productId", productId);
                    Log.d("MainActivity", "productId: " + productId);
                }
                try {
                    navController.navigate(R.id.action_global_to_standOutPaymentFragment, args);
                } catch (Exception e) {
                    Log.e("MainActivity", "Error al navegar a stand_out_payment: " + e.getMessage(), e);
                }
            }

            intent.removeExtra("navigate_to");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp()) || super.onSupportNavigateUp();
    }
}