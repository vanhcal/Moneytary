package org.monetaryc.monetaryb.monetarya;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Van on 6/22/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String database = "Expenses.db";

    public static final String expensesTable = "expenses_table";
    public static final String idColumn = "ExpenseID";
    public static final String valueColumn = "ExpenseValue";
    public static final String tagsColumn = "Tags";
    public static final String dateColumn = "Date";

    public static final String tagsTable = "tags_table";
    public static final String allTagsColumn = "AllTags";

    public DatabaseHelper(Context context) {
        super(context, database, null, 1);
    }

    private static final String createExpensesTable = "CREATE TABLE IF NOT EXISTS " +
            expensesTable + "(" + idColumn + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            valueColumn + " DOUBLE, " + tagsColumn + " TEXT, " + dateColumn + " TEXT " + ");";

    private static final String createTagsTable = "CREATE TABLE IF NOT EXISTS " + tagsTable + "(" + allTagsColumn + " TEXT" + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createExpensesTable);
        db.execSQL(createTagsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        System.out.println("Tables updating.");
        db.execSQL("DROP TABLE IF EXISTS " + expensesTable);
        db.execSQL("DROP TABLE IF EXISTS " + tagsTable);
        onCreate(db);
    }

    public boolean insertData (String expense, String tags, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(valueColumn, expense);
        contentValues.put(tagsColumn, tags);
        contentValues.put(dateColumn, date);
        long result = db.insert(expensesTable, null, contentValues);
        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean insertData (String tag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(allTagsColumn, tag);
        long result = db.insert(tagsTable, null, contentValues);
        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }
}
