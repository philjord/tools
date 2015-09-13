package tools.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import tools.bootstrap.ProcessExitDetector.ProcessListener;
import tools.io.StreamPump;

public class GeneralBootStrap
{
	public static String ps = System.getProperty("file.separator");

	public static String fs = System.getProperty("path.separator");

	public static String noddraw = "-Dsun.java2d.noddraw=true";

	public static String sharedctx = "-Dj3d.sharedctx=true";

	public static String java3dSound = "-Dj3d.audiodevice=com.sun.j3d.audioengines.javasound.JavaSoundMixer";

	public static String fancyGCa = "-XX:+UnlockExperimentalVMOptions";

	public static String fancyGCb = "-XX:+UseG1GC";

	public static String disableExtJars = "-Djava.ext.dirs=." + ps + "none" + ps;

	//TODO: add the -server arg when the jre is my own deployed one as a server jvm

	protected static String createJavaExeStr()
	{
		String javaExe = "java";// just call the path version by default

		//find out if a JRE folder exists, and use it if possible
		File possibleJreFolder = new File(".\\jre");
		if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
		{
			javaExe = ".\\jre\\bin\\java";
		}

		return javaExe;
	}

	protected static void startProcess(ProcessBuilder pb, String logFilename, String errLogFilename)
	{
		File log = new File(logFilename);
		File logErr = new File(errLogFilename);
		try
		{
			if (!log.exists())
				log.getParentFile().mkdirs();
			if (!logErr.exists())
				logErr.getParentFile().mkdirs();

			Process p = pb.start();

			StreamPump streamPump = new StreamPump(p.getInputStream(), log);
			streamPump.start();

			StreamPump streamPumpErr = new StreamPump(p.getErrorStream(), logErr);
			streamPumpErr.start();

			ProcessExitDetector processExitDetector = new ProcessExitDetector(p);
			processExitDetector.addProcessListener(new ProcessListener()
			{
				public void processFinished(Process process)
				{
					//Mac os x complains if I don't get an exit value TODO: check this works?
					System.exit(0);
				}
			});
			processExitDetector.start();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		//if oxs show log location
		String os = System.getProperty("os.name");

		if (os.indexOf("Mac") != -1)
		{
			JOptionPane opt = new JOptionPane("Logs are located:" + log.getAbsolutePath(), JOptionPane.INFORMATION_MESSAGE,
					JOptionPane.DEFAULT_OPTION, null, new Object[] {}); // no buttons
			final JDialog dlg = opt.createDialog("Log location");
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						Thread.sleep(3000);
					}
					catch (InterruptedException e)
					{
						//ignore?
					}
					dlg.dispose();

				}
			}).start();
			dlg.setVisible(true);
		}

	}

	protected static String getXMX()
	{
		String os = System.getProperty("os.arch");

		if (os.indexOf("64") != -1)
		{
			return "-Xmx2400m";
		}
		else
		{
			return "-Xmx1200m";
		}

	}

	/**
	 * return true if the boot should continue false if an update needs to happen
	 * @param downloadLocation 
	 * @param currentVersion 
	 * @return
	 * @throws Exception
	 */
	protected static boolean doUpdateFromSourceForge(String currentVersionFileName, String listFilesURL) throws Exception
	{
		// taken from response
		String cookieString = "FreedomCookie=true;path=/;";

		// let's see whats at teh end of the given url shall we
		URL url = new URL(listFilesURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

		String htmlStr = html.toString();
		System.out.println("URL Content... \n" + htmlStr);

		int directLinkIdx = htmlStr.indexOf("id=\"problems\"");
		if (directLinkIdx != -1)
		{
			String startMaker = "href=\"";
			int hrefIdx = html.indexOf(startMaker, directLinkIdx);
			if (hrefIdx != -1)
			{
				String endMarker = "\" class=\"direct-download\"";
				int hrefEndIdx = html.indexOf(endMarker, hrefIdx);
				if (hrefEndIdx != -1)
				{
					String downloadUrl = html.substring(hrefIdx + startMaker.length(), hrefEndIdx);
					System.out.println("downloadUrl " + downloadUrl);
					int fileNameStartIdx = downloadUrl.lastIndexOf("/");
					if (fileNameStartIdx != -1)
					{
						int fileNameEndIdx = downloadUrl.indexOf("?r=", fileNameStartIdx);
						if (fileNameEndIdx != -1)
						{
							String downloadFileName = URLDecoder.decode(
									downloadUrl.substring(fileNameStartIdx + "/".length(), fileNameEndIdx), "UTF-8");
							System.out.println("downloadFileName " + downloadFileName);

							if (!currentVersionFileName.equals(downloadFileName))
							{
								System.out.println("currentVersionFileName:" + currentVersionFileName + " != downloadFileName");

								int result = JOptionPane.showConfirmDialog(null, "Do you want to update now?", "Update Availible",
										JOptionPane.YES_NO_OPTION);
								if (result == JOptionPane.OK_OPTION)
								{
									System.out.println("Time to download...");
									HttpDownloadUtility.downloadFile(downloadUrl, downloadFileName, ".\\update");
									System.out.println("Time to restart...");
									callUpdater();
									// tell caller to get out of town!
									return false;
								}
							}
							else
							{
								System.out.println("currentVersionFileName:" + currentVersionFileName + " = downloadFileName");
								System.out.println("No update");
							}
						}
					}
				}
			}
		}

		// all is well continue booting
		return true;
	}

	private static void callUpdater() throws URISyntaxException, IOException
	{
		String recallJar = new File(GeneralBootStrap.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
				.getAbsolutePath();
		String rootDirectory = new File(recallJar).getParentFile().getAbsolutePath();
		String unzipPath = new File(rootDirectory).getParentFile().getAbsolutePath();
		String updateZipFile = "ElderScrollsExplorer v2.02.zip";
		String updateZip = rootDirectory + ps + "update" + ps + updateZipFile;

		String javaExe = "java";// just call the path version by default

		//find out if a JRE folder exists, and use it if possible
		File possibleJreFolder = new File(rootDirectory + "\\jre");
		if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
		{
			javaExe = rootDirectory + "\\jre\\bin\\java";
		}
		String jarpath = "." + ps + "lib" + ps + "update.jar" + fs;
		ProcessBuilder pb = new ProcessBuilder(javaExe, "-cp", jarpath, "tools.bootstrap.Update", updateZip, unzipPath, rootDirectory,
				recallJar);
		pb.start();
	}
}
