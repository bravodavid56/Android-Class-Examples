package com.sargent.mark.todolist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;
import com.sargent.mark.todolist.data.ToDoItem;

import java.util.ArrayList;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ItemHolder> {

    private Cursor cursor;
    private ItemClickListener listener;
    private String TAG = "todolistadapter";

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public interface ItemClickListener {
        // classes that implement this interface must implement the onItemClick method that
        // now includes 'category' and 'done'
        void onItemClick(int pos, String description, String duedate, String category,int done,long id);
    }

    public ToDoListAdapter(Cursor cursor, ItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    // ItemHolder now has Checkbox, category, and done as variables
    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView descr;
        TextView due;
        TextView cate;
        CheckBox ch;
        String duedate;
        String description;
        String category;
        int done;
        long id;


        ItemHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            // sets the variables in the ItemHolder for category and checkbox
            cate = (TextView) view.findViewById(R.id.category);
            ch = (CheckBox) view.findViewById(R.id.description);
            view.setOnClickListener(this);
        }

        public void bind(ItemHolder holder, int pos) {
            cursor.moveToPosition(pos);
            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);

            duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
            description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));

            // gets the category and done columns from the cursor just like the duedate and description
            category = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY));
            done = cursor.getInt(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DONE));

            descr.setText(description);
            due.setText(duedate);

            // to maintain consistency, this part sets the CheckBox to checked if the item is done,
            // and unchecked if the item is not done
            cate.setText(category);
            if (done == 0) {
                ch.setChecked(false);
            } else {
                ch.setChecked(true);
            }


            // changes the color of the items based on their category
            // for clarity purposes
            if (category.equals("Finance")) {
                itemView.setBackgroundColor(0xFFaef98e);
            } else if (category.equals("School")) {
                itemView.setBackgroundColor(0xFFfc8f99);
            } else if (category.equals("Other")) {
                itemView.setBackgroundColor(0xFFcb91ff);
            } else if (category.equals("Personal")) {
                itemView.setBackgroundColor(0xFFeaef62);
            } else if (category.equals("Default")) {
                itemView.setBackgroundColor(0xFF5bb1ef);
            }

            // when a checkbox is checked, the actual todoitem is updated in the database
            // to maintain consistency
            // this way, the 'checked' version remains consistent
            ch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBHelper helper = new DBHelper(v.getContext());
                    SQLiteDatabase db = helper.getWritableDatabase();
                    if (ch.isChecked()) {
                        db.execSQL("UPDATE todoitems set done=1 where _id" +
                                "=\"" + id +"\"");
                    } else if (!ch.isChecked()) {
                        db.execSQL("UPDATE todoitems set done=0 where _id" +
                                "=\"" + id +"\"");
                    }
                }
            });
            holder.itemView.setTag(id);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            // include the 'category' and 'done' value on the listener's onItemClick method
            listener.onItemClick(pos, description, duedate, category,done,id);

        }
    }

}
