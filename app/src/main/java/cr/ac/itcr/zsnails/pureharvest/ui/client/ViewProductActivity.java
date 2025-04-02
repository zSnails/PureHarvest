package cr.ac.itcr.zsnails.pureharvest.ui.client;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cr.ac.itcr.zsnails.pureharvest.R;

public class ViewProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        // Show "back" arrow in the app bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("View Product");
        }
    }

    // Handle back arrow click
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Close this screen and return to previous one
        return true;
    }
}