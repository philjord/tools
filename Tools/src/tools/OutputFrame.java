package tools;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Just a wee class to bung a frame up with tesxt in it
 */
public class OutputFrame
{
	private JTextArea outputArea = new JTextArea();

	public OutputFrame(String title, int width, int height)
	{
		JFrame outputFrame = new JFrame(title);
		outputFrame.setSize(width, height);
		outputFrame.getContentPane().add(new JScrollPane(outputArea));
		outputFrame.setVisible(true);
	}

	public void addText(String newText)
	{
		outputArea.append(newText);
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}

	public void setText(String newText)
	{
		outputArea.setText(newText);
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}

}
