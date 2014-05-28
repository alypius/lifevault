package alypius.software.lifevault.interfaces;

public interface IOrientation {
    String toDatabaseString();

    String toFormattedString();

    float[] getCoords();

    boolean isNull();

    boolean isEqual(IOrientation orientation);
}

