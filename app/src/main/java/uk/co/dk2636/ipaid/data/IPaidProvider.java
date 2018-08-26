package uk.co.dk2636.ipaid.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import uk.co.dk2636.ipaid.data.IPaidContract.IPaidEntry;

public class IPaidProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = IPaidProvider.class.getSimpleName();

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int PAYMENTS = 100;
    public static final int PAYMENT_ID = 101;

    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of PaymentProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * Database helper that will provide us access to the database
     */
    private IPaidDbHelper iPaidDb;

    /**
     * Creates the UriMatcher that will match each URI to the PAYMENTS and
     * PAYMENT_ID constants defined above.
     * <p>
     * It's possible you might be thinking, "Why create a UriMatcher when you can use regular
     * expressions instead? After all, we really just need to match some patterns, and we can
     * use regular expressions to do that right?" Because you're not crazy, that's why.
     * <p>
     * UriMatcher does all the hard work for you. You just have to tell it which code to match
     * with which URI, and it does the rest automagically. Remember, the best programmers try
     * to never reinvent the wheel. If there is a solution for a problem that exists and has
     * been tested and proven, you should almost always use it unless there is a compelling
     * reason not to.
     *
     * @return A UriMatcher that correctly matches the constants for PAYMENTS and PAYMENT_ID
     */
    public static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = IPaidContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use PAYMENTS or PAYMENT_ID.
         */

        /* This URI is content:///uk.co.dk2636.ipaid/payments/ */
        matcher.addURI(authority, IPaidContract.PATH_PAYMENTS, PAYMENTS);

        /*
         * This URI would look something like content:/uk.co.dk2636.ipaid/payments/1472214172
         * The "/#" signifies to the UriMatcher that if PAYMENTS is followed by ANY number,
         * that it should return the PAYMENT_ID  code
         */
        matcher.addURI(authority, IPaidContract.PATH_PAYMENTS+ "/#", PAYMENT_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        iPaidDb = new IPaidDbHelper(getContext());
        return true;
    }

    /**
     * Handles query requests from clients. We will use this method to query for all
     * of our payment data as well as to query for the payment on a particular id.
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     *                      included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the
     *                      selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // DbHelper object initialised in onCreate method to be used throughout the class
        SQLiteDatabase db = iPaidDb.getReadableDatabase();
        Cursor cursor;
        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {
            /*
             * When sUriMatcher's match method is called with a URI that looks something like this
             *      content:/uk.co.dk2636.ipaid/payments/1472214172
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return the payment for a particular id.
             */
            case PAYMENT_ID: {
                selection = IPaidContract.IPaidEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(IPaidContract.IPaidEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case PAYMENTS: {
                cursor = db.query(
                        IPaidContract.IPaidEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
                default:
                    throw new IllegalArgumentException("Invalid URI:" + uri);
            }
        /* Set notification URI on the Cursor,
         * so we know what content URI the Cursor was created for.
         * If the data at this URI changes, then we knw we need to update the Cursor.
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * We aren't going to do anything with this method. However, we are required to
     * override it as IPaidProvider extends ContentProvider and getType is an abstract method in
     * ContentProvider. Normally, this method handles requests for the MIME type of the data at the
     * given URI. For example, if your app provided images at a particular URI, then you would
     * return an image URI from this method.
     *
     * @param uri the URI to query.
     * @return nothing, but normally a MIME type string, or null if there is no type.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in IPaid.");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PAYMENTS:
                return insertPayment(uri, contentValues);
            default:
                throw new IllegalArgumentException("Invalid URI, cannot insert:" + uri);
        }
    }

    /**
     * Insert a payment into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPayment(Uri uri, ContentValues values) {
        //call helper method to check for valid values.
        checkValues(values);
        // Get writable database
        SQLiteDatabase db = iPaidDb.getWritableDatabase();
        // If the ID is -1, then the insertion failed. Log an error and return null.
        long id = db.insert(IPaidEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notify all listeners that the data have changed for the payment content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase db = iPaidDb.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        // Track the number of rows that were deleted
        int rowsDeleted;

        switch (match) {
            case PAYMENTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(IPaidEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    //Notify all listeners that the data have changed for the pet content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case PAYMENT_ID:
                // Delete a single row given by the ID in the URI
                selection = IPaidEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(IPaidEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    //Notify all listeners that the data have changed for the pet content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PAYMENTS:
                return updatePayment(uri, contentValues, selection, selectionArgs);
            case PAYMENT_ID:
                // For the PAYMENT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = IPaidEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePayment(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Invalid URI, cannot update:" + uri);
        }

    }

    /**
     * Update a payment into the database with the given content values. Return the the number of rows affected.
     */
    private int updatePayment(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }
        // call helper method to check for valid values.
         checkValues(contentValues);
        SQLiteDatabase db = iPaidDb.getWritableDatabase();
        // If the ID is -1, then the insertion failed. Log an error and return null.
        // Returns the number of database rows affected by the update statement
        int  rowsUpdated = db.update(IPaidEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0 ) {
            //Notify all listeners that the data have changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        iPaidDb.close();
        super.shutdown();
    }

    /**
     * Perform data validation for the values
     */
    private void checkValues (ContentValues values) {
        StringBuffer str = new StringBuffer();
        // If the {@link IPaidEntry#COLUMN_USER_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(IPaidEntry.COLUMN_USER_NAME)) {
            String name = values.getAsString(IPaidEntry.COLUMN_USER_NAME);
            if (name == null || name.equals("")) {
                // throw new IllegalArgumentException("Pet requires a name");
                str.append("User name is required in order to complete a payment");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(IPaidEntry.COLUMN_DATE)) {
            Integer date = values.getAsInteger(IPaidEntry.COLUMN_DATE);
            //TODO check if the integer is a valid date
            if (date == null ) {
                //throw new IllegalArgumentException("Pet requires valid gender");
                str.append("Payment requires a valid date");
            }
        }

        // If the {@link IpaidEntry#COLUMN_AMOUNT} key is present,
        // check that the amount value is valid.
        if (values.containsKey(IPaidEntry.COLUMN_AMOUNT)) {
            // Check that the amount is greater than 2 and less than 10
            Double amount = values.getAsDouble(IPaidEntry.COLUMN_AMOUNT);
            if (( amount == null) ||  (amount < 2 || amount > 10)) {
                // throw new IllegalArgumentException("Payment requires a valid amount, 2 < amount < 10 ");
                str.append("Payment requires a valid amount, 2 < amount < 10");
            }
        }

        // No need to check the breed, any value is valid (including null).

        if (!(str == null || str.toString().equals(""))) {
            throw new IllegalArgumentException(str.toString());
        }
    }
}
