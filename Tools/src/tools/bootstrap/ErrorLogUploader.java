package tools.bootstrap;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPMessageCollector;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 *  This class grabs game media from my ftp server, it asks for the password though...
 *
 */
public class ErrorLogUploader
{
	// names alter to avoid github bots pulling this
	private static final String A = "philjord.ddns.net";

	private static final String B = "errorlogwriter";

	private static String C = "q#dk8jh3:k$jY*(F$"; //I should put this away betterer

	private FTPClient ftp = null;

	private FTPMessageCollector ftpMessageCollector = new FTPMessageCollector();

	private JFrame frame = new JFrame("Uploading error file");

	private File logErr;

	public ErrorLogUploader(File logErr)
	{
		this.logErr = logErr;
		frame.setSize(200, 80);
		frame.setResizable(false);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		frame.getContentPane().add(progressBar, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		frame.setVisible(true);

		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ftp.cancelTransfer();
			}
		});
	}

	/**
	 * VERY blocking
	 */
	public void doUpload()
	{
		try
		{
			ftp = new FTPClient(A);
			ftp.setMessageListener(ftpMessageCollector);
			ftp.login(B, C);

			// if no exception we are good to go			

			ftp.setConnectMode(FTPConnectMode.PASV);
			ftp.setType(FTPTransferType.BINARY);
			// no need to set dir as this user only has root

			// put log file plus a unique id  
			ftp.put(logErr.getAbsolutePath(), logErr.getName() + System.currentTimeMillis() + ".txt");
			ftp.quit();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (FTPException e)
		{
			e.printStackTrace();
		}
		// just ignore all failures and quietly leave the room

	}
}
