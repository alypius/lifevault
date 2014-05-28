package alypius.software.lifevault;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//=============================================================================
public class DataSQLiteDB {

	/* -------------------------------------------------------------------------------- */

    public static final String DATABASE_NAME = "LifeVault.db";

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "Entries";

    public static final String[] fields =
            {"_id", "title", "date", "location", "text", "picture", "pictures",
                    "voice", "video", "contacts", "orientation"};

    public static final String[] fieldTypes =
            {"INTEGER", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT",
                    "TEXT", "TEXT", "TEXT", "TEXT"};

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "date TEXT NOT NULL," +
                    "location TEXT," +
                    "text TEXT," +
                    "picture TEXT," +
                    "pictures TEXT," +
                    "voice TEXT," +
                    "video TEXT, " +
                    "contacts TEXT, " +
                    "orientation TEXT" +
                    ");";

    private DatabaseHelper dbHelper;  // Private inner class

    private SQLiteDatabase theDB; // the actual Database

    private final String _ID = "_id";

	/* -------------------------------------------------------------------------------- */

    public DataSQLiteDB(Context theContext) {
        dbHelper = new DatabaseHelper(theContext);
        theDB = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
        theDB.close();
    }

	/* -------------------------------------------------------------------------------- */

    /**
     * Add an entry
     *
     * @return boolean
     */
    public long add(ContentValues data) {
        Long date = new Date().getTime();
        data.put("date", date);
        return theDB.insert(TABLE_NAME, null, data);
    }

    /**
     * Update an entry
     *
     * @return boolean
     */
    public boolean update(long id, ContentValues data) {
        Long date = System.currentTimeMillis();
        data.put("date", date);
        return (theDB.update(TABLE_NAME, data, _ID + " =" + id, null) > 0);
    }

    /**
     * Delete an entry
     *
     * @return boolean
     */
    public boolean delete(long id) {
        return (theDB.delete(TABLE_NAME, _ID + "=" + id, null) > 0);
    }

    /**
     * Get all Elements from database
     *
     * @return Cursor
     */
    public Cursor fetchAll(String orderBy) {
        return (theDB.query(TABLE_NAME, fields, null, null, null, null, orderBy));
    }

    /**
     * Get specific entry by id
     *
     * @param id
     * @return ContentValues
     */
    public ContentValues getById(int id) {

        Cursor cursor;
        ContentValues data;
        cursor = theDB.query(TABLE_NAME, null, _ID + " = " + id, null, null, null, null);
        data = valuesFromCursor(cursor);
        cursor.close();
        return (data);
    }


    /**
     * Create a ContentValues Object from the first element in Cursor
     *
     * @param cursor
     * @return ContentValues
     */
    private ContentValues valuesFromCursor(Cursor cursor) {

        String[] fieldNames;
        int index;
        ContentValues data;

        // Go to the first element
        if (cursor != null && cursor.moveToFirst()) {

            fieldNames = cursor.getColumnNames();
            data = new ContentValues();

            // Go through all fields and add values to ContentValues
            for (index = 0; index < fieldNames.length; index++) {
                String type = fieldTypes[index];
                String field = fields[index];

                if (type.equals("INTEGER")) {
                    data.put(field, cursor.getInt(index));
                } else if (type.equals("BLOB")) {
                    data.put(field, cursor.getBlob(index));
                } else {
                    data.put(field, cursor.getString(index));
                }

            }
            return (data);
        } else {
            return (null);
        }
    }
    //=============================================================================

    /**
     * Private inner class, overrides SQLLiteOpenHelper
     * in order to perferom onCreate and onUpgrade method
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private Context userContext;

        /**
         * Constructor
         *
         * @param context
         */
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            userContext = context;
        }

        /**
         * Execute create table commands
         */
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        /**
         * Upgrade Database version
         */
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            (new ContextWrapper(userContext)).deleteDatabase(DATABASE_NAME);
        }

    }
}
