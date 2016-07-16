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
    String [] inputedTags;
    ArrayList<String> parsedTags = new ArrayList<>();

    DatabaseHelper databaseHelper;
    EditText valueEntered, tagsEntered;
    TextView greeting, toAllTags, toAllExpenses;

    Button setDate;
    int year, month, day;
    static final int dialogId = 0;
    String datePicked;
    enum PeriodOfTime {MONTH, YEAR};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        databaseHelper = new DatabaseHelper(this);

        initializeTags();
        startCalendarOnToday();

        greeting = (TextView) findViewById(R.id.helloText);
        greeting.setText("\"Hello! What expense would you like to record today?\"");

        homeToTags();
        homeToExpenses();
    }

    // Populate existing tags, put them into adapter, set up autocomplete
    public void initializeTags() {
        allTags = populateArray(databaseHelper);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, allTags);
        adapter.notifyDataSetChanged();

        MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView)
                findViewById(R.id.editTags);
        textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        textView.setAdapter(adapter);
    }

    // Getting tags from database into current tags array
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

    // Set the calendar dialog to start on the correct date
    public void startCalendarOnToday() {
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDialogOnButtonClick();
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

    public double parseValue() {
        valueEntered = (EditText) findViewById(R.id.enterValue);
        double value;
        // Remember: when parsing anything, to catch exceptions
        try {
            value = Double.parseDouble(valueEntered.getText().toString().trim());
        } catch (NumberFormatException e) {
            value = -1;
        }
        return value;
    }

    public void parseTags() {
       String tags = tagsEntered.getText().toString().trim();

       // If the last character entered was a comma, get rid of it
       if (tags.charAt(tags.length() - 1) == ',') {
           tags = tags.substring(0, tags.length() - 1);
       }
       inputedTags = tags.split(",");

       for (int i = 0; i < inputedTags.length; i++) {
           String tag = inputedTags[i].trim();
           tag = tag.substring(0, 1).toUpperCase() + tag.substring(1);
           parsedTags.add(tag);
       }
   }

    // Called when user clicks send
    public void sendMessage(View view) {
        double value = parseValue();
        String string = Double.toString(value);
        tagsEntered = (EditText) findViewById(R.id.editTags);

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
            parseTags();
            addNewTags();
            printDataAndInsert();
            startNextActivity();
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

    // If a tag is not found, add it to the Tags array
    public void addNewTags() {
        for (int i = 0; i < parsedTags.size(); i++) {
            String tag = parsedTags.get(i);

            boolean found = false;
            for (int j = 0; j < allTags.size(); j++) {
                if (tag.equals(allTags.get(j))) {
                    System.out.println("FOUND " + tag + "/" + allTags.get(j));
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("NOT FOUND " + tag);
                allTags.add(tag);
                adapter.add(tag);
                adapter.notifyDataSetChanged();
                databaseHelper.insertData(tag);
            }
        }
    }

    // Calculate how much was spent this month and this year
    public String getTotalForTime(PeriodOfTime periodOfTime) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        Cursor cursor = null;
        if (periodOfTime.equals(PeriodOfTime.MONTH)) {
            cursor = sqLiteDatabase.rawQuery("select sum(expenseValue) from expenses_table where substr(date,6,2) == strftime('%m','now')", null);
        }
        else if (periodOfTime.equals(PeriodOfTime.YEAR)) {
            cursor = sqLiteDatabase.rawQuery("select sum(expenseValue) from expenses_table where substr(date,1,4) == strftime('%Y','now')", null);
        }
        String string = null;
        while(cursor.moveToNext()) {
            string = cursor.getString(0);
        }
        cursor.close();
        return string;
    }

    // Calculate how much was spent per inputed tag this month
    public ArrayList<String> getValuePerTag(String [] inputedTags) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        ArrayList<String> localArray = new ArrayList<>();
        for (int i = 0; i < parsedTags.size(); i++) {
            String tag = parsedTags.get(i);
            Cursor cursor = sqLiteDatabase.rawQuery("select sum(expenseValue) from expenses_table " +
                    "where tags like '%" + tag + "%' and substr(date,6,2) == strftime('%m','now')", null);
            while(cursor.moveToNext()) {
                // 0 refers to Column0 or the fact that this is the only info returned, not index0
                String amount = cursor.getString(0);
                System.out.println("You have spent $" + amount + " on " + tag + " this month.");
                localArray.add("You have spent $" + amount + " on " + tag + " this month.");
            }
            cursor.close();
        }
        return localArray;
    }

    // Gather strings from the above two methods for printing
    public ArrayList<String> getStringsToPrint() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("You have spent $" + getTotalForTime(PeriodOfTime.MONTH) + " this month.");
        arrayList.add("You have spent $" + getTotalForTime(PeriodOfTime.YEAR) + " this year.");

        ArrayList<String> tagStatements = getValuePerTag(inputedTags);
        for (int i = 0; i < tagStatements.size(); i++) {
            arrayList.add(tagStatements.get(i));
        }
        return arrayList;
    }

    public void printDataAndInsert() {
        // Prepare inputed tags for printing on page 2
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < parsedTags.size(); k++) {
            String tag = parsedTags.get(k);
            sb.append(tag + ", ");
        }
        sb.delete(sb.length() - 2, sb.length() - 1);

        // If we successfully insert into the databaseHelper, toast how much we've spent
        boolean isInserted = databaseHelper.insertData(valueEntered.getText().toString(), sb.toString(), datePicked);
        if (isInserted) {
            String valueString = valueEntered.getText().toString();
            Toast.makeText(this.getBaseContext(), "You have spent $" + valueString + " on " + sb.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void startNextActivity() {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("key", getStringsToPrint());
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void homeToTags() {
        toAllTags = (TextView) findViewById(R.id.homeToAllTags);
        toAllTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(view.getContext(), AllTags.class);
                startActivity(startIntent);
            }
        });
    }

    public void homeToExpenses() {
        toAllExpenses = (TextView) findViewById(R.id.homeToAllCharges);
        toAllExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(view.getContext(), AllExpenses.class);
                startActivity(startIntent);
            }
        });
    }
}


