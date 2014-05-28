package alypius.software.lifevault.interfaces;

public interface IDBRow {
    IDBEntry<String> getTitle();
    IDBEntry<String> getLocation();
    IDBEntry<String> getDescription();
    IDBEntry<String> getDate();
    IDBEntry<String> getMainImage();
    IDBArrayEntry<String> getImage();
    IDBArrayEntry<String> getAudio();
    IDBArrayEntry<String> getVideo() ;
    IDBArrayEntry<String> getContact();
    IOrientation getOrientation() ;
    void setOrientation(IOrientation orientation);
    void retrieveFromDB();
    void saveToDB();
}
