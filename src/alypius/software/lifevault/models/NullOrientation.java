package alypius.software.lifevault.models;

import alypius.software.lifevault.interfaces.IOrientation;

public class NullOrientation implements IOrientation {
    public String toDatabaseString() {
        return "";
    }

    public String toFormattedString() {
        return "Not available";
    }

    public float[] getCoords() {
        return new float[]{};
    }

    public boolean isNull() {
        return true;
    }

    public boolean isEqual(IOrientation orientation) {
        return isNull() == orientation.isNull();
    }
}
