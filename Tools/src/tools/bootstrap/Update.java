package tools.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;

public class Update
{
	
	//TEST values
	/*String zipfile = "E:\\Java\\installers\\ElderScrollsExplorer\\update\\ElderScrollsExplorer v2.02.zip";//args[0];
	String unzipPath = "E:\\Java\\installers";//args[1];
	String rootDirectory = "E:\\Java\\installers\\ElderScrollsExplorer";//args[2];
	String restartJar = "E:\\Java\\installers\\ElderScrollsExplorer\\ElderScrollsExplorer.jar";//args[3];

	// do the unzip but skip the update.jar file (of course) or ignore error

	ArrayList<File> skipList = new ArrayList<File>();
	skipList.add(new File(Update.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
	skipList.add(new File("E:\\Java\\installers\\ElderScrollsExplorer\\lib\\update.jar"));
*/
	private static final int BUFF_SIZE = 4096;

	public static String ps = System.getProperty("file.separator");

	public static String fs = System.getProperty("path.separator");

	public static void main(String[] args) throws Exception
	{
		
		String zipfile = args[0];
		String unzipPath = args[1];
		String rootDirectory = args[2];
		String restartJar = args[3];

		// do the unzip but skip the update.jar file (of course) or ignore error

		ArrayList<File> skipList = new ArrayList<File>();
		skipList.add(new File(Update.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
		
		if (new File(zipfile).exists())
		{
			// wait for a moment to ensure callee has closed
			Thread.sleep(2000);	
			processFile(new File(zipfile), unzipPath, skipList);
			Thread.sleep(2000);
		}				
		
		//now restart the callee jar
		String javaExe = "java";// just call the path version by default

		//find out if a JRE folder exists, and use it if possible
		File possibleJreFolder = new File(rootDirectory + "\\jre");
		if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
		{
			javaExe = rootDirectory + "\\jre\\bin\\java";
		}

		ProcessBuilder pb = new ProcessBuilder(javaExe, "-jar", restartJar);
		pb.start();
		// wait just a bit longer...
		Thread.sleep(2000);
		System.exit(0);
	}

	public static void processFile(File file, String destinationPath, List<File> skipList)
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
					

					//Build the output file
					String outFilePath = destinationPath + System.getProperty("file.separator") + fileHeader.getFileName();
					File outFile = new File(outFilePath);
					
					// skip anything in the skiplist jar 
					if (skipList.contains(outFile))					
						continue;

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
		catch (ZipException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				closeFileHandlers(is, os);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
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
