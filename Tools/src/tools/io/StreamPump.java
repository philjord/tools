package tools.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamPump extends Thread
{
	/*
	 * Note IOExceptions, closure of output stream will kill this thread
	 */

	private InputStream is;

	private OutputStream os;

	private long bytesPumped = 0;

	private boolean stop = false;

	public StreamPump(InputStream is, FileOutputStream out) throws FileNotFoundException
	{
		this.is = is;
		this.os = out;
	}

	public long getBytesPumped()
	{
		return bytesPumped;
	}

	public void stopNow()
	{
		stop = true;
		this.interrupt();
	}

	@Override
	public void run()
	{

		// see http://stackoverflow.com/questions/24312147/bufferedwriter-printwriter-outputstreamwriter-dont-flush-buffer-until-close
		BufferedOutputStream bos = null;
		try
		{
			BufferedInputStream bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(os);
			byte[] buffer = new byte[32]; // Adjust if you want
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1 && !stop)
			{
				bos.write(buffer, 0, bytesRead);
				bos.flush();
				bytesPumped += bytesRead;
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				if (bos != null)
					bos.close();
			}
			catch (IOException ioe2)
			{
				//ignore
			}
		}
	}

}
