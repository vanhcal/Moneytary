package org.monetaryc.monetaryb.monetarya;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.monetaryc.monetaryb.monetarya.R;

import java.util.ArrayList;

/**
 * Created by Van on 7/13/2016.
 */
public class AllExpenses extends AppCompatActivity {
    TextView restart, expensesToTags;
    ListView listView;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expenses_display);
        databaseHelper = new DatabaseHelper(this);

        expensesToTags();
        expensesToHome();

        listView = (ListView) findViewById(R.id.expensesList);
        ArrayList<String> list = populateRecentChargesArray();

        CustomArrayAdapter customAdapter = new CustomArrayAdapter(list, this);

        //handle listview and assign adapter
        listView.setAdapter(customAdapter);
    }

    public ArrayList<String> populateRecentChargesArray() {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        ArrayList<String> localArray = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from expenses_table order by expenseID desc limit 10", null);
        while(cursor.moveToNext()) {
            String amount = cursor.getString(cursor.getColumnIndex("ExpenseValue"));
            String tags = cursor.getString(cursor.getColumnIndex("Tags"));
            String date = cursor.getString(cursor.getColumnIndex("Date"));
            localArray.add("$" + amount + "\n" + tags + "\n" + date + "\n");
        }
        cursor.close();
        return localArray;
    }

    public void expensesToTags() {
        expensesToTags = (TextView) findViewById(R.id.expensesToTags);
        expensesToTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(view.getContext(), AllTags.class);
                startActivity(startIntent);
            }
        });
    }

    public void expensesToHome() {
        restart = (TextView) findViewById(R.id.expensesToHome);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(view.getContext(), MyActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            }
        });
    }
}
