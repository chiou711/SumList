/*
 * Copyright (C) 2022 CW Chiu
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

package com.cw.sumlist.operation.folder_sum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.GridView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FolderSum_grid extends Fragment{
	AppCompatActivity act;
	public View rootView;
    TextView title;
	CheckedTextView mCheckTvSelAll;
	FolderSum_list_grid folderSumList;
    TextView textFolderSum;

	public FolderSum_grid(){}
	GridView gridview_sumlist;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.folder_sum_list_grid, container, false);
        act = MainAct.mAct;

		gridview_sumlist = rootView.findViewById(R.id.grid_view_sumlist);
		List<String> gridViewList = new ArrayList<>();

		for(int i=1;i<=31;i++){
			gridViewList.add(String.valueOf(i));
		}

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.folder_sum_title);

        // folder sum
        textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);

        // checked Text View: select all
        mCheckTvSelAll = (CheckedTextView) rootView.findViewById(R.id.chkSelectAllPages);
        mCheckTvSelAll.setOnClickListener(new OnClickListener()
        {	@Override
            public void onClick(View checkSelAll)
            {
                boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
                ((CheckedTextView)checkSelAll).setChecked(!currentCheck);

                if(((CheckedTextView)checkSelAll).isChecked())
                    folderSumList.selectAllPages(true);
                else
                    folderSumList.selectAllPages(false);
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
        folderSumList = new FolderSum_list_grid(act,rootView , gridview_sumlist);
        mCheckTvSelAll.callOnClick();
    }

}