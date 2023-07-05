/*
 * Copyright (C) 2023 CW Chiu
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

package com.cw.sumlist.note_add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cw.sumlist.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Created by cw on 2023/06/29
 */
public class SelectOftenItem extends Fragment{
    TextView title;
    ListView mListView;
    List_selectOftenItem list_selOftenIem;
	public static View rootView;
    AppCompatActivity act;

	public SelectOftenItem(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.select_often_item, container, false);
		act = (AppCompatActivity) getActivity();

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.select_often_item);

        // list view: selecting which pages to send
        mListView = (ListView)rootView.findViewById(R.id.listView1);

        //show list for selection
        list_selOftenIem = new List_selectOftenItem(act,rootView , mListView);

		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}