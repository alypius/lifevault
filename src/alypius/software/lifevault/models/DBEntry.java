package alypius.software.lifevault.models;

import alypius.software.lifevault.interfaces.IDBEntry;

public class DBEntry implements IDBEntry<String> {
    private String dbString;

    public DBEntry(String dbString) {
        this.dbString = dbString;
    }

    public String getEntry() {
        return dbString;
    }

    public void setEntry(String entry) {
        dbString = entry;
    }
}
