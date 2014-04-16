package tools.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class StreamPump extends Thread
{
	/*
	 * Note IOExceptions, closure of output stream will kill this thread
	 */

	InputStream is;

	OutputStream os;

	public StreamPump(InputStream is, File out) throws FileNotFoundException
	{
		this.is = is;
		this.os = new FileOutputStream(out);
	}

	@Override
	public void run()
	{
		BufferedWriter bw = null;
		try
		{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			OutputStreamWriter osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			String nl = System.getProperty("line.separator");
			String line = null;
			while ((line = br.readLine()) != null)
			{
				bw.write(line + nl);
				bw.flush();
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
				if (bw != null)
					bw.close();
			}
			catch (IOException ioe2)
			{
				//ignore
			}
		}
	}

}
