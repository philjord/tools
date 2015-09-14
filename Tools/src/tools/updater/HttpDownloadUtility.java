package tools.updater;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import tools.io.FileDownloadProgressThread;
import tools.io.FileDownloadProgressThread.CancelCallBack;

/**
 * A utility that downloads a file from a URL.
 * @author www.codejava.net
 *
 */
public class HttpDownloadUtility
{
	private static final int BUFFER_SIZE = 4096;

	private HttpURLConnection httpConn;

	private boolean hasCancelled = false;

	public HttpDownloadUtility()
	{

	}

	/**
	 * Downloads a file from a URL, blocking call
	 * @param fileURL HTTP URL of the file to be downloaded
	 * @param saveDir path of the directory to save the file
	 * @returns true if successful
	 * @throws IOException
	 */
	public boolean downloadFile(Component parent, String fileURL, String fileName, String saveDir)
	{
		FileOutputStream outputStream = null;
		FileDownloadProgressThread progT = null;
		try
		{
			URL url = new URL(fileURL);
			httpConn = (HttpURLConnection) url.openConnection();
			int responseCode = httpConn.getResponseCode();

			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK)
			{
				String disposition = httpConn.getHeaderField("Content-Disposition");
				String contentType = httpConn.getContentType();
				int contentLength = httpConn.getContentLength();

				if (disposition != null)
				{
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0)
					{
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				}
				else
				{
					// use file name provided
				}

				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " + disposition);
				System.out.println("Content-Length = " + contentLength);
				System.out.println("fileName = " + fileName);

				// opens input stream from the HTTP connection
				InputStream inputStream = httpConn.getInputStream();

				//ensure out is good to go
				File dir = new File(saveDir);
				if (!dir.exists())
					dir.mkdirs();

				String saveFilePath = saveDir + File.separator + fileName;

				// opens an output stream to save into file
				outputStream = new FileOutputStream(saveFilePath);

				progT = new FileDownloadProgressThread(parent, saveFilePath, contentLength, new File(saveFilePath));
				progT.setCancelCallBack(new CancelCallBack()
				{
					@Override
					public void cancel()
					{
						hasCancelled = true;
						httpConn.disconnect();
						System.out.println("transfer cancelled");
					}

				});
				progT.start();

				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1)
				{
					outputStream.write(buffer, 0, bytesRead);
				}

				progT.stopNow();
				outputStream.close();
				inputStream.close();

				System.out.println("File downloaded");
				return true;
			}
			else
			{
				System.out.println("No file to download. Server replied HTTP code: " + responseCode);
			}
		}
		catch (IOException e)
		{
			//we'll get this if cancel is called just ignore and leave
			if (!hasCancelled)
				e.printStackTrace();
		}
		finally
		{
			if (progT != null)
				progT.stopNow();
			if (outputStream != null)
			{
				try
				{
					outputStream.close();
				}
				catch (IOException e)
				{//ignore
				}
			}
			if (httpConn != null)
				httpConn.disconnect();
		}

		return false;
	}
}