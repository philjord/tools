package tools;

import javax.swing.JComponent;
import javax.swing.JTextArea;

/**
 * A crap output log system log4j will very soon replace this
 * @author main
 *
 */
public class OutputLogger
{
	//TODO: use log4j instead of this class
	private static JTextArea outputArea = new JTextArea();

	public static JComponent getOutput()
	{
		outputArea.setEditable(false);
		return outputArea;
	}

	public static void logEntry(String logEntry)
	{
		logEntry(logEntry, 0);
	}

	public static void logEntry(String logEntry, int errorLevel)
	{
		errorLevel++; // just to remove the warning
		outputArea.append(logEntry + "\n");
		outputArea.setCaretPosition(outputArea.getDocument().getLength());

		System.out.println(logEntry);
		outputArea.validate();
		// and various others
	}

}
