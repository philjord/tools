package tools.io;



public class ESMByteConvert
{
	public static float extractFloat(byte[] bytes, int start)
	{
		return Float.intBitsToFloat(((bytes[start + 3] & 0xff) << 24) | ((bytes[start + 2] & 0xff) << 16)
				| ((bytes[start + 1] & 0xff) << 8) | (bytes[start + 0] & 0xff));
	}

	public static byte extractByte(byte[] bytes, int start)
	{
		return bytes[start];
	}

	public static short extractUnsignedByte(byte[] bytes, int start)  
	{
		return byteToUnsigned(bytes[start]);
	}

	public static short byteToUnsigned(byte in)
	{
		return (short) (in & 0xFF);
	}

	public static int extractInt(byte[] bytes, int start)
	{
		return ((bytes[start + 3] & 0xff) << 24) | ((bytes[start + 2] & 0xff) << 16) | ((bytes[start + 1] & 0xff) << 8)
				| (bytes[start + 0] & 0xff);
	}

	public static int extractInt64(byte[] bytes, int start)
	{
		return ((bytes[start + 7] & 0xff) << 56) | ((bytes[start + 6] & 0xff) << 48) | ((bytes[start + 5] & 0xff) << 40)
				| ((bytes[start + 4] & 0xff) << 32) | ((bytes[start + 3] & 0xff) << 24) | ((bytes[start + 2] & 0xff) << 16)
				| ((bytes[start + 1] & 0xff) << 8) | (bytes[start + 0] & 0xff);
	}

	//NOTE returns an int for now as it is unsigned short
	public static int extractShort(byte[] bytes, int start)
	{
		return ((bytes[start + 1] & 0xff) << 8) | (bytes[start + 0] & 0xff);
	}
	
	 

	public static void insertFloat(byte[] bytes, float f, int start)
	{
		/*return Float.intBitsToFloat(
		 ((bytes[start + 3] & 0xff) << 24)
		 | ((bytes[start + 2] & 0xff) << 16)
		 | ((bytes[start + 1] & 0xff) << 8)
		 | (bytes[start + 0] & 0xff));*/
		int bits = Float.floatToRawIntBits(f);
		bytes[start + 3] = (byte) (bits >> 24);
		bytes[start + 2] = (byte) (bits >> 16);
		bytes[start + 1] = (byte) (bits >> 8);
		bytes[start + 0] = (byte) (bits >> 0);
	}
}
