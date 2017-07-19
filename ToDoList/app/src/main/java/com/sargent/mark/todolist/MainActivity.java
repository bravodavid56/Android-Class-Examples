package com.sargent.mark.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;


import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;

import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity implements AddToDoFragment.OnDialogCloseListener, UpdateToDoFragment.OnUpdateDialogCloseListener{

    private RecyclerView rv;
    private FloatingActionButton button;
    private DBHelper helper;
    private Cursor cursor;
    private SQLiteDatabase db;
    ToDoListAdapter adapter;
    private final String TAG = "mainactivity";

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {}
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate called in main activity");
        button = (FloatingActionButton) findViewById(R.id.addToDo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddToDoFragment frag = new AddToDoFragment();
                frag.show(fm, "addtodofragment");
            }
        });
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // this spinner indicates which items are shown in the main activity view
        // just like in the AddToDoFragment and UpdateToDoFragment, the spinner is accessed the same
        // get the spinner using its id
        // notice this spinner gets a different spinner than the one in the fragments
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        // we name this adapter spinner_adapter to differentiate between the spinner adapter
        // and the recyclerview adapter
        ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(this,
                R.array.todo_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // when a spinner item is selected, we have to filter the items shown by implementing the
        // listener for spinners, and then implementing the spinner activity class that includes these methods
        // create a new SpinnerActivity and define the onItemSelected method
        spinner.setOnItemSelectedListener(new SpinnerActivity() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // check if the default item is selected
                // if it is, getAllItems from the db to display them all, and swap the cursor (force it to close
                // so we dont have open cursors
                if (parentView.getSelectedItem().toString().equals("Default")) {
                    cursor = getAllItems(db);
                    adapter.swapCursor(cursor);
                } else {
                    // if the default item is not selected (i.e. if a user selects 'School' as the item
                    // then use getSomeItems to specify which items to retrieve from the database
                    // i.e. only items that match the selected category
                    cursor = getSomeItems(db, parentView.getSelectedItem().toString());
                    // swap the cursor again to avoid open cursors
                    adapter.swapCursor(cursor);
                }

                Log.e(TAG, "onItemSelected: "+ parentView.getItemAtPosition(position).toString());
            }

            // if nothing is selected, then show all of the items in the main activity
            // (realistically, this will never be triggered since the 'default' option is always selected by default on create
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                cursor = getAllItems(db);
                adapter.swapCursor(cursor);
            }
        });
        // apply the adapter to the spinner
        spinner.setAdapter(spinner_adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();
        cursor = getAllItems(db);
        final CheckBox ch = (CheckBox) findViewById(R.id.description);

        //
        adapter = new ToDoListAdapter(cursor, new ToDoListAdapter.ItemClickListener() {

            // when a user clicks on an item, get its different values to preset it
            // that way when you update, you retain its old informtaion
            // example, you dont have to write the description and the due date again
            @Override
            public void onItemClick(int pos, String description, String duedate, String category,int done,long id) {
                Log.d(TAG, "item click id: " + id);
                String[] dateInfo = duedate.split("-");
                int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                FragmentManager fm = getSupportFragmentManager();
                // create the fragment indicating the update screen
                UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description, category,done, id);
                // show the actual fragment
                frag.show(fm, "updatetodofragment");
            }
        });

        rv.setAdapter(adapter);

        // handles the swiping of the item including deletion
        // this was provided from Mark
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                removeToDo(db, id);
                adapter.swapCursor(getAllItems(db));
            }
        }).attachToRecyclerView(rv);
    }

    // closing the dialog is the action that adds the item to the database by calling addToDo with its various parameters
    @Override
    public void closeDialog(int year, int month, int day, String description, String category, int done) {
        addToDo(db, description, formatDate(year, month, day), category);
        // after adding an item, showcase all the items to show that it was indeed added
        cursor = getAllItems(db);

        adapter.swapCursor(cursor);
    }
    // formats the date for sql syntax (provided by Mark)
    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }


    // this is the actual filter for displaying the items
    // passing in a category (via the spinner in the main Activity) creates a query that only returns items
    // whose category matches the one selected on the spinner
    private Cursor getSomeItems(SQLiteDatabase db, String category) {
        return db.query(
          Contract.TABLE_TODO.TABLE_NAME,
                null,
                "category="+"\""+category+"\"",
                null,
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
        );
    }

    // this method is the more general case of getSomeItems
    // it returns a cursor after a query of all items in the database
    private Cursor getAllItems(SQLiteDatabase db) {
        return db.query(
                Contract.TABLE_TODO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
        );
    }

    // creates a record to add into the database
    private long addToDo(SQLiteDatabase db, String description, String duedate, String category) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DONE, 0);
        return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);
    }

    // called by the swiping motion on the item
    // it looks for the record in the database and removes the item from the database
    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }


    // used to update the record in the database by closeUpdateDialog
    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description, String category, long id){

        String duedate = formatDate(year, month - 1, day);

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);

        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }

    // does the actual updating of the record in the database
    @Override
    public void closeUpdateDialog(int year, int month, int day, String description,String category,int done, long id) {
        updateToDo(db, year, month, day, description, category, id);
        adapter.swapCursor(getAllItems(db));
    }
}
