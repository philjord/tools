package tools.io;

//TODO: bring this and other little endian together, then wrap with streams
public class PrimitiveBytes
{
	public static float extractFloat(byte[] bytes, int start)
	{
		// LittleEndianPrimitiveBytes has lookup table too
		return Float.intBitsToFloat(((bytes[start + 3] & 0xff) << 24) | ((bytes[start + 2] & 0xff) << 16) | ((bytes[start + 1] & 0xff) << 8)
				| (bytes[start + 0] & 0xff));
	}

	public static boolean extractBoolean(byte[] bytes, int start)
	{
		return bytes[start] == 0;
	}

	public static byte extractByte(byte[] bytes, int start)
	{
		return bytes[start];
	}

	public static int extractInt(byte[] bytes, int start)
	{
		return ((bytes[start + 3] & 0xff) << 24) | ((bytes[start + 2] & 0xff) << 16) | ((bytes[start + 1] & 0xff) << 8) | (bytes[start + 0] & 0xff);
	}

	public static int extractLong(byte[] bytes, int start)
	{
		return ((bytes[start + 7] & 0xff) << 56) | ((bytes[start + 6] & 0xff) << 48) | ((bytes[start + 5] & 0xff) << 40)
				| ((bytes[start + 4] & 0xff) << 32) | ((bytes[start + 3] & 0xff) << 24) | ((bytes[start + 2] & 0xff) << 16)
				| ((bytes[start + 1] & 0xff) << 8) | (bytes[start + 0] & 0xff);
	}

	// NOTE returns an int for now as it is unsigned short
	public static int extractShort(byte[] bytes, int start)
	{
		return ((bytes[start + 1] & 0xff) << 8) | (bytes[start + 0] & 0xff);
	}

	public static void insertFloat(byte[] bytes, float f, int start)
	{
		int bits = Float.floatToRawIntBits(f);
		bytes[start + 3] = (byte) (bits >> 24);
		bytes[start + 2] = (byte) (bits >> 16);
		bytes[start + 1] = (byte) (bits >> 8);
		bytes[start + 0] = (byte) (bits >> 0);
	}

	public static void insertInt(byte[] bytes, int i, int start)
	{
		int bits = i;
		bytes[start + 3] = (byte) (bits >> 24);
		bytes[start + 2] = (byte) (bits >> 16);
		bytes[start + 1] = (byte) (bits >> 8);
		bytes[start + 0] = (byte) (bits >> 0);
	}

	public static void insertLong(byte[] bytes, long i, int start)
	{
		long bits = i;
		bytes[start + 7] = (byte) (bits >> 56);
		bytes[start + 6] = (byte) (bits >> 48);
		bytes[start + 5] = (byte) (bits >> 40);
		bytes[start + 4] = (byte) (bits >> 32);
		bytes[start + 3] = (byte) (bits >> 24);
		bytes[start + 2] = (byte) (bits >> 16);
		bytes[start + 1] = (byte) (bits >> 8);
		bytes[start + 0] = (byte) (bits >> 0);
	}

	public static void insertBoolean(byte[] bytes, boolean b, int start)
	{
		bytes[start] = b ? (byte) 1 : (byte) 0;
	}

	public static float extractFloat(byte[] bytes)
	{
		return extractFloat(bytes, 0);
	}

	public static boolean extractBoolean(byte[] bytes)
	{
		return extractBoolean(bytes, 0);
	}

	public static byte extractByte(byte[] bytes)
	{
		return extractByte(bytes, 0);
	}

	public static int extractInt(byte[] bytes)
	{
		return extractInt(bytes, 0);
	}

	public static int extractLong(byte[] bytes)
	{
		return extractLong(bytes, 0);
	}

	// NOTE returns an int for now as it is unsigned short
	public static int extractShort(byte[] bytes)
	{
		return extractShort(bytes, 0);
	}

	public static void insertFloat(byte[] bytes, float f)
	{
		insertFloat(bytes, f, 0);
	}

	public static void insertInt(byte[] bytes, int i)
	{
		insertInt(bytes, i, 0);
	}

	public static void insertLong(byte[] bytes, long i)
	{
		insertLong(bytes, i, 0);
	}

	public static void insertBoolean(byte[] bytes, boolean b)
	{
		insertBoolean(bytes, b, 0);
	}

	public static byte[] floatBytes(float f)
	{
		byte[] bytes = new byte[4];
		int bits = Float.floatToRawIntBits(f);
		bytes[3] = (byte) (bits >> 24);
		bytes[2] = (byte) (bits >> 16);
		bytes[1] = (byte) (bits >> 8);
		bytes[0] = (byte) (bits >> 0);
		return bytes;
	}

	public static byte[] intBytes(int i)
	{
		byte[] bytes = new byte[4];
		int bits = i;
		bytes[3] = (byte) (bits >> 24);
		bytes[2] = (byte) (bits >> 16);
		bytes[1] = (byte) (bits >> 8);
		bytes[0] = (byte) (bits >> 0);
		return bytes;
	}

	public static byte[] longBytes(long i)
	{
		byte[] bytes = new byte[8];
		long bits = i;
		bytes[7] = (byte) (bits >> 56);
		bytes[6] = (byte) (bits >> 48);
		bytes[5] = (byte) (bits >> 40);
		bytes[4] = (byte) (bits >> 32);
		bytes[3] = (byte) (bits >> 24);
		bytes[2] = (byte) (bits >> 16);
		bytes[1] = (byte) (bits >> 8);
		bytes[0] = (byte) (bits >> 0);
		return bytes;
	}

	public static byte[] booleanBytes(boolean b)
	{
		//Note 0 = true
		byte[] bytes = new byte[1];
		bytes[0] = b ? (byte) 0 : (byte) 1;
		return bytes;
	}
}