package tools.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Convenient way to convert express a RandomAccessFile interface from a FileChannel
 * @author phil
 */
public class FileChannelRAF {
	private FileChannel fileChannel;

	/**
	 * getChannel()   method on any either FileInputStream, FileOutputStream or RandomAccessFile.
	 * 
	 * @param file
	 * @param mode
	 * @throws IOException
	 */
	public FileChannelRAF(File file, String mode) throws IOException {
		// this will be read only file channel, outputstream for write
		this.fileChannel = new FileInputStream(file).getChannel();
	}
	
	public FileChannelRAF(RandomAccessFile file, String mode) throws IOException {		
		this.fileChannel = file.getChannel();
	}

	public FileChannelRAF(FileChannel fileChannel) throws IOException {
		this.fileChannel = fileChannel;
	}
	public FileChannelRAF(FileChannel fileChannel, String mode) throws IOException {
		this.fileChannel = fileChannel;
	}

	public long getFilePointer() throws IOException {
		throw new UnsupportedOperationException();
		//return fileChannel.position();
	}

	public void seek(long pos) throws IOException {
		throw new UnsupportedOperationException();
		//fileChannel.position(pos);
	}

	/**
	 * Read a single byte and ret
	 */
	public int read() throws IOException {
		throw new UnsupportedOperationException();
		/*try {
			ByteBuffer bb = ByteBuffer.wrap(new byte[1]);
			fileChannel.read(bb);
			return bb.getInt();
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}*/
	}

	public byte readByte() throws IOException {
		throw new UnsupportedOperationException();
		/*try {
			byte[] b = new byte[1];
			ByteBuffer bb = ByteBuffer.wrap(b);
			fileChannel.read(bb);
			return b [0];
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}*/
	}

/*	private int readBytes(byte b[], int off, int len) throws IOException {
		try {
			ByteBuffer bb = ByteBuffer.wrap(b, off, len);
			len = fileChannel.read(bb);
			return len;
		} catch (BufferUnderflowException e) {
			// indicate end of file
			return -1;
		}
	}*/

	public int read(byte b[], int off, int len) throws IOException {
		throw new UnsupportedOperationException();
		//return readBytes(b, off, len);
	}

	public int read(byte b[]) throws IOException {
		throw new UnsupportedOperationException();
		//return readBytes(b, 0, b.length);
	}

	public int skipBytes(int n) throws IOException {
		throw new UnsupportedOperationException();
		//fileChannel.position(fileChannel.position() + n);
		//return n;
	}

	public void write(int b) throws IOException {
		System.out.println("FileChannelRAF position write use1");
		ByteBuffer bb = ByteBuffer.wrap(new byte[] {(byte)b});
		fileChannel.write(bb);
	}

	private void writeBytes(byte b[], int off, int len) throws IOException {
		System.out.println("FileChannelRAF position write use1");
		ByteBuffer bb = ByteBuffer.wrap(b, off, len);
		fileChannel.write(bb);
	}

	public void write(byte b[]) throws IOException {
		System.out.println("FileChannelRAF position write use1");
		writeBytes(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		System.out.println("FileChannelRAF position write use1");
		writeBytes(b, off, len);
	}

	public long length() throws IOException {
		return fileChannel.size();
	}

	public void setLength(long newLength) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void close() throws IOException {
		fileChannel.force(true);
		fileChannel.close();
	}

	public FileChannel getChannel() {
		return fileChannel;
	}
}

