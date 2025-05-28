package cr.ac.itcr.zsnails.pureharvest;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

    private Button settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null && "company_contact".equals(intent.getStringExtra("navigate_to"))) {
            String companyId = intent.getStringExtra("company_id");

            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putString("company_id", companyId);
            navController.navigate(R.id.companyContactFragment, bundle);
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_account, R.id.navigation_shopping_cart)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
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

                if (item.getItemId() == R.id.navigation_dashboard || item.getItemId() == R.id.navigation_home) {
                    builder.setRestoreState(false);
                } else {
                    builder.setRestoreState(true);
                }

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

        if (intent != null && "company_contact".equals(intent.getStringExtra("navigate_to"))) {
            String companyId = intent.getStringExtra("company_id");

            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putString("company_id", companyId);
            navController.navigate(R.id.companyContactFragment, bundle);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}