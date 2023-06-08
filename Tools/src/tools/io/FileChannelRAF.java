package tools.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Convenient way to convert express a RandomAccessFile interface from a FileChannel
 * @author phil
 */
public class FileChannelRAF implements IRandomAccessFile {
	private FileChannel fileChannel;
	//private MappedByteBuffer mappedByteBuffer;

	/**
	 * getChannel() − method on any either FileInputStream, FileOutputStream or RandomAccessFile.
	 * 
	 * @param file
	 * @param mode
	 * @throws IOException
	 */
	public FileChannelRAF(File file, String mode) throws IOException {

		this.fileChannel = new FileInputStream(file).getChannel();

		//ArchiveFile has more details on these, possibly not useful now FileChannels are in play
		/*if (file.length() <= Integer.MAX_VALUE && false) {
			FileChannel.MapMode mm = FileChannel.MapMode.READ_ONLY;
			if (!mode.equals("r")) {
				mm = FileChannel.MapMode.READ_WRITE;
			}
		
			mappedByteBuffer = fileChannel.map(mm, 0, fileChannel.size());
			//TODO: then call this instead of filechannel
		}*/
	}

	public FileChannelRAF(FileChannel fileChannel) throws IOException {
		this.fileChannel = fileChannel;
	}
	public FileChannelRAF(FileChannel fileChannel, String mode) throws IOException {
		this.fileChannel = fileChannel;
	}

	@Override
	public long getFilePointer() throws IOException {
		return fileChannel.position();
	}

	@Override
	public void seek(long pos) throws IOException {
		fileChannel.position((int)pos);
	}

	/**
	 * REad a single byte and ret
	 */
	@Override
	public int read() throws IOException {
		try {
			ByteBuffer bb = ByteBuffer.wrap(new byte[1]);
			fileChannel.read(bb);
			return bb.getInt();
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}
	}

	@Override
	public byte readByte() throws IOException {
		try {
			byte[] b = new byte[1];
			ByteBuffer bb = ByteBuffer.wrap(b);
			fileChannel.read(bb);
			return b [0];
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}
	}

	private int readBytes(byte b[], int off, int len) throws IOException {
		try {
			ByteBuffer bb = ByteBuffer.wrap(b, off, len);
			len = fileChannel.read(bb);
			return len;
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		return readBytes(b, off, len);
	}

	@Override
	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		fileChannel.position(fileChannel.position() + n);
		return n;
	}

	@Override
	public void write(int b) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(new byte[] {(byte)b});
		fileChannel.write(bb);
	}

	private void writeBytes(byte b[], int off, int len) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(b, off, len);
		fileChannel.write(bb);
	}

	@Override
	public void write(byte b[]) throws IOException {
		writeBytes(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		writeBytes(b, off, len);
	}

	@Override
	public long length() throws IOException {
		return fileChannel.size();
	}

	@Override
	public void setLength(long newLength) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		fileChannel.close();
	}

	@Override
	public FileChannel getChannel() {
		return fileChannel;
	}
}

/*	
public class FileChannelRAF extends RandomAccessFile {	
	private FileChannel fileChannel;

	public FileChannelRAF(File file, String mode) throws IOException {
		super(file, mode);
		//getChannel() − method on any either FileInputStream, FileOutputStream or RandomAccessFile.
		this.fileChannel = getChannel();
	}

	@Override
	public long getFilePointer() throws IOException {
		return fileChannel.position();
	}

	@Override
	public void seek(long pos) throws IOException {
		fileChannel.position((int)pos);
	}

	@Override
	public int read() throws IOException {
		try {
			byte[] b = new byte[1];
			ByteBuffer bb = ByteBuffer.wrap(b);
			fileChannel.read(bb);
			return b [0];
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}
	}

	private int readBytes(byte b[], int off, int len) throws IOException {
		try {
			ByteBuffer bb = ByteBuffer.wrap(b, off, len);
			len = fileChannel.read(bb);
			return len;
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		return readBytes(b, off, len);
	}

	@Override
	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		fileChannel.position(fileChannel.position() + n);
		return n;
	}

	@Override
	public void write(int b) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(new byte[] {(byte)b});
		fileChannel.write(bb);
	}

	private void writeBytes(byte b[], int off, int len) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(b, off, len);
		fileChannel.write(bb);
	}

	@Override
	public void write(byte b[]) throws IOException {
		writeBytes(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		writeBytes(b, off, len);
	}

	@Override
	public long length() throws IOException {
		return fileChannel.size();
	}

	@Override
	public void setLength(long newLength) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * For regular old operations
	 * @return
	 *
	public FileChannel getFileChannel() {
		return fileChannel;
	}
}	*/

/*
public class FileChannelRAF extends RandomAccessFile
{
	private MappedByteBuffer mappedByteBuffer;

	public FileChannelRAF(File file, String mode) throws IOException
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
		try
		{
			return mappedByteBuffer.get();
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
	 *
	public MappedByteBuffer getMappedByteBuffer()
	{
		return mappedByteBuffer;
	}


}*/
