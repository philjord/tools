package tools.swing;

import java.awt.Component;
import java.io.File;

import javax.swing.ProgressMonitor;

public class FileDownloadProgressThread extends Thread
{
	private File destination;

	private long fileStartSize;

	private ProgressMonitor progressMonitor;

	private boolean stop = false;

	private CancelCallBack cancelCallBack = null;

	public FileDownloadProgressThread(Component parent, String fileName, long fileStartSize, File destination)
	{
		this.setDaemon(true);
		this.setName("Progress thread of download of " + fileName);
		this.destination = destination;
		this.fileStartSize = fileStartSize;
		progressMonitor = new ProgressMonitor(parent, "Download progress of " + fileName, "", 0, (int) fileStartSize);
	}

	public void setCancelCallBack(CancelCallBack cancelCallBack)
	{
		this.cancelCallBack = cancelCallBack;
	}

	public void run()
	{
		while (!stop)
		{
			long destLen = destination.length();
			progressMonitor.setProgress((int) destLen);			

			if (destLen == fileStartSize)
				stop = true;

			if (cancelCallBack != null && progressMonitor.isCanceled())
			{
				cancelCallBack.cancel();
				stop = true;
			}

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

	public interface CancelCallBack
	{
		public void cancel();
	}
}
