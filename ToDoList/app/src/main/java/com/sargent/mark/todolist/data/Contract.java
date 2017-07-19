package com.sargent.mark.todolist.data;

import android.provider.BaseColumns;

/**
 * Created by mark on 7/4/17.
 */

public class Contract {

    public static class TABLE_TODO implements BaseColumns{
        public static final String TABLE_NAME = "todoitems";

        // these are the column names for the table in the database
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_DUE_DATE = "duedate";

        // these are the two columns that were added to showcase categories and completion of tasks
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_DONE = "done";
    }
}
