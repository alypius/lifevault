package alypius.software.lifevault.models;

import alypius.software.lifevault.DataSQLiteDB;
import alypius.software.lifevault.Helper;
import alypius.software.lifevault.factories.*;
import alypius.software.lifevault.interfaces.*;
import android.content.ContentValues;

public class DBRow implements IDBRow {
    //Database column names
    private final String TITLE_COL_NAME = "title";
    private final String LOCATION_COL_NAME = "location";
    private final String DESCRIPTION_COL_NAME = "text";
    private final String MAIN_IMAGE_COL_NAME = "picture";
    private final String IMAGE_COL_NAME = "pictures";
    private final String AUDIO_COL_NAME = "voice";
    private final String VIDEO_COL_NAME = "video";
    private final String CONTACTS_COL_NAME = "contacts";
    private final String ORIENTATION_COL_NAME = "orientation";
    private final String DATE_COL_NAME = "date";

    //Strings for each database column
    private IDBEntry<String> title;
    private IDBEntry<String> location;
    private IDBEntry<String> description;
    private IDBEntry<String> mainImage;
    private IDBArrayEntry<String> image;
    private IDBArrayEntry<String> audio;
    private IDBArrayEntry<String> video;
    private IDBArrayEntry<String> contact;
    private IOrientation orientation;
    private IDBEntry<String> date;

    private DataSQLiteDB appDB;
    private int entryId;

    public DBRow(DataSQLiteDB appDB, int entryId) {
        this.appDB = appDB;
        this.entryId = entryId;

        title = new DBEntry("");
        location = new DBEntry("");
        description = new DBEntry("");
        mainImage = new DBEntry("");
        image = new DBArrayEntry("");
        audio = new DBArrayEntry("");
        video = new DBArrayEntry("");
        contact = new DBArrayEntry("");
        orientation = OrientationFactory.create("");
        date = new DBEntry("");
    }

    public IDBEntry<String> getTitle() { return title; }

    public IDBEntry<String> getLocation() {
        return location;
    }

    public IDBEntry<String> getDescription() {
        return description;
    }

    public IDBEntry<String> getDate() {
        return date;
    }

    public IDBEntry<String> getMainImage() {
        return mainImage;
    }

    public IDBArrayEntry<String> getImage() {
        return image;
    }

    public IDBArrayEntry<String> getAudio() {
        return audio;
    }

    public IDBArrayEntry<String> getVideo() {
        return video;
    }

    public IDBArrayEntry<String> getContact() {
        return contact;
    }

    public IOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(IOrientation orientation) { this.orientation = orientation; }

    public void retrieveFromDB() {

        //Get database values for this entry
        ContentValues entryData = appDB.getById(entryId);
        title = new DBEntry(dbRowToString(entryData.get(TITLE_COL_NAME)));
        location = new DBEntry(dbRowToString(entryData.get(LOCATION_COL_NAME)));
        description = new DBEntry(dbRowToString(entryData.get(DESCRIPTION_COL_NAME)));
        date = new DBEntry(Helper.msToDate(dbRowToString(entryData.get(DATE_COL_NAME) + "")));
        mainImage = new DBEntry(dbRowToString(entryData.get(MAIN_IMAGE_COL_NAME)));
        image = new DBArrayEntry(dbRowToString(entryData.get(IMAGE_COL_NAME)));
        audio = new DBArrayEntry(dbRowToString(entryData.get(AUDIO_COL_NAME)));
        video = new DBArrayEntry(dbRowToString(entryData.get(VIDEO_COL_NAME)));
        contact = new DBArrayEntry(dbRowToString(entryData.get(CONTACTS_COL_NAME)));
        orientation = OrientationFactory.create(dbRowToString(entryData.get(ORIENTATION_COL_NAME)));
    }

    public void saveToDB() {

        //Update or add database row
        ContentValues entryData = new ContentValues();
        entryData.put(TITLE_COL_NAME, title.getEntry());
        entryData.put(LOCATION_COL_NAME, location.getEntry());
        entryData.put(DESCRIPTION_COL_NAME, description.getEntry());
        entryData.put(DATE_COL_NAME, date.getEntry());
        entryData.put(MAIN_IMAGE_COL_NAME, mainImage.getEntry());
        entryData.put(IMAGE_COL_NAME, image.getEntry());
        entryData.put(AUDIO_COL_NAME, audio.getEntry());
        entryData.put(VIDEO_COL_NAME, video.getEntry());
        entryData.put(CONTACTS_COL_NAME, contact.getEntry());
        entryData.put(ORIENTATION_COL_NAME, orientation.toDatabaseString());
        if (appDB.getById(entryId) == null) {
            appDB.add(entryData);
        } else {
            appDB.update(entryId, entryData);
        }
    }

    private String dbRowToString(Object row) {
        String rowString = row + "";
        return rowString.equals("null") ? "" : rowString;
    }
}
