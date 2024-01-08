/*
 * Copyright (C) 2020 CW Chiu
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


package com.cw.sumlist.page;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.page.item_touch_helper.OnStartDragListener;
import com.cw.sumlist.page.item_touch_helper.SimpleItemTouchHelperCallback;
import com.cw.sumlist.page.item_touch_helper.SwipeItemTouchHelperCallback;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.util.preferences.Pref;

import static com.cw.sumlist.define.Define.ENABLE_ITEM_TOUCH_SWIPE;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class Page extends Fragment implements OnStartDragListener {

    public static int page_tableId;
    public RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutMgr;
    int page_pos;
    public static int mCurrPlayPosition;
    public AppCompatActivity act;

    public PageAdapter itemAdapter;
    private ItemTouchHelper itemTouchHelper;

    public Page(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        assert args != null;
        page_pos = args.getInt("page_pos");
        page_tableId = args.getInt("page_table_id");
        System.out.println("Page / _onCreateView / page_tableId = " + page_tableId);
        act = MainAct.mAct;
        int focusTableId = Pref.getPref_focusView_page_tableId(act);
        int diff = Math.abs(focusTableId - page_tableId);
        if(diff > 1) {
            System.out.println("Page / _onCreateView / return");
            return null;
        } else
            System.out.println("Page / _onCreateView / go on");

        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        TextView blankView = rootView.findViewById(R.id.blankPage);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        layoutMgr = new LinearLayoutManager(getActivity());

        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        layoutMgr = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutMgr);
        recyclerView.scrollToPosition(scrollPosition);

        fillData();

        // swipe callback
        if(ENABLE_ITEM_TOUCH_SWIPE) {
            ItemTouchHelper.Callback callback = new SwipeItemTouchHelperCallback(itemAdapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        } else {
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(itemAdapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        if( (itemAdapter != null) &&
            (itemAdapter.getItemCount() ==0) ){ //todo bug: Attempt to invoke interface method 'int android.database.Cursor.getCount()' on a null object reference
            blankView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else {
            blankView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Pref.getPref_focusView_page_tableId(MainAct.mAct) == page_tableId) {
//            System.out.println("Page / _onResume / resume_listView_vScroll");
            TabsHost.resume_listView_vScroll(recyclerView);
        }
    }

    private void fillData()
    {
        System.out.println("Page / _fillData / page_tableId = " + page_tableId);
        System.out.println("Page / _fillData / page_pos = " + page_pos);
        itemAdapter = new PageAdapter(page_pos, page_tableId, this);
        // Set PageAdapter_recycler as the adapter for RecyclerView.
        recyclerView.setAdapter(itemAdapter);
    }

    // swap rows
    protected static void swapRows(DB_page dB_page, int startPosition, int endPosition)
    {
        Long noteNumber1;
        String noteTitle1;
        Integer noteBody1;
        Integer noteQuantity1;
        String noteCategory1;
        Integer markingIndex1;
        Long noteNumber2 ;
        String noteTitle2;
        Integer noteBody2;
        String noteCategory2;
        Integer noteQuantity2;
        Integer markingIndex2;

        dB_page.open();
        noteNumber1 = dB_page.getNoteId(startPosition,false);
        noteTitle1 = dB_page.getNoteTitle(startPosition,false);
        noteBody1 = dB_page.getNoteBody(startPosition,false);
        noteQuantity1 = dB_page.getNoteQuantity(startPosition,false);
        noteCategory1 = dB_page.getNoteCategory(startPosition,false);
        markingIndex1 = dB_page.getNoteMarking(startPosition,false);

        noteNumber2 = dB_page.getNoteId(endPosition,false);
        noteTitle2 = dB_page.getNoteTitle(endPosition,false);
        noteBody2 = dB_page.getNoteBody(endPosition,false);
        noteQuantity2 = dB_page.getNoteQuantity(endPosition,false);
        noteCategory2 = dB_page.getNoteCategory(endPosition,false);
        markingIndex2 = dB_page.getNoteMarking(endPosition,false);

        dB_page.updateNote(noteNumber2,
                noteTitle1,
                noteBody1,
                noteCategory1,
                noteQuantity1,
                markingIndex1,false);

        dB_page.updateNote(noteNumber1,
                noteTitle2,
                noteBody2,
                noteCategory2,
                noteQuantity2,
                markingIndex2,false);

        dB_page.close();
    }

    public void swapTopBottom()
    {
        DB_page dB_page = new DB_page(  MainAct.mAct ,DB_page.getFocusPage_tableId());
        int startCursor = dB_page.getNotesCount(true)-1;
        int endCursor = 0;

        //reorder data base storage for ADD_NEW_TO_TOP option
        int loop = Math.abs(startCursor-endCursor);
        for(int i=0;i< loop;i++)
        {
            swapRows(dB_page, startCursor,endCursor);
            if((startCursor-endCursor) >0)
                endCursor++;
            else
                endCursor--;
        }
    }


    public int getNotesCountInPage(AppCompatActivity act)
    {
        DB_page db_page = new DB_page(act,TabsHost.getCurrentPageTableId() );
        int count = db_page.getNotesCount(true);
        return count;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
//        if(ENABLE_ITEM_TOUCH_SWIPE)
            itemTouchHelper.startDrag(viewHolder);
    }
}
