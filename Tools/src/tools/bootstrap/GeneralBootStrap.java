package tools.bootstrap;

import java.io.File;
import java.io.IOException;

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
		final File logErr = new File(errLogFilename);
		try
		{
			if (!log.exists())
				log.getParentFile().mkdirs();
			if (!logErr.exists())
				logErr.getParentFile().mkdirs();

			Process p = pb.start();

			StreamPump streamPump = new StreamPump(p.getInputStream(), log);
			streamPump.start();

			final StreamPump streamPumpErr = new StreamPump(p.getErrorStream(), logErr);
			streamPumpErr.start();

			ProcessExitDetector processExitDetector = new ProcessExitDetector(p);
			processExitDetector.addProcessListener(new ProcessListener()
			{
				public void processFinished(Process process)
				{
					if (streamPumpErr.getBytesPumped() > 0)
					{
						int result = JOptionPane.showConfirmDialog(null, "Maybe error detected, wanna send it to the cloud?",
								"Error output upload", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.OK_OPTION)
						{
							ErrorLogUploader errorLogUploader = new ErrorLogUploader(logErr);
							errorLogUploader.doUpload();// will block
						}
					}

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

}
