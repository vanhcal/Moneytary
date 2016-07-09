package org.monetaryc.monetaryb.monetarya;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.monetaryc.monetaryb.monetarya.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


public class MyActivity extends FragmentActivity {
    static ArrayAdapter<String> adapter;
    static ArrayList<String> allTags = new ArrayList<>();

    DatabaseHelper databaseHelper;
    EditText valueEntered, tagsEntered;
    TextView greeting;

    Button setDate;
    int year, month, day;
    static final int dialogId = 0;
    String datePicked;

    String [] inputedTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        databaseHelper = new DatabaseHelper(this);

        allTags = populateArray(databaseHelper);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, allTags);

        adapter.notifyDataSetChanged();

        MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView)
                findViewById(R.id.editTags);
        textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        textView.setAdapter(adapter);

        // setting the Calendar popup to start at the correct date
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDialogOnButtonClick();

        greeting = (TextView) findViewById(R.id.helloText);
        greeting.setText("\"Hello! What expense would you like to record today?\"");
    }

    // Getting SQL tags into the current tags array
    public ArrayList<String> populateArray (DatabaseHelper helper) {
        SQLiteDatabase sqLiteDatabase = helper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from tags_table", null);

        String [] tags = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            String string = cursor.getString(cursor.getColumnIndex("AllTags"));
            tags[i] = string;
            i++;
        }
        ArrayList<String> tagsList = new ArrayList<>(Arrays.asList(tags));

        for (int j = 0; j < tagsList.size(); j++) {
            System.out.print("TAGS " + tagsList.get(j) + " ");
        }

        return tagsList;
    }

    public void showDialogOnButtonClick() {
        setDate = (Button) findViewById(R.id.setDateButton);

        setDate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(dialogId);
                    }
                }
        );
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == dialogId) {
            return new DatePickerDialog(this, dpickerListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int yearPicked, int monthPicked, int dayPicked) {
            year = yearPicked;
            month = monthPicked + 1;
            day = dayPicked;
            Toast.makeText(MyActivity.this, "Selected date: " + month + " / " + day + " / " + year, Toast.LENGTH_LONG).show();
            datePicked = createDateString(year, month, day);
        }
    };

    public String createDateString(int year, int month, int day) {
        StringBuilder sb = new StringBuilder();
        sb.append(year + "-");
        if (month < 10) {
            sb.append("0" + month + "-");
        } else {
            sb.append(month + "-");
        }
        if (day < 10) {
            sb.append("0" + day);
        } else {
            sb.append(day);
        }
        return sb.toString();
    }

    /**
     * Called when the user clicks the Send button
     */
    public void sendMessage(View view) {
        tagsEntered = (EditText) findViewById(R.id.editTags);

        // Get the value entered, translate to string, parse into int
        valueEntered = (EditText) findViewById(R.id.enterValue);

        String string = valueEntered.getText().toString().trim();
        double value;
        // Remember: when parsing anything, to catch exceptions
        try {
            value = Double.parseDouble(valueEntered.getText().toString());
        } catch (NumberFormatException e) {
            value = -1;
        }

        // All fields must be completed or else some combination of errorStrings will be toasted
        boolean allFieldsCompleted = true;
        ArrayList<String> errorStrings = new ArrayList<>();
        if (string == null || string.length() == 0 || string.equals("") || value <= 0) {
            errorStrings.add("Enter a numerical value.");
            allFieldsCompleted = false;
        }
        if (tagsEntered.getText().toString().length() <= 0) {
            errorStrings.add("Enter at least one tag.");
            allFieldsCompleted = false;
        }
        if (datePicked == null) {
            errorStrings.add("Choose a date.");
            allFieldsCompleted = false;
        }

        if (allFieldsCompleted) {
            // Get individual tags entered
            String tags = tagsEntered.getText().toString().trim();
            if (tags.charAt(tags.length() - 1) == ',') {
                tags = tags.substring(0, tags.length() - 1);
            }
            inputedTags = tags.split(",");

            // If a tag is not found, add it to the Tags array
            for (int i = 0; i < inputedTags.length; i++) {
                boolean found = false;
                for (int j = 0; j < allTags.size(); j++) {
                    if (inputedTags[i].trim().equals(allTags.get(j))) {
                        System.out.println("FOUND " + inputedTags[i] + "/" + allTags.get(j));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("NOT FOUND " + inputedTags[i]);
                    allTags.add(inputedTags[i].trim());
                    adapter.add(inputedTags[i].trim());
                    adapter.notifyDataSetChanged();
                    databaseHelper.insertData(inputedTags[i].trim());
                }
            }

            // Prepare inputed tags for printing
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < inputedTags.length; k++) {
                sb.append(inputedTags[k] + ", ");
            }
            sb.delete(sb.length() - 2, sb.length() - 1);

            // If we successfully insert into the databaseHelper, toast how much we've spent
            boolean isInserted = databaseHelper.insertData(valueEntered.getText().toString(), tagsEntered.getText().toString(), datePicked);
            if (isInserted) {
                String valueString = valueEntered.getText().toString();
                Toast.makeText(this.getBaseContext(), "You have spent $" + valueString + " on " + sb.toString(), Toast.LENGTH_LONG).show();
            }

            //Start the next activity
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("key", getStringsToPrint());
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        // If not all fields are completed, toast the appropriate error messages.
        else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < errorStrings.size(); i++) {
                sb.append(errorStrings.get(i));
                if (i != errorStrings.size() - 1) {
                        sb.append("\n");
                }
            }
            Toast.makeText(this.getBaseContext(), sb.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public String getTotalForMonth() {
        // Total amount spent this month
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select sum(expenseValue) from expenses_table where substr(date,6,2) == strftime('%m','now')", null);
        String string = null;
        while(cursor.moveToNext()) {
            string = cursor.getString(0);
            System.out.println("MONTH SUM " + string);
        }
        cursor.close();
        return string;
    }

    public String getTotalForYear() {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select sum(expenseValue) from expenses_table where substr(date,1,4) == strftime('%Y','now')", null);
        String string = null;
        while(cursor.moveToNext()) {
            string = cursor.getString(0);
            System.out.println("YEAR SUM " + string);
        }
        cursor.close();
        return string;
    }

    public ArrayList<String> getValuePerTag(String [] inputedTags) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        ArrayList<String> localArray = new ArrayList<>();
        for (int i = 0; i < inputedTags.length; i++) {
            String tag = inputedTags[i];
            Cursor cursor = sqLiteDatabase.rawQuery("select sum(expenseValue) from expenses_table " +
                    "where tags like '%" + tag + "%' and substr(date,6,2) == strftime('%m','now')", null);
            while(cursor.moveToNext()) {
                String amount = cursor.getString(0);
                System.out.println("You have spent $" + amount + " on " + tag + " this month.");
                localArray.add("You have spent $" + amount + " on " + tag + " this month.");
            }
            cursor.close();
        }
        return localArray;
    }

    public ArrayList<String> getStringsToPrint() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("You have spent $" + getTotalForMonth() + " this month.");
        arrayList.add("You have spent $" + getTotalForYear() + " this year.");

        ArrayList<String> tagStatements = getValuePerTag(inputedTags);
        for (int i = 0; i < tagStatements.size(); i++) {
            arrayList.add(tagStatements.get(i));
        }
        return arrayList;
    }
}

