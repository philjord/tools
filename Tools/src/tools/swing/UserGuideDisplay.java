package tools.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class UserGuideDisplay
{
	private JEditorPane editorPane = null;
	private JDialog dialog = null;

	public void display(Component parent, String htmlFile)
	{
		if (dialog == null)
		{
			try
			{
				editorPane = new JEditorPane();
				// Turn anti-aliasing on
				System.setProperty("awt.useSystemAAFontSettings", "on");
				// Enable use of custom set fonts
				editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
				editorPane.setFont(new Font("Arial", Font.BOLD, 13));

				editorPane.setPreferredSize(new Dimension(750, 550));
				editorPane.setEditable(false);
				editorPane.setContentType("text/html");

				editorPane.setPage(new File(htmlFile).toURI().toURL());

				// TIP: Make the JOptionPane resizable using the HierarchyListener
				editorPane.addHierarchyListener(new HierarchyListener() {
					public void hierarchyChanged(HierarchyEvent e)
					{
						Window window = SwingUtilities.getWindowAncestor(editorPane);
						if (window instanceof Dialog)
						{
							Dialog dialog = (Dialog) window;
							if (!dialog.isResizable())
							{
								dialog.setResizable(true);
							}
						}
					}
				});

				// TIP: Add Hyperlink listener to process hyperlinks
				editorPane.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(final HyperlinkEvent e)
					{
						if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
						{
							EventQueue.invokeLater(new Runnable() {
								public void run()
								{
									// TIP: Show hand cursor
									SwingUtilities.getWindowAncestor(editorPane).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
									// TIP: Show URL as the tooltip
									editorPane.setToolTipText(e.getURL().toExternalForm());
								}
							});
						}
						else if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
						{
							EventQueue.invokeLater(new Runnable() {
								public void run()
								{
									// Show default cursor
									SwingUtilities.getWindowAncestor(editorPane).setCursor(Cursor.getDefaultCursor());

									// Reset tooltip
									editorPane.setToolTipText(null);
								}
							});
						}
						else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
						{
							// TIP: Starting with JDK6 you can show the URL in desktop browser
							if (Desktop.isDesktopSupported())
							{
								try
								{
									Desktop.getDesktop().browse(e.getURL().toURI());
								}
								catch (Exception ex)
								{
									ex.printStackTrace();
								}
							}
							//System.out.println("Go to URL: " + e.getURL());
						}
					}
				});

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			//JOptionPane.showMessageDialog(parent, new JScrollPane(editorPane), "User Guide", JOptionPane.PLAIN_MESSAGE);
			JOptionPane pane = new JOptionPane(new JScrollPane(editorPane), JOptionPane.PLAIN_MESSAGE);
			// Configure via set methods
			dialog = pane.createDialog(parent, "User Guide");
			// the line below is added to the example from the docs
			dialog.setModal(false); // this says not to block background components
			dialog.setVisible(true);
		}
		else
		{
			dialog.setVisible(true);
		}
	}

}
