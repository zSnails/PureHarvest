package cr.ac.itcr.zsnails.pureharvest.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

public class MarginItemDecoration extends ItemDecoration {
    private int space = 0;

    public MarginItemDecoration(int size) {
        super();
        this.space = size;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.top = this.space;
        outRect.left = this.space;
        outRect.right = this.space;
        //outRect.bottom = this.space;
    }
}
