package alypius.software.lifevault.models;

import alypius.software.lifevault.interfaces.IOrientation;

public class OrientationEvent implements IOrientation {

    private float x, y, z;
    private final String COORD_FORMAT = "%5.1f";
    private final float EQUAL_THRESHOLD = 5.0f;

    public OrientationEvent(float[] coords) {
        if (coords.length != 3 ) throw new IllegalArgumentException("Expecting an array with three elements");
        this.x = coords[0];
        this.y = coords[1];
        this.z = coords[2];
    }

    public OrientationEvent(String coordString) {
        String[] coords = coordString.split(",");
        if (coords.length != 3) throw new IllegalArgumentException("Expecting a string with three coords");
        this.x = Float.parseFloat(coords[0]);
        this.y = Float.parseFloat(coords[1]);
        this.z = Float.parseFloat(coords[2]);
    }

    public String toDatabaseString() {
        return x + "," + y + "," + z;
    }

    public String toFormattedString() {
        return "A: " + String.format(COORD_FORMAT, x) + ", " +
                "P: " + String.format(COORD_FORMAT, y) + ", " +
                "R: " + String.format(COORD_FORMAT, z);
    }

    public float[] getCoords() {
        return new float[]{x, y, z};
    }

    public boolean isNull() {
        return false;
    }

    public boolean isEqual(IOrientation orientation) {
        return isNull() == orientation.isNull() && !arrayValuesDiffer(getCoords(), getCoords(), EQUAL_THRESHOLD);
    }

    private boolean arrayValuesDiffer(float[] newValues, float[] oldValues, float amountForChange) {
        if (newValues.length != oldValues.length)
            throw new IllegalArgumentException("Comparison arrays have different lengths");

        for (int copyIndex = 0; copyIndex < newValues.length; copyIndex++) {
            if (Math.abs(newValues[copyIndex] - oldValues[copyIndex]) > amountForChange) {
                return true;
            }
        }
        return false;
    }
}