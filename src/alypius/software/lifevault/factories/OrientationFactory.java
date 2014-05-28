package alypius.software.lifevault.factories;

import alypius.software.lifevault.interfaces.IOrientation;
import alypius.software.lifevault.models.*;

public class OrientationFactory {

    public static IOrientation create(String orientationString) {
        return (orientationString == null || orientationString.equals(""))
                ? new NullOrientation()
                : new OrientationEvent(orientationString);
    }

    public static IOrientation create(float[] coords) {
        return (coords == null || coords.length == 0)
                ? new NullOrientation()
                : new OrientationEvent(coords);
    }
}