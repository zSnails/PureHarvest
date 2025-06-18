package cr.ac.itcr.zsnails.pureharvest.ui.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import cr.ac.itcr.zsnails.pureharvest.R;

public class ProductSectionView extends LinearLayout {

    private TextView sectionTitle;
    private RecyclerView recyclerView;

    public ProductSectionView(Context context) {
        super(context);
        init(context);
    }

    public ProductSectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_product_section, this, true);
        sectionTitle = findViewById(R.id.sectionTitle);
        recyclerView = findViewById(R.id.productRecyclerView);

        // Set GridLayoutManager (2 columns)
        recyclerView.setLayoutManager(new WrapContentGridLayoutManager(context, 2));
    }

    public void setTitle(String title) {
        sectionTitle.setText(title);
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}

