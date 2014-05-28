package alypius.software.lifevault;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class ListActivity extends Activity
        implements OnItemClickListener, OnItemSelectedListener,
        OnItemLongClickListener {

	/* ---------------------------------------------------------------- */

    private DataSQLiteDB db;

    private ListView theList;

    private Cursor dbCursor;

    private ArrayList<HashMap<String, Object>> listItems;

    private Spinner spinner;

    private String sortBy = "title";

    private String[] arraySpinner = new String[3];

    private String[] arrayFields = new String[3];

    private final String PREF_NAME = "app_pref";

    private int selectedId = -1;



	/* --------------------- Building up methods---------------------------- */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        db = new DataSQLiteDB(this);

        //        createSmapleEntry();

        theList = (ListView) findViewById(R.id.the_list);


        // Create sort by values
        arraySpinner = new String[3];
        arraySpinner[0] = "Title";
        arraySpinner[1] = "Date";
        arraySpinner[2] = "Picture";

        arrayFields = new String[3];
        arrayFields[0] = "title";
        arrayFields[1] = "date";
        arrayFields[2] = "picture";

        // Fill spinner element
        spinner = (Spinner) findViewById(R.id.spinner);

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Get sort by preferences
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String prefSortBy = preferences.getString("sortBy", "title");
        sortBy = prefSortBy;


        // Show first start help message?
        int started = preferences.getInt("started", 0);
        if (started == 0) {
            // show help message
            Toast.makeText(this, "This looks like your first start. " +
                    "Check the help file, pressing the blue help button" +
                    " on the right bottom.", Toast.LENGTH_LONG).show();
            // Dont show help message again
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("started", 1); // value to store
            editor.commit();
        }


        // Set selected element for spinner
        int pos = getIndex(arrayFields, sortBy);
        spinner.setSelection(pos, true);

    }

    @Override
    public void onResume() {
        super.onResume();
        createUi();
    }

    /**
     * Creates an entry with predefined values
     */
    private void createSampleEntry() {
        // Create example entries
        ContentValues newEntry = new ContentValues();
        newEntry.put("title", "");
        newEntry.put("location", "");
        long lastId = db.add(newEntry);
        startEditActivity(Integer.parseInt(lastId + ""));
    }

    /**
     * Build up user Interface
     */
    private void createUi() {

        // Get all elements from database
        dbCursor = db.fetchAll(sortBy);

        // Add elements to list
        fillList();

    }

    /**
     * Creates for each element in the database
     * an list entry. If the picture doesnt exist
     * anymore a blank picture will be used.
     */
    private void fillList() {

        // Get column indexes for picture Cursor
        int columnIndexId = dbCursor.getColumnIndex(DataSQLiteDB.fields[0]);
        int columnIndexPicture = dbCursor.getColumnIndex(DataSQLiteDB.fields[5]);
        int coumnIndexTitle = dbCursor.getColumnIndex(DataSQLiteDB.fields[1]);
        int columnIndexDate = dbCursor.getColumnIndex(DataSQLiteDB.fields[2]);

        listItems = new ArrayList<HashMap<String, Object>>();

        // Go through all elements of the pictureCursor
        if (dbCursor.moveToFirst()) {
            do {
                HashMap<String, Object> oneItem = new HashMap<String, Object>();
                String picturePath = dbCursor.getString(columnIndexPicture);

                Bitmap thumb;
                if (picturePath != null && new File(picturePath).exists()) {
                    thumb = Helper.getImageFromFileName(picturePath);
                } else {
                    thumb = BitmapFactory.decodeResource(this.getResources(), R.drawable.blankpic);
                }
                oneItem.put("picture", thumb);


                // Get id
                int id = dbCursor.getInt(columnIndexId);
                oneItem.put("id", id);

                // Get title
                String title = dbCursor.getString(coumnIndexTitle);
                oneItem.put("title", title);

                // Get description
                String date = dbCursor.getString(columnIndexDate);
                oneItem.put("date", Helper.msToDate(date));

                // Add item to list
                listItems.add(oneItem);

            } while (dbCursor.moveToNext());
        }

        // Create a new SimpleAdapter and define fields + views
        String[] displayFields = new String[]{"picture", "title", "date"};
        int[] displayViews = new int[]{R.id.item_picture, R.id.item_title, R.id.item_date};

        SimpleAdapter listAdapter = new SimpleAdapter(this, listItems, R.layout.list_item, displayFields, displayViews);
        listAdapter.setViewBinder(new MyViewBinder());

        registerForContextMenu(theList);
        // Set Adapter to list
        theList.setAdapter(listAdapter);

        // Set Click Listeners
        theList.setOnItemClickListener(this);
        theList.setOnItemLongClickListener(this);
        theList.setOnCreateContextMenuListener(this);

    }

    /**
     * Long item click performed on list entry
     */

    public boolean onItemLongClick(AdapterView<?> parent, View view, int pos,
                                   long id) {
        selectedId = pos;
        return false;
    }

    /**
     * Create a context menu using menu_layout
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        menu.setHeaderTitle("Choose an option: ");
        inflater.inflate(R.menu.menu_layout, menu);
    }

    /**
     * Item selected from context menu,
     * either delete item or send item via email
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int entryId;
        switch (item.getItemId()) {
            case R.id.send_item:
                Toast.makeText(this, "Send via email", Toast.LENGTH_SHORT).show();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "My entry");
                sendIntent.putExtra(Intent.EXTRA_STREAM, "Content");
                sendIntent.setType("text/csv");

                return true;
            case R.id.delete_item:
                entryId = Integer.parseInt(listItems.get(selectedId).get("id") + "");
                db.delete(entryId);
                createUi();
                return true;
            case R.id.duplicate_item:
                entryId = Integer.parseInt(listItems.get(selectedId).get("id") + "");
                ContentValues values = db.getById(entryId);
                values.remove("_id");
                db.add(values);
                createUi();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Start new Activity that shows an entry
     * when a list item is clicked
     */

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> entry = listItems.get(position);
        startEditActivity(Integer.parseInt(entry.get("id") + ""));
    }

    /**
     * Start edit activtiy
     *
     * @param id
     */
    public void startEditActivity(int id) {
        Intent myIntent = new Intent(ListActivity.this, EditEntryActivity.class);
        myIntent.putExtra("id", id);
        startActivity(myIntent);
    }

    /**
     * Listen for changes in the sort by spinner.
     * When a new sorting order is selected,
     * save it permanently and update view.
     */

    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        // Get selected value
        String fieldName = arrayFields[position];
        this.sortBy = fieldName;

        // Save selected value to shared Preferences
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("sortBy", fieldName); // value to store
        editor.commit();

        // Repaint user interface
        createUi();
    }


    public void onNothingSelected(AdapterView<?> arg0) {

    }

	/* ---------------------------- Click Handler ---------------------------------- */

    /**
     * Listen for Clicks in UI
     */
    public void myClickHandler(View view) {
        int id = view.getId();

        // Add a new element
        if (id == R.id.text_plus || id == R.id.img_plus) {
            createSampleEntry();
            createUi();

            // Show help
        } else if (id == R.id.show_help) {
            Intent myIntent = new Intent(ListActivity.this, HelpActivity.class);
            startActivity(myIntent);
        }

    }

	/* ---------------------------- Help Methods ---------------------------------- */

    // Get index in String array
    private int getIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }


}