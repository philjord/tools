package tools.io;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Copy of the RandomAccessFile interface to make things consistent, needs to be dropped
 */
public interface IRandomAccessFile {

	public long getFilePointer() throws IOException;

	public void seek(long pos) throws IOException;

	public int read() throws IOException;

	public byte readByte() throws IOException;

	public int read(byte b[], int off, int len) throws IOException;

	public int read(byte b[]) throws IOException;

	public int skipBytes(int n) throws IOException;

	public void write(int b) throws IOException;

	public void write(byte b[]) throws IOException;

	public void write(byte b[], int off, int len) throws IOException;

	public long length() throws IOException;

	public void setLength(long newLength) throws IOException;

	public void close() throws IOException;

	public FileChannel getChannel();
}
