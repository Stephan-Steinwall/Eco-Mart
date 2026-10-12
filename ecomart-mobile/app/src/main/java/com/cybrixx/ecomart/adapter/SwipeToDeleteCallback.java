package com.cybrixx.ecomart.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cybrixx.ecomart.R;

/**
 * Swipe a list row left or right to dismiss it. Draws the red "trash" strip that is revealed
 * behind the row while it slides. Subclasses implement {@link #onSwiped}.
 */
public abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final Drawable icon;
    private final GradientDrawable background = new GradientDrawable();
    private final int iconSize;
    private final int iconMargin;

    public SwipeToDeleteCallback(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        float density = context.getResources().getDisplayMetrics().density;

        icon = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_trash).mutate());
        DrawableCompat.setTint(icon, ContextCompat.getColor(context, R.color.error));

        background.setColor(ContextCompat.getColor(context, R.color.error_container));
        background.setCornerRadius(18 * density); // matches the cart card's corners

        iconSize = (int) (24 * density);
        iconMargin = (int) (20 * density);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false; // rows can be swiped, not reordered
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        View row = viewHolder.itemView;
        if (dX != 0) {
            boolean swipingRight = dX > 0;
            if (swipingRight) {
                background.setBounds(row.getLeft(), row.getTop(), row.getLeft() + (int) dX, row.getBottom());
            } else {
                background.setBounds(row.getRight() + (int) dX, row.getTop(), row.getRight(), row.getBottom());
            }
            background.draw(c);

            // Only show the icon once enough of the strip is revealed to fit it.
            if (Math.abs(dX) > iconMargin + iconSize) {
                int iconTop = row.getTop() + (row.getHeight() - iconSize) / 2;
                int iconLeft = swipingRight ? row.getLeft() + iconMargin : row.getRight() - iconMargin - iconSize;
                icon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
                icon.draw(c);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
