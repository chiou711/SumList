/*
 * Copyright (c) 2015 The Android Open Source Project
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

package com.cw.sumlist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;

/**
 * A collection of utility methods, all static.
 */
public class Utils {

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }

    /**
     * Returns the screen/display size.
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // You can get the height & width like such:
        // int width = size.x;
        // int height = size.y;
        return size;
    }

    public static int convertDpToPixel(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // get sum of a page
    public static long getPageSum(Activity act, int pageTableId){
        DB_page mDb_page = new DB_page(act, pageTableId);

        long sum = 0;

        mDb_page.open();
        int count = mDb_page.getNotesCount(false);
        for(int i=0;i<count;i++) {
            int checked = mDb_page.getNoteMarking(i,false);

            long value = 0;
            long qty = 0;
            if(mDb_page.getNoteBody(i, false) != 0)
                value = mDb_page.getNoteBody(i, false);

            if(mDb_page.getNoteQuantity(i, false)>0)
                qty = mDb_page.getNoteQuantity(i, false);

            if(checked==1)
                sum += (value*qty);
        }
        mDb_page.close();

        return sum;
    }

    // get sum of a folder
    public static long getFolderSum(Activity act,int folder_table_id){
        System.out.println("Utils / _getFolderSum / ");
        int folderSum = 0;
        DB_folder mDb_folder = new DB_folder(act, folder_table_id);

        mDb_folder.open();
        int pageCount = mDb_folder.getPagesCount(false);
        for(int i = 0; i< pageCount; i++){
            // get sum of each page
            int pageTableId = mDb_folder.getPageTableId(i,false);
            folderSum += Utils.getPageSum(act,pageTableId);
            System.out.println("Utils / _getFolderSum / pageTableId = " + pageTableId +
                    " , folderSum = " + folderSum);
        }
        mDb_folder.close();

        return folderSum;
    }

}
