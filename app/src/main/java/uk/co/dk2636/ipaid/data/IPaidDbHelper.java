package uk.co.dk2636.ipaid.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.co.dk2636.ipaid.data.IPaidContract.IPaidEntry;

public class IPaidDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = IPaidDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "IPaid.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link IPaidDbHelper}.
     *
     * @param context of the app
     */
    public IPaidDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
// Create a String that contains the SQL statement to create the payments table
        String SQL_CREATE_PAYMENTS_TABLE =  "CREATE TABLE " + IPaidEntry.TABLE_NAME + " ("
        /*
         * WeatherEntry did not explicitly declare a column called "_ID". However,
         * WeatherEntry implements the interface, "BaseColumns", which does have a field
         * named "_ID". We use that here to designate our table's primary key.
         */
                + IPaidEntry._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IPaidEntry.COLUMN_USER_NAME + " TEXT NOT NULL, "
                + IPaidEntry.COLUMN_DATE      + " INTEGER NOT NULL, "
                + IPaidEntry.COLUMN_AMOUNT    + " REAL NOT NULL DEFAULT 0,"
                /*
                 * To ensure this table can only contain one weather entry per date, we declare
                 * the date column to be unique. We also specify "ON CONFLICT REPLACE". This tells
                 * SQLite that if we have a weather entry for a certain date and we attempt to
                 * insert another weather entry with that date, we replace the old weather entry.
                 */
                + " UNIQUE (" + IPaidEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

        // Log on debug mode and Execute the SQL statement
        Log.d(LOG_TAG, SQL_CREATE_PAYMENTS_TABLE);
        db.execSQL(SQL_CREATE_PAYMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + IPaidEntry.TABLE_NAME);
        onCreate(db);

    }
}
