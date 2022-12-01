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
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FolderSum extends Fragment{
	AppCompatActivity act;
	public View rootView;
    TextView title;
	CheckBox mCheckTvSelAll;
	FolderSum_grid folderSum_grid;
    TextView textFolderSum;

	public FolderSum(){
	}

	GridView gridview_sumlist;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.folder_sum, container, false);
        act = MainAct.mAct;

		gridview_sumlist = rootView.findViewById(R.id.folder_sum_grid_view);

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.folder_sum_title);

        // folder sum
        textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);

        // checked Text View: select all
        mCheckTvSelAll = rootView.findViewById(R.id.check_box_select_all_pages_folder_sum);
		mCheckTvSelAll.setChecked(true); // will call selectAllPages(true)
        mCheckTvSelAll.setOnClickListener(new OnClickListener(){
			@Override
            public void onClick(View checkSelAll){
                if(((CheckBox)checkSelAll).isChecked())
                    folderSum_grid.selectAllPages(true);
                else
                    folderSum_grid.selectAllPages(false);
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
	    MainAct.updatePageSumArr();

        //show list for selection
        folderSum_grid = new FolderSum_grid(act,rootView , gridview_sumlist, MainAct.pageSumArr);
        mCheckTvSelAll.callOnClick();
    }

}