package tools.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Convinent way to convert from RandomAccessFile to MappedByteBuffer 
 * @author phil
 *
 */
public class MappedByteBufferRAF extends RandomAccessFile
{
	private MappedByteBuffer mappedByteBuffer;

	public MappedByteBufferRAF(File file, String mode) throws IOException
	{
		super(file, mode);
		//mode can only be r rw rws rwd
		FileChannel.MapMode mm = FileChannel.MapMode.READ_ONLY;
		if (!mode.equals("r"))
		{
			mm = FileChannel.MapMode.READ_WRITE;
		}

		FileChannel ch = getChannel();
		mappedByteBuffer = ch.map(mm, 0, ch.size());
	}

	@Override
	public long getFilePointer() throws IOException
	{
		return mappedByteBuffer.position();
	}

	@Override
	public void seek(long pos) throws IOException
	{
		mappedByteBuffer.position((int) pos);
	}

	@Override
	public int read() throws IOException
	{
	//	try
		{
			return mappedByteBuffer.get();
		}
	//	catch (BufferUnderflowException e)
	//	{
			// indicate end of file
	//		return -1;
	//	}
	}

	private int readBytes(byte b[], int off, int len)
	{
		try
		{
			mappedByteBuffer.get(b, off, len);
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
	public int skipBytes(int n)
	{
		mappedByteBuffer.position(mappedByteBuffer.position() + n);
		return n;
	}

	@Override
	public void write(int b) throws IOException
	{
		mappedByteBuffer.put((byte) b);
	}

	private void writeBytes(byte b[], int off, int len)
	{
		mappedByteBuffer.put(b, off, len);
	}

	@Override
	public void write(byte b[]) throws IOException
	{
		writeBytes(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException
	{
		writeBytes(b, off, len);
	}

	@Override
	public long length() throws IOException
	{
		return super.length();
	}

	@Override
	public void setLength(long newLength) throws IOException
	{
		super.setLength(newLength);
	}

	/**
	 * For regular old operations
	 * @return
	 */
	public MappedByteBuffer getMappedByteBuffer()
	{
		return mappedByteBuffer;
	}
}
