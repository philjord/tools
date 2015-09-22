package tools.updater;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import tools.bootstrap.GeneralBootStrap;

public class SourceForgeUpdater
{
	public static String ps = System.getProperty("file.separator");

	public static String fs = System.getProperty("path.separator");

	private static String cookieString = "FreedomCookie=true;path=/;"; // taken from response

	private static HttpURLConnection conn;

	private static boolean hasCancelled = false;

	/**
	 * return true if the boot should continue false if an update needs to happen
	 * @param downloadLocation 
	 * @param currentVersion 
	 * @return
	 * @throws Exception
	 */
	public static boolean doUpdate(String currentVersionFileName, String listFilesURL)
	{
		JFrame f = new JFrame("Checking for updates to " + currentVersionFileName);
		f.setSize(200, 10);
		f.setResizable(false);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setBorder(new TitledBorder("Checking for updates"));
		f.getContentPane().add(progressBar, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		f.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		f.pack();
		f.setVisible(true);

		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				hasCancelled = true;
				conn.disconnect();
			}
		});

		// let's see if the last update left an update jar for us to swap out...
		updateUpdater();

		String htmlStr = null;
		// let's see whats at teh end of the given url shall we
		try
		{
			URL url = new URL(listFilesURL);

			conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(true);
			conn.setReadTimeout(5000);
			conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
			conn.addRequestProperty("User-Agent", "Mozilla");
			conn.addRequestProperty("Referer", "sourceforge.net");
			conn.setRequestProperty("Cookie", cookieString);

			System.out.println("Request URL ... " + url);

			boolean redirect = false;

			// normally, 3xx is redirect
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK)
			{
				if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}

			System.out.println("Response Code ... " + status);

			if (redirect)
			{
				// get redirect url from "location" header field
				String newUrl = conn.getHeaderField("Location");

				// get the cookie if need, for login
				//String cookies = conn.getHeaderField("Set-Cookie");

				// open the new connnection again
				conn = (HttpURLConnection) new URL(newUrl).openConnection();
				//conn.setRequestProperty("Cookie", cookies);
				conn.setRequestProperty("Cookie", cookieString);
				conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				conn.addRequestProperty("User-Agent", "Mozilla");
				conn.addRequestProperty("Referer", "google.com");

				System.out.println("Redirect to URL : " + newUrl);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer html = new StringBuffer();

			while ((inputLine = in.readLine()) != null)
			{
				html.append(inputLine);
			}
			in.close();
			htmlStr = html.toString();
		}
		catch (MalformedURLException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			if (!hasCancelled)
				e1.printStackTrace();
		}

		// null if we cancelled or had trouble
		if (htmlStr != null)
		{
			int directLinkIdx = htmlStr.indexOf("id=\"problems\"");
			if (directLinkIdx != -1)
			{
				String startMaker = "href=\"";
				int hrefIdx = htmlStr.indexOf(startMaker, directLinkIdx);
				if (hrefIdx != -1)
				{
					String endMarker = "\" class=\"direct-download\"";
					int hrefEndIdx = htmlStr.indexOf(endMarker, hrefIdx);
					if (hrefEndIdx != -1)
					{
						String downloadUrl = htmlStr.substring(hrefIdx + startMaker.length(), hrefEndIdx);
						System.out.println("downloadUrl " + downloadUrl);
						int fileNameStartIdx = downloadUrl.lastIndexOf("/");
						if (fileNameStartIdx != -1)
						{
							int fileNameEndIdx = downloadUrl.indexOf("?r=", fileNameStartIdx);
							if (fileNameEndIdx != -1)
							{
								try
								{
									String downloadFileName = URLDecoder.decode(
											downloadUrl.substring(fileNameStartIdx + "/".length(), fileNameEndIdx), "UTF-8");
									System.out.println("downloadFileName " + downloadFileName);

									if (!currentVersionFileName.equals(downloadFileName))
									{
										System.out.println("currentVersionFileName:" + currentVersionFileName + " != downloadFileName");

										int result = JOptionPane.showConfirmDialog(f, "Update Availible: " + downloadFileName
												+ "\nDo you want to update now?", "Update Availible", JOptionPane.YES_NO_OPTION);
										if (result == JOptionPane.OK_OPTION)
										{
											f.setTitle("Updating to " + downloadFileName);
											f.getContentPane().removeAll();// don't need the chcker progress
											f.getContentPane().invalidate();
											f.getContentPane().validate();
											f.getContentPane().doLayout();
											f.getContentPane().repaint();
											System.out.println("Time to download...");
											String recallJar = new File(GeneralBootStrap.class.getProtectionDomain().getCodeSource()
													.getLocation().toURI().getPath()).getAbsolutePath();// this is the main jar (to call back)
											String rootDirectory = new File(recallJar).getParentFile().getAbsolutePath();// this is where the main jar lives (it has a lib below it)
											String unzipPath = new File(rootDirectory).getParentFile().getAbsolutePath();// the zip file has the root folder in it
											String updateFolder = rootDirectory + ps + "update";// download to here

											HttpDownloadUtility httpDownloadUtility = new HttpDownloadUtility();
											if (httpDownloadUtility.downloadFile(f, downloadUrl, downloadFileName, updateFolder))
											{
												System.out.println("Time to restart...");
												callUpdater(updateFolder + ps + downloadFileName, unzipPath, rootDirectory, recallJar);

												f.setVisible(false);
												f.dispose();
												// tell caller to get out of town!
												return false;
											}
											else
											{
												System.out.println("HTTP download failed");
											}
										}
									}
									else
									{
										System.out.println("currentVersionFileName:" + currentVersionFileName + " = downloadFileName");
										System.out.println("No update");
									}
								}
								catch (URISyntaxException e1)
								{
									e1.printStackTrace();
								}
								catch (UnsupportedEncodingException e1)
								{
									e1.printStackTrace();
								}
								catch (IOException e1)
								{
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		f.setVisible(false);
		f.dispose();
		// all is well (enough) continue booting
		return true;
	}

	private static void updateUpdater()
	{
		String jarpath = "." + ps + "lib" + ps + "update.jar";
		File updateJarUpdateFile = new File(jarpath + ".update");
		// shall we swap out?
		System.out.println("updateJarUpdateFile " + updateJarUpdateFile);
		if (updateJarUpdateFile.exists())
		{
			System.out.println("exists");
			File updateJar = new File(jarpath);
			updateJar.delete();
			updateJarUpdateFile.renameTo(new File(jarpath));
			System.out.println("renamed");
		}
	}

	private static void callUpdater(String updateZip, String unzipPath, String rootDirectory, String recallJar) throws IOException
	{

		String javaExe = "java";// just call the path version by default

		//find out if a JRE folder exists, and use it if possible
		File possibleJreFolder = new File(rootDirectory + ps + "jre");
		if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
		{
			javaExe = rootDirectory + ps + "jre" + ps + "bin" + ps + "java";
		}
		String jarpath = rootDirectory + ps + "lib" + ps + "update.jar" + fs;
		ProcessBuilder pb = new ProcessBuilder(javaExe, "-cp", jarpath, "tools.updater.Update", updateZip, unzipPath, rootDirectory,
				recallJar);
		pb.start();

		System.out.println("Called " + javaExe);
		System.out.println("jarpath " + jarpath);
		System.out.println("tools.updater.Update ");
		System.out.println("updateZip " + updateZip);
		System.out.println("unzipPath " + unzipPath);
		System.out.println("rootDirectory " + rootDirectory);
		System.out.println("recallJar " + recallJar);
	}
}
