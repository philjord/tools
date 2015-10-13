package tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Convinent way to adapt from ByteBuffer to InputStream 
 * @author phil
 *
 */
public class ByteBufferIS extends InputStream
{

	private ByteBuffer byteBuffer;

	public ByteBufferIS(ByteBuffer byteBuffer)
	{
		this.byteBuffer = byteBuffer;
	}

	@Override
	public long skip(long n) throws IOException
	{
		byteBuffer.position(byteBuffer.position() + (int) n);
		return n;
	}

	@Override
	public int read() throws IOException
	{
		try
		{
			return byteBuffer.get();
		}
		catch (BufferUnderflowException e)
		{
			// indicate end of file
			return -1;
		}
	}

	private int readBytes(byte b[], int off, int len)
	{
		try
		{
			byteBuffer.get(b, off, len);
			return len;
		}
		catch (BufferUnderflowException e)
		{
			// indicate end of file
			return -1;
		}
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		return readBytes(b, off, len);
	}

	@Override
	public int read(byte b[]) throws IOException
	{
		return readBytes(b, 0, b.length);
	}

	@Override
	public int available() throws IOException
	{
		return byteBuffer.remaining();
	}

	@Override
	public void close() throws IOException
	{
		// do nothing for now
	}

	@Override
	public void mark(int readlimit)
	{
		byteBuffer.mark();
	}

	@Override
	public void reset() throws IOException
	{
		byteBuffer.reset();
	}

	@Override
	public boolean markSupported()
	{
		return true;
	}

	/**
	 * For regular old operations
	 * @return
	 */
	public ByteBuffer getByteBuffer()
	{
		return byteBuffer;
	}
}
