package org.monetaryc.monetaryb.monetarya;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.monetaryc.monetaryb.monetarya.R;

import java.util.ArrayList;

/**
 * Created by Van on 7/13/2016.
 */
public class AllTags extends AppCompatActivity {
    TextView tags, restart, tagsToExpenses, mainText;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tags_display);
        databaseHelper = new DatabaseHelper(this);

        tags = (TextView) findViewById((R.id.tagsDisplay));
        tags.setMovementMethod(new ScrollingMovementMethod());

        mainText = (TextView) findViewById(R.id.tagsDisplay);
        StringBuilder sb = new StringBuilder();
        sb.append("You have spent: " + "\n" + "\n");
        ArrayList<String> array = valuePerTagUsedinMonth();
        for (int i = 0; i < array.size(); i++) {
            sb.append("•  " + array.get(i) + "\n" + "\n");
        }
        mainText.setText(sb.toString());
        mainText.setMovementMethod(new ScrollingMovementMethod());

        tagsToExpenses();
        tagsToHome();
    }

    // Calculate how much was spent for EACH tag used this month
    public ArrayList<String> valuePerTagUsedinMonth () {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        ArrayList<String> allTags = new ArrayList<>();
        ArrayList<String> allTagStatements = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.rawQuery("select * from tags_table", null);
        while(cursor.moveToNext()) {
            allTags.add(cursor.getString(0));
        }
        cursor.close();

        for (int i = 0; i < allTags.size(); i++) {
            String tag = allTags.get(i);
            Cursor cursorB = sqLiteDatabase.rawQuery("select sum(expenseValue) from expenses_table " +
                    "where tags like '%" + tag + "%' and substr(date,6,2) == strftime('%m','now')", null);
            while(cursorB.moveToNext()) {
                String amount = cursorB.getString(0);
                allTagStatements.add("$" + amount + " on " + tag + " this month.");
            }
            cursorB.close();
        }
        return allTagStatements;
    }

    public void tagsToExpenses() {
        tagsToExpenses = (TextView) findViewById(R.id.tagsToExpenses);
        tagsToExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(view.getContext(), AllExpenses.class);
                startActivity(startIntent);
            }
        });
    }

    public void tagsToHome() {
        restart = (TextView) findViewById(R.id.tagsToHome);
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
