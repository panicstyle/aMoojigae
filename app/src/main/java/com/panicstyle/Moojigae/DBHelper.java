package com.panicstyle.Moojigae;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "moojigae";

    private static final String TABLE_NAME = "ArticleRead";

    private static final String KEY_NO = "id";
    private static final String KEY_BOARD = "board";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_TABLE_DRINK =
                    "CREATE TABLE " + TABLE_NAME + "(" +
                            KEY_NO + " VARCHAR(100) PRIMARY KEY, " +
                            KEY_BOARD + " VARCHAR(100) NOT NULL, " +
                            "dt datetime default current_timestamp);";
            db.execSQL(CREATE_TABLE_DRINK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            String DROP_TABLE_DRINK =
                    "DROP TABLE IF EXISTS " + TABLE_NAME;
            db.execSQL(DROP_TABLE_DRINK);

            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(ArticleRead articleRead) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_NO, articleRead.getId());
            values.put(KEY_BOARD, articleRead.getBoard());

            db.insert(TABLE_NAME, null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean exist(String _id) {
        try {
            String SELECT_ALL = "SELECT * FROM " + TABLE_NAME + " WHERE id = '"+ _id + "'";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(SELECT_ALL, null);

            if (cursor.getCount() > 0) return true;
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void delete() {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calObj = Calendar.getInstance();
            calObj.add(Calendar.MONTH, -6);
    //        calObj.add(Calendar.MINUTE, -1);
            String currentDate = dateFormat.format(calObj.getTime());
            Log.e("current date", currentDate);

            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, "dt < datetime(?)", new String[] {currentDate});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

