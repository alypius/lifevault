package alypius.software.lifevault.interfaces;

public interface IDBArray<T> {
    T[] getArray();
    void setArrayElement(int index, T element);
    void appendArrayElement(T element);
}
