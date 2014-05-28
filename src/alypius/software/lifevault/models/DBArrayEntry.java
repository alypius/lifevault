package alypius.software.lifevault.models;

import alypius.software.lifevault.interfaces.*;

public class DBArrayEntry implements IDBArrayEntry<String> {
    private final String DB_ROW_ENTRY_SEPARATOR = "*";
    private final String DB_ROW_ENTRY_SEPARATOR_PATTERN = "\\*";
    private String dbString;

    public DBArrayEntry(String dbString) {
        this.dbString = dbString;
    }

    public String getEntry() {
        return dbString;
    }

    public void setEntry(String entry) {
        this.dbString = dbString;
    }

    public String[] getArray() {
        return splitDBString(dbString);
    }

    public void setArrayElement(int index, String element) {
        String[] tempArray = getArray();
        tempArray[index] = element;
        dbString = buildDBString(tempArray);
    }

    public void appendArrayElement(String element) {
        dbString = appendToDBString(dbString, element);
    }

    private String[] splitDBString(String dbString) {
        return dbString.split(DB_ROW_ENTRY_SEPARATOR_PATTERN);
    }

    private String buildDBString(String urls[]) {
        String tempString = "";
        for (int i = 0; i < urls.length; i++) {
            if (!urls[i].equals("")) {
                tempString += urls[i] + DB_ROW_ENTRY_SEPARATOR;
            }
        }
        return !tempString.equals("") ? tempString.substring(0, tempString.length() - 1) : "";
    }

    private String appendToDBString(String dbString, String newText) {
        return dbString.equals("")
                ? newText
                : dbString + DB_ROW_ENTRY_SEPARATOR + newText;
    }
}
