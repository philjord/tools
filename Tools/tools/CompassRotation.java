package tools;

/**
 * @author Administrator
 *
 */
public class CompassRotation
{
	public static final double ZERO = 0;

	public static final double RADIANS = Math.PI * 2;

	public static final double DEGREES = 360;

	public static final double radToDegMultiple = DEGREES / RADIANS;

	public static final double degToRadMultiple = RADIANS / DEGREES;

	public static double difference(double start, double end, double max)
	{
		double difference = end - start;
		difference -= difference > max / 2 ? max : 0;
		return difference;
	}

	public static double wrapToMax(double value, double max)
	{
		value = value % max;
		value += value < 0 ? max : 0;
		return value;
	}

	public static double degToRad(double degs)
	{
		return degs * degToRadMultiple;
	}

	public static double radToDeg(double rads)
	{
		return rads * radToDegMultiple;
	}

}
