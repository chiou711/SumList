/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cw.sumlist.page.item_touch_helper;

import android.graphics.Canvas;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.page.PageAdapter;
import com.cw.sumlist.tabs.TabsHost;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import static com.cw.sumlist.main.MainAct.mAct;

/**
 * An implementation of {@link ItemTouchHelper.Callback} that enables basic drag & drop and
 * swipe-to-dismiss. Drag events are automatically started by an item long-press.<br/>
 * </br/>
 * Expects the <code>RecyclerView.Adapter</code> to listen for {@link
 * ItemTouchHelperAdapter} callbacks and the <code>RecyclerView.ViewHolder</code> to implement
 * {@link ItemTouchHelperViewHolder}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class SwipeItemTouchHelperCallback extends ItemTouchHelper.Callback {

    public static final float ALPHA_FULL = 1.0f;

    private final PageAdapter mAdapter;

    public SwipeItemTouchHelperCallback(PageAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
//        return true;
        return false;//disabled
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true; // enable card view Swipe
//        return false;//disabled
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Set movement flags based on the layout manager
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        // Notify the adapter of the move
        mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder sourceViewHolder, int fromPos, RecyclerView.ViewHolder targetViewHolder, int toPos, int x, int y) {
        super.onMoved(recyclerView, sourceViewHolder, fromPos, targetViewHolder, toPos, x, y);
        mAdapter.onItemMoved(sourceViewHolder,fromPos,targetViewHolder,toPos);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {

        DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
        int qty = db_page.getNoteQuantity(viewHolder.getAdapterPosition(),true);
        // i = 32: from left to right
        // i = 16: from right to left
        boolean isSwipeRight = (i==32);
        boolean isSwipeLeft = (i==16);
        int position = viewHolder.getAdapterPosition();

        if(isSwipeRight && (qty > 0))
            updateNote_addInt(position,-1);

        if(isSwipeLeft && (qty >= 0))
            updateNote_addInt(position,1);

        // Notify the adapter of the dismissal
        // the following item will shift up when Dismiss
        //mAdapter.onItemDismiss(viewHolder.getAdapterPosition());

        mAdapter.updateDbCache();
//        mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemChanged(position);
        TabsHost.showFooter(mAct);
    }

    void updateNote_addInt(int position, int i){
        DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
        db_page.updateNote(db_page.getNoteId(position,true),
                db_page.getNoteTitle(position,true),
                db_page.getNoteBody(position,true),
                db_page.getNoteCategory(position,true),
                db_page.getNoteQuantity(position,true)+i,
                db_page.getNoteMarking(position,true),
                true);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // We only want the active item to change
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                // Let the view holder know that this item is being moved or dragged
                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        viewHolder.itemView.setAlpha(ALPHA_FULL);

        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            // Tell the view holder it's time to restore the idle state
            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear();
        }
    }
}
