package com.sargent.mark.todolist;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ConfigurationHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

/**
 * Created by mark on 7/5/17.
 */

// this is the fragment that appears when a user clicks on an item in the main activity
public class UpdateToDoFragment extends DialogFragment {

    private EditText toDo;
    private DatePicker dp;
    private CheckBox ch;
    private Button add;
    private final String TAG = "updatetodofragment";
    private long id;


    public UpdateToDoFragment(){}

    // newInstance now includes the 'category' and 'done' parameter
    public static UpdateToDoFragment newInstance(int year, int month, int day, String descrpition,
                                                 String category, int done, long id) {
        UpdateToDoFragment f = new UpdateToDoFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        args.putLong("id", id);
        args.putString("description", descrpition);
        // include the 'category' and 'done' key in the bundle to access the values elsewhere
        args.putString("category", category);
        args.putInt("done", done);

        f.setArguments(args);

        return f;
    }

    //To have a way for the activity to get the data from the dialog
    public interface OnUpdateDialogCloseListener {
        // the closeUpdateDialog includes the 'category' and 'done' parameters to accurately update items
        void closeUpdateDialog(int year, int month, int day, String description,
                               String category,int done,long id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_adder, container, false);
        toDo = (EditText) view.findViewById(R.id.toDo);
        dp = (DatePicker) view.findViewById(R.id.datePicker);
        add = (Button) view.findViewById(R.id.add);
        // retrieve the CheckBox and assign it to the private variable, just like the other widgets
        ch = (CheckBox) view.findViewById(R.id.description);

        // implement the spinner similar to the AddToDoFragment spinner
        // however, since we get the selectedItem from the spinner in the listener, we have to set
        // this spinner as final to make sure the referenced spinner does not change
        // get the spinenr by id
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner_add);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.todo_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        spinner.setAdapter(adapter);


        int year = getArguments().getInt("year");
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");
        id = getArguments().getLong("id");
        String description = getArguments().getString("description");
        dp.updateDate(year, month, day);

        toDo.setText(description);

        add.setText("Update");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateToDoFragment.OnUpdateDialogCloseListener activity = (UpdateToDoFragment.OnUpdateDialogCloseListener) getActivity();
                Log.d(TAG, "id: " + id);
                // closeUpdateDialog includes the category and done parameters
                // it retrieves the category from the selected spinner item
                activity.closeUpdateDialog(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), toDo.getText().toString(),
                        spinner.getSelectedItem().toString(), 0, id);
                UpdateToDoFragment.this.dismiss();
            }
        });

        return view;
    }
}