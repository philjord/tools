package tools.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import tools.bootstrap.ProcessExitDetector.ProcessListener;
import tools.io.StreamPump;

public class GeneralBootStrap
{
	public static String ps = System.getProperty("file.separator");

	public static String fs = System.getProperty("path.separator");

	public static String cacheAutoComputeBounds = "-Dj3d.cacheAutoComputeBounds=true";

	public static String noddraw = "-Dsun.java2d.noddraw=true";

	public static String sharedctx = "-Dj3d.sharedctx=true";

	public static String clearStencilBuffer = "-Dj3d.stencilClear=true";

	public static String java3dSound = "-Dj3d.audiodevice=com.sun.j3d.audioengines.javasound.JavaSoundMixer";

	public static String fancyGCa = "-XX:+UnlockExperimentalVMOptions";

	public static String fancyGCb = "-XX:+UseG1GC";

	public static String implicitAntialiasing = "-Dj3d.implicitAntialiasing=true";

	public static String noOptimizeForSpace = "-Dj3d.optimizeForSpace=false";

	public static String allowSoleUser = "-Dj3d.allowSoleUser=true";

	public static String disableExtJars = "-Djava.ext.dirs=." + ps + "none" + ps;

	//TODO: add the -server arg when the jre is my own deployed one as a server jvm

	protected static String createJavaExeStr()
	{
		String javaExe = "java";// just call the path version by default

		//find out if a JRE folder exists, and use it if possible
		File possibleJreFolder = new File("." + ps + "jre");
		if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
		{
			javaExe = "." + ps + "jre" + ps + "bin" + ps + "java";
		}

		return javaExe;
	}

	protected static void startProcess(ProcessBuilder pb, String logFilename, String errLogFilename, final String extraInfo)
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

			StreamPump streamPump = new StreamPump(p.getInputStream(), new FileOutputStream(log));
			streamPump.start();

			final StreamPump streamPumpErr = new StreamPump(p.getErrorStream(), new FileOutputStream(logErr));
			streamPumpErr.start();

			ProcessExitDetector processExitDetector = new ProcessExitDetector(p);
			processExitDetector.addProcessListener(new ProcessListener() {
				public void processFinished(Process process)
				{
					// a couple of fixed outputs I can't avoid (java3d, openal, prefs, my version stamp)
					if (streamPumpErr.getBytesPumped() > 800)
					{
						int result = JOptionPane.showConfirmDialog(null, "Some issues occurred during session, send err log to author?",
								"Error output upload", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.OK_OPTION)
						{
							//First we need to desperately load the jftp.jar in lib, before constructing the ErrorLogUploader
							try
							{
								File thisJar = new File(
										GeneralBootStrap.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
								ClassPathHack.addFile(new File(thisJar.getParent(), ps + "lib" + ps + "jftp.jar"));
								System.out.println("jftp jar added to classpath hackishly");

								//must close teh handle to the error file, so uploader can add to it
								streamPumpErr.stopNow();
								ErrorLogUploader errorLogUploader = new ErrorLogUploader(logErr, extraInfo);
								errorLogUploader.doUpload();// will block

							}
							catch (URISyntaxException e)
							{
								e.printStackTrace();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
					System.out.println("A happy finish");
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
			new Thread(new Runnable() {

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
		long availableMemory = 1000000000L;// assume low
		try
		{
			com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
					.getOperatingSystemMXBean();
			availableMemory = mxbean.getTotalPhysicalMemorySize();
		}
		catch (Exception e)
		{// fine skip it
		}
		if (os.indexOf("64") != -1 && availableMemory > 4000000000L)
		{
			return "-Xmx2400m";
		}
		else
		{
			return "-Xmx1200m";
		}

	}

}
