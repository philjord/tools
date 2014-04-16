package tools.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.prefs.Preferences;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;
import tools.swing.DetailsFileChooser;
@SuppressWarnings("all")
public class HexPasswordUnzip
{
	private static final int BUFF_SIZE = 4096;

	private static char[] pchars = new char[]
	{ 0xB7, 0x27, 0x4A, 0x3B, 0xCB, 0xDD, 0x4B, 0xD8, 0xB4, 0xCD, 0x8D, 0xD8, 0x2D, 0x8F, 0x00, 0xDB };

	private static String password = new String(pchars);

	private static Preferences prefs;

	public static void main(String[] args)
	{
		prefs = Preferences.userNodeForPackage(HexPasswordUnzip.class);

		DetailsFileChooser dfc = new DetailsFileChooser(prefs.get("zipfiles", ""), new DetailsFileChooser.Listener()
		{
			@Override
			public void directorySelected(File dir)
			{
				prefs.put("HexPasswordUnzip", dir.getAbsolutePath());
				processDir(dir);
			}

			@Override
			public void fileSelected(File file)
			{
				prefs.put("HexPasswordUnzip", file.getAbsolutePath());
				System.out.println("Selected file: " + file);
				processFile(file);
			}
		});

	}

	private static void processFile(File file)
	{

		ZipInputStream is = null;
		OutputStream os = null;

		try
		{
			// Initiate the ZipFile
			ZipFile zipFile = new ZipFile(file);
			String destinationPath = file.getAbsolutePath() + ".out";

			// If zip file is password protected then set the password
			if (zipFile.isEncrypted())
			{
				zipFile.setPassword(password);
			}

			//Get a list of FileHeader. FileHeader is the header information for all the
			//files in the ZipFile
			 
			List fileHeaderList = zipFile.getFileHeaders();

			// Loop through all the fileHeaders
			for (int i = 0; i < fileHeaderList.size(); i++)
			{
				FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
				if (fileHeader != null)
				{

					//Build the output file
					String outFilePath = destinationPath + System.getProperty("file.separator") + fileHeader.getFileName();
					File outFile = new File(outFilePath);

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

	private static void processDir(File dir)
	{
		System.out.println("Processing directory " + dir);
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++)
		{
			try
			{
				if (fs[i].isDirectory())
				{
					processDir(fs[i]);
				}
				else
				{
					System.out.println("\tFile: " + fs[i]);
					processFile(fs[i]);
				}

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

}
