package tools.io;

import java.io.File;

import javax.swing.ProgressMonitor;

public class FileDownloadProgressThread extends Thread
{
	private File destination;

	private long fileStartSize;

	private ProgressMonitor progressMonitor;

	private boolean stop = false;

	public FileDownloadProgressThread(String fileName, long fileStartSize, File destination)
	{
		this.setDaemon(true);
		this.setName("Progress thread of download of " + fileName);
		this.destination = destination;
		this.fileStartSize = fileStartSize;
		progressMonitor = new ProgressMonitor(null, "Download progress of " + fileName, "", 0, (int) fileStartSize);
	}

	public void run()
	{
		while (!stop)
		{
			long destLen = destination.length();
			progressMonitor.setProgress((int) destLen);
			
			if (destLen >= fileStartSize)
				stop = true;
			
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		progressMonitor.close();
	}

	public void stopNow()
	{
		stop = true;
	}
}
