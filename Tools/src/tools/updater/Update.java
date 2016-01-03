package tools.updater;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;

public class Update
{
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static JProgressBar progressBar = new JProgressBar();

	private static final int BUFF_SIZE = 4096;

	public static String ps = System.getProperty("file.separator");

	public static String fs = System.getProperty("path.separator");

	// do the unzip but skip the update.jar file (of course) or ignore error
	public static void main(String[] args) throws Exception
	{
		// force a logs folder in all cases!
					File logFolder = new File("." + ps + "logs" + ps);
		try
		{			
			logFolder.mkdirs();
			System.setOut(new PrintStream(new FileOutputStream("." + ps + "logs" + ps + "updater.out.txt", true)));
			System.setErr(new PrintStream(new FileOutputStream("." + ps + "logs" + ps + "updater.err.txt", true)));
		}
		catch (Exception e)
		{
			// if no error log then desperate dialog?
			JOptionPane.showMessageDialog(null, "Problem with logs, folder = " +logFolder.getAbsolutePath()+ " : " + e);
			restart(args[2], args[3]);
		}

		try
		{
			System.out.println(dateFormat.format(new Date())); //2014/08/06 15:59:48

			String zipfile = args[0];
			String unzipPath = args[1];
			String rootDirectory = args[2];
			String restartJar = args[3];
			//TEST values
			/*String zipfile = "E:\\Java\\installers\\ElderScrollsExplorer\\update\\ElderScrollsExplorer v2.09.zip";//args[0];
			String unzipPath = "E:\\Java\\installers";//args[1];
			String rootDirectory = "E:\\Java\\installers\\ElderScrollsExplorer";//args[2];
			String restartJar = "E:\\Java\\installers\\ElderScrollsExplorer\\ElderScrollsExplorer.jar";//args[3];
			*/

			JFrame f = new JFrame("Updating " + restartJar);
			f.setSize(200, 10);
			f.setResizable(false);

			progressBar.setIndeterminate(true);
			progressBar.setBorder(new TitledBorder("Updating"));
			f.getContentPane().add(progressBar, BorderLayout.CENTER);
			f.pack();
			f.setVisible(true);

			// do the unzip but skip the update.jar file (of course) or ignore error
			ArrayList<File> skipList = new ArrayList<File>();
			skipList.add(new File(Update.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));

			File zf = new File(zipfile);
			if (zf.exists())
			{
				// wait for a moment to ensure callee has closed
				Thread.sleep(500);
				processFile(zf, unzipPath, skipList);
				Thread.sleep(200);
			}
			else
			{
				System.err.println("Update file does not exist? " + zf.getAbsolutePath());
				JOptionPane.showMessageDialog(null, "Update file des not exist? " + zf.getAbsolutePath());
			}
			restart(rootDirectory, restartJar);
		}
		catch (Exception e)
		{
			System.out.println(dateFormat.format(new Date())); //2014/08/06 15:59:48
			e.printStackTrace();
		}
	}

	private static void restart(String rootDirectory, String restartJar) throws Exception
	{
		//now restart the callee jar
		String javaExe = "java";// just call the path version by default

		//find out if a JRE folder exists, and use it if possible
		File possibleJreFolder = new File(rootDirectory + ps + "jre");
		if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
		{
			javaExe = rootDirectory + ps + "jre" + ps + "bin" + ps + "java";
		}

		ProcessBuilder pb = new ProcessBuilder(javaExe, "-jar", restartJar);
		pb.start();
		// wait just a bit longer...
		Thread.sleep(200);
		System.exit(0);

	}

	public static void processFile(File file, String destinationPath, List<File> skipList) throws IOException, ZipException
	{

		ZipInputStream is = null;
		OutputStream os = null;

		try
		{
			// Initiate the ZipFile
			ZipFile zipFile = new ZipFile(file);

			//Get a list of FileHeader. FileHeader is the header information for all the
			//files in the ZipFile

			List<?> fileHeaderList = zipFile.getFileHeaders();

			// Loop through all the fileHeaders
			for (int i = 0; i < fileHeaderList.size(); i++)
			{
				FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
				if (fileHeader != null)
				{
					// never replace ini files
					if (fileHeader.getFileName().toLowerCase().endsWith(".ini"))
					{
						System.out.println("Ignoring " + fileHeader.getFileName());
						continue;
					}

					System.out.println("Starting " + fileHeader.getFileName());
					//Build the output file
					String outFilePath = destinationPath + ps + fileHeader.getFileName();

					File outFile = new File(outFilePath);

					// extract skip files with a .update extension
					if (skipList.contains(outFile))
					{
						outFile = new File(outFilePath + ".update");
					}
					System.out.println("outFile " + outFile.getAbsolutePath());

					//Checks if the file is a directory
					if (fileHeader.isDirectory())
					{
						//This functionality is up to your requirements
						//For now I create the directory
						outFile.mkdirs();
						continue;
					}

					//Check if the directories(including parent directories)
					//in the output file path exists
					File parentDir = outFile.getParentFile();
					if (!parentDir.exists())
					{
						parentDir.mkdirs();
					}

					//Get the InputStream from the ZipFile
					is = zipFile.getInputStream(fileHeader);
					//Initialize the output stream
					os = new FileOutputStream(outFile);

					int readLen = -1;
					byte[] buff = new byte[BUFF_SIZE];

					//Loop until End of File and write the contents to the output stream
					while ((readLen = is.read(buff)) != -1)
					{
						os.write(buff, 0, readLen);
					}

					//Please have a look into this method for some important comments
					closeFileHandlers(is, os);

					//To restore File attributes (ex: last modified file time, 
					//read only flag, etc) of the extracted file, a utility class
					//can be used as shown below
					UnzipUtil.applyFileAttributes(fileHeader, outFile);

					System.out.println("Done extracting: " + fileHeader.getFileName());
				}
				else
				{
					System.err.println("fileheader is null. Shouldn't be here");
				}
			}
		}
		finally
		{

			closeFileHandlers(is, os);

		}
	}

	private static void closeFileHandlers(ZipInputStream is, OutputStream os) throws IOException
	{
		//Close output stream
		if (os != null)
		{
			os.close();
			os = null;
		}

		//Closing inputstream also checks for CRC of the the just extracted file.
		//If CRC check has to be skipped (for ex: to cancel the unzip operation, etc)
		//use method is.close(boolean skipCRCCheck) and set the flag,
		//skipCRCCheck to false
		//NOTE: It is recommended to close outputStream first because Zip4j throws 
		//an exception if CRC check fails
		if (is != null)
		{
			is.close();
			is = null;
		}
	}
}
