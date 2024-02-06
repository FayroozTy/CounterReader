package com.example.counterreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Counter.db";
    public static final String TABLE_NAME = "Counter";




    public static final String COL_1 = "ID";//service number
    public static final String COL_2 = "KEY_currentRead";
    public static final String COL_3 = "KEY_newtRead";

    public static final String COL_4 = "currentCounterNumber";
    public static final String COL_5 = "currentCounterDate";
    public static final String COL_6 = "newCounterNumber";

    public static final String COL_8 = "status";
    public static final String COL_9 = "notes";
    public static final String COL_10 = "service_number";
    public static final String COL_11 = "IsUploaded";
    public static final String COL_12 = "Latitude";
    public static final String COL_13 = "Longitude";


    ////

    public static final String TABLE_NAME2 = "Agreement";

    public static final String Agreements_COL_1 = "service_num";//service number
    public static final String Agreements_COL_2 = "user";
    public static final String Agreements_COL_3 = "emploee";

    public static final String Agreements_COL_4 = "status";
    public static final String Agreements_COL_5 = "location";
    public static final String Agreements_COL_6 = "rate";
    public static final String Agreements_COL_7 = "counterNumber";
    public static final String Agreements_COL_8 = "city";
    public static final String Agreements_COL_9 = "consume";




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,KEY_currentRead TEXT,KEY_newtRead TEXT,currentCounterNumber TEXT , currentCounterDate TEXT , newCounterNumber TEXT " +
                ", status INTEGER ,notes TEXT , service_number TEXT , IsUploaded TEXT, Latitude TEXT, Longitude TEXT)");

//
//       db.execSQL("create table " + TABLE_NAME2 +" (field1 INTEGER ,field2 TEXT,field3 TEXT,field4 TEXT , field5 TEXT , field6 TEXT " +
//                ", field7 INTEGER ,field8 TEXT , field9 TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
       // db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME2);
        onCreate(db);
    }


    public boolean insertData(String KEY_currentRead,String currentCounterNumber ,String currentCounterDate
            ,int status ,String notes ,String KEY_newtRead, String service_number, String isUploaded , String newCounterNumber, String lat , String longt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();


        contentValues.put(COL_2,KEY_currentRead);
        contentValues.put(COL_3,KEY_newtRead);
        contentValues.put(COL_4,currentCounterNumber);
        contentValues.put(COL_5,currentCounterDate);
        contentValues.put(COL_6,newCounterNumber);
       // contentValues.put(COL_7,newCounterDate);
        contentValues.put(COL_8,status);
        contentValues.put(COL_9,notes);
        contentValues.put(COL_10,service_number);
        contentValues.put(COL_11,isUploaded);

        contentValues.put(COL_12,lat);
        contentValues.put(COL_13,longt);

        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }



    public Cursor getAllDataFrom2() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME2,null);
        return res;
    }



    public Cursor getItemIdByPosition(int position) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
        cursor.moveToPosition(position);

        return cursor;
    }


    public boolean updateData(String id,String KEY_currentRead,String currentCounterNumber ,String currentCounterDate
            ,int status ,String notes ,String KEY_newtRead, String service_number, String isUploaded , String newCounterNumber, String lat , String longt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_1,id);
        contentValues.put(COL_2,KEY_currentRead);
        contentValues.put(COL_3,KEY_newtRead);
        contentValues.put(COL_4,currentCounterNumber);
        contentValues.put(COL_5,currentCounterDate);
        contentValues.put(COL_6,newCounterNumber);
        // contentValues.put(COL_7,newCounterDate);
        contentValues.put(COL_8,status);
        contentValues.put(COL_9,notes);
        contentValues.put(COL_10,service_number);
        contentValues.put(COL_11,isUploaded);
        contentValues.put(COL_12,lat);
        contentValues.put(COL_13,longt);


        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }
}