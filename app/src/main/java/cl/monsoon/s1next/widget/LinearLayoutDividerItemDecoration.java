package cl.monsoon.s1next.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cl.monsoon.s1next.util.ObjectUtil;

public class LinearLayoutDividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = {
            android.R.attr.listDivider
    };

    private final Drawable mDivider;

    public LinearLayoutDividerItemDecoration(Context context, int color, PorterDuff.Mode mode) {
        TypedArray typedArray = context.obtainStyledAttributes(ATTRS);
        mDivider = typedArray.getDrawable(0);
        typedArray.recycle();

//        mDivider.setColorFilter(color, mode);
    }

    public LinearLayoutDividerItemDecoration(Drawable divider, int color, PorterDuff.Mode mode) {
        this.mDivider = divider;
        this.mDivider.setColorFilter(color, mode);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mDivider == null) {
            return;
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildPosition(view) < 1) {
            return;
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.top = mDivider.getIntrinsicHeight();
        } else {
            outRect.left = mDivider.getIntrinsicWidth();
        }
    }

    void drawVertical(Canvas c, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getTop() - layoutParams.topMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    void drawHorizontal(Canvas c, RecyclerView parent) {
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getLeft() - layoutParams.leftMargin;
            int right = left + mDivider.getIntrinsicWidth();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private int getOrientation(RecyclerView parent) {
        return
                ObjectUtil.cast(
                        parent.getLayoutManager(), LinearLayoutManager.class).getOrientation();
    }
}
