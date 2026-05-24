package tools.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * TO BE REMOVED, only FileChannel should be passed about
 * @author phil
 */
public class FileChannelRAF {
	private FileChannel fileChannel;

	/**
	 * TO BE REMOVED
	 * 
	 * @param file
	 * @param mode
	 * @throws IOException
	 */
	public FileChannelRAF(File file, String mode) throws IOException {
		// this will be read only file channel, outputstream for write
		this.fileChannel = new FileInputStream(file).getChannel();
	}
	
	public FileChannelRAF(FileChannel fileChannel) throws IOException {
		this.fileChannel = fileChannel;
	}

	public void close() throws IOException {
		fileChannel.force(true);
		fileChannel.close();
	}

	public FileChannel getChannel() {
		return fileChannel;
	}
}

