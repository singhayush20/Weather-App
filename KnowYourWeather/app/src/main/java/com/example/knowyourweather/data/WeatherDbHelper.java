package com.example.knowyourweather.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.knowyourweather.data.WeatherContract.WeatherEntry;

import androidx.annotation.Nullable;
/*
Database helper class to create a new database and handle it
 */
public class WeatherDbHelper extends SQLiteOpenHelper {
    private static final String TAG=WeatherDbHelper.class.getSimpleName();
    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="weather.db";
    public WeatherDbHelper(@Nullable Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(TAG+" ###","entered the onCreate() method of WeatherDbHelper class");
        final String SQL_CREATE_WEATHER_TABLE =

                "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +

                        /*
                         * WeatherEntry did not explicitly declare a column called "_ID". However,
                         * WeatherEntry implements the interface, "BaseColumns", which does have a field
                         * named "_ID". We use that here to designate our table's primary key.
                         */
                        WeatherEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        WeatherEntry.COLUMN_DATE       + " INTEGER NOT NULL, "                 +

                        WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL,"                  +

                        WeatherEntry.COLUMN_MIN_TEMP   + " REAL NOT NULL, "                    +
                        WeatherEntry.COLUMN_MAX_TEMP   + " REAL NOT NULL, "                    +

                        WeatherEntry.COLUMN_HUMIDITY   + " REAL NOT NULL, "                    +
                        WeatherEntry.COLUMN_PRESSURE   + " REAL NOT NULL, "                    +

                        WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "                    +
                        WeatherEntry.COLUMN_DEGREES    + " REAL NOT NULL, "                    +

                        /*
                         * To ensure this table can only contain one weather entry per date, we declare
                         * the date column to be unique. We also specify "ON CONFLICT REPLACE". This tells
                         * SQLite that if we have a weather entry for a certain date and we attempt to
                         * insert another weather entry with that date, we replace the old weather entry.
                         */
                        " UNIQUE (" + WeatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";
        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
        Log.i(TAG+"###","weather data table created with String: \n"+SQL_CREATE_WEATHER_TABLE+
                "\nnow return from onCreate()");
    }
    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.i(TAG+"###","entered the onUpgrade() method in WeatherDbHelper class\n" +
                "oldVersion: "+oldVersion+" newVersion: "+newVersion);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        Log.i(TAG+"###","returning from the  onUpgrade() method in WeatherDbHelper class\n" +
                "oldVersion: "+oldVersion+" newVersion: "+newVersion);
    }
}
