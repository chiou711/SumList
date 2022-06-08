/*
 * Copyright (C) 2019 CW Chiu
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

package com.cw.sumlist.operation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.folder.FolderUi;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FolderSum extends Fragment{
    TextView title;
	CheckedTextView mCheckTvSelAll;
    ListView mListView;
	List_folderSum list_folderSum;
	public static View rootView;
    AppCompatActivity act;
    TextView textFolderSum;

	public FolderSum(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.folder_sum_list, container, false);
        act = MainAct.mAct;

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.folder_sum_title);

        // folder sum
        textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);
        textFolderSum.setText(R.string.folder_sum_title);

        // checked Text View: select all
        mCheckTvSelAll = (CheckedTextView) rootView.findViewById(R.id.chkSelectAllPages);
        mCheckTvSelAll.setOnClickListener(new OnClickListener()
        {	@Override
            public void onClick(View checkSelAll)
            {
                boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
                ((CheckedTextView)checkSelAll).setChecked(!currentCheck);

                if(((CheckedTextView)checkSelAll).isChecked())
                    list_folderSum.selectAllPages(true);
                else
                    list_folderSum.selectAllPages(false);
            }
        });

        // list view: selecting which pages to send
        mListView = (ListView)rootView.findViewById(R.id.listView1);

        // cancel button
        Button btnSelPageCancel = (Button) rootView.findViewById(R.id.btnSelPageCancel);
		btnSelPageCancel.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);

        btnSelPageCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            if(FolderUi.getFolder_pagesCount(act,FolderUi.getFocus_folderPos()) == 0)
            {
                getActivity().finish();
                Intent intent  = new Intent(act,MainAct.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(intent);
            }
            else
                act.getSupportFragmentManager().popBackStack();
            }
        });

		((MainAct)act).setOnBackPressedListener(new BaseBackPressedListener(act));

		return rootView;
	}


	@Override
	public void onPause() {
		super.onPause();
	}

    @Override
    public void onResume() {
        super.onResume();
        //show list for selection
        list_folderSum = new List_folderSum(act,rootView , mListView);
        mCheckTvSelAll.callOnClick();
    }

}