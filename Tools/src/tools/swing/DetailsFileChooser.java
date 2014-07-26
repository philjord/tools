package tools.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

public class DetailsFileChooser extends JFileChooser
{
	private Listener listener;

	public DetailsFileChooser(Listener l)
	{
		this(null, l);
	}

	public DetailsFileChooser(String currentDirectoryPath, Listener l)
	{
		super(currentDirectoryPath);
		this.listener = l;

		setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		JFrame fr = new JFrame();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.getContentPane().add(this);
		fr.setVisible(true);
		fr.setSize(350, 1000);

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION))
				{
					listener.fileSelected(null);
				}
				else if (getSelectedFile() != null && getSelectedFile().isDirectory())
				{
					File selFile = getSelectedFile();
					listener.directorySelected(selFile);
				}
			}
		});

		addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				String prop = e.getPropertyName();				
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop))
				{
					File file = (File) e.getNewValue();
					if (!file.isDirectory())
					{
						listener.fileSelected(file);
					}
				}

			}
		});

		//TODO: VERY POOR FORM
		// pre click the details view
		for (Component c : getComponents())
		{
			if (c instanceof Container)
			{
				for (Component c2 : ((Container) c).getComponents())
				{
					if (c2 instanceof Container)
					{
						for (Component c3 : ((Container) c2).getComponents())
						{
							if (c3 instanceof JToggleButton)
							{
								JToggleButton jtb = (JToggleButton) c3;
								jtb.doClick();
							}
						}
					}
				}
			}
		}
	}

	public static interface Listener
	{
		public void fileSelected(File file);

		public void directorySelected(File dir);
	}
}
