package org.monetaryc.monetaryb.monetarya;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
    CustomArrayAdapter customAdapter;
    ArrayList<String> list, idArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expenses_display);
        databaseHelper = new DatabaseHelper(this);

        expensesToTags();
        expensesToHome();

        listView = (ListView) findViewById(R.id.expensesList);
        list = populateRecentChargesArray();
        customAdapter = new CustomArrayAdapter(list, this);
        listView.setAdapter(customAdapter);
    }

    public void showAlertDialog(final Context context, int position) {
        final int pos = position;
        final String dbId = idArray.get(pos);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this charge?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                        try {
                            list.remove(pos);
                            customAdapter.notifyDataSetChanged();
                            sqLiteDatabase.execSQL("DELETE FROM expenses_table WHERE ExpenseID = " + dbId);
                            sqLiteDatabase.close();
                        }
                        catch(Exception e) {
                        }
                        Intent startIntent = new Intent(context, AllExpenses.class);
                        startActivity(startIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public ArrayList<String> populateRecentChargesArray() {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        ArrayList<String> localArray = new ArrayList<>();
        idArray = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from expenses_table order by expenseID desc limit 10", null);
        while(cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("ExpenseID"));
            String amount = cursor.getString(cursor.getColumnIndex("ExpenseValue"));
            String tags = cursor.getString(cursor.getColumnIndex("Tags"));
            String date = cursor.getString(cursor.getColumnIndex("Date"));
            localArray.add("$" + amount + "\n" + tags + "\n" + date + "\n");
            idArray.add(id);
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
