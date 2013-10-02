package tools.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/*
 * ListDialog.java is meant to be used by programs such as
 * ListDialogRunner.  It requires no additional files.
 */

/**
 * Use this modal dialog to let the user choose one string from a long
 * list.  See ListDialogRunner.java for an example of using ListDialog.
 * The basics:
 * <pre>
    String[] choices = {"A", "long", "array", "of", "strings"};
    String selectedName = ListDialog.showDialog(
                                componentInControllingFrame,
                                locatorComponent,
                                "A description of the list:",
                                "Dialog Title",
                                choices,
                                choices[0]);
 * </pre>
 */
public class ListDialog extends JDialog implements ActionListener
{
	private static ListDialog dialog;

	private static Object value = null;

	private JList list;

	/**
	 * Set up and show the dialog.  The first Component argument
	 * determines which frame the dialog depends on; it should be
	 * a component in the dialog's controlling frame. The second
	 * Component argument should be null if you want the dialog
	 * to come up with its left corner in the center of the screen;
	 * otherwise, it should be the component on top of which the
	 * dialog should appear.
	 */
	public static Object showDialog(Component frameComp, Component locationComp, String labelText, String title, Object[] possibleValues,
			Object initialValue, Object longValue)
	{
		Frame frame = JOptionPane.getFrameForComponent(frameComp);
		dialog = new ListDialog(frame, locationComp, labelText, title, possibleValues, initialValue, longValue);
		dialog.setVisible(true);
		return value;
	}

	private void setValue(Object newValue)
	{
		value = newValue;
		list.setSelectedValue(value, true);
	}

	@SuppressWarnings("unchecked")
	private ListDialog(Frame frame, Component locationComp, String labelText, String title, Object[] data, Object initialValue,
			Object longValue)
	{
		super(frame, title, true);

		//Create and initialize the buttons.
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		//
		final JButton setButton = new JButton("Ok");
		setButton.setActionCommand("Ok");
		setButton.addActionListener(this);
		getRootPane().setDefaultButton(setButton);

		//main part of the dialog
		list = new JList(data)
		{
			//Subclass JList to workaround bug 4832765, which can cause the
			//scroll pane to not let the user easily scroll up to the beginning
			//of the list.  An alternative would be to set the unitIncrement
			//of the JScrollBar to a fixed value. You wouldn't get the nice
			//aligned scrolling, but it should work.
			public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
			{
				int row;
				if (orientation == SwingConstants.VERTICAL && direction < 0 && (row = getFirstVisibleIndex()) != -1)
				{
					Rectangle r = getCellBounds(row, row);
					if ((r.y == visibleRect.y) && (row != 0))
					{
						Point loc = r.getLocation();
						loc.y--;
						int prevIndex = locationToIndex(loc);
						Rectangle prevR = getCellBounds(prevIndex, prevIndex);

						if (prevR == null || prevR.y >= r.y)
						{
							return 0;
						}
						return prevR.height;
					}
				}
				return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
			}
		};

		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		if (longValue != null)
		{
			list.setPrototypeCellValue(longValue); //get extra space
		}
		//list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					setButton.doClick(); //emulate button click
				}
			}
		});
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);

		//Create a container so that we can add a title around
		//the scroll pane.  Can't add a title directly to the
		//scroll pane because its background would be white.
		//Lay out the label and scroll pane from top to bottom.
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel(labelText);
		label.setLabelFor(list);
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0, 5)));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		//Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		//buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		//buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(setButton);
		//buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(cancelButton);

		//Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);

		//Initialize values.
		setValue(initialValue);
		pack();
		setLocationRelativeTo(locationComp);
	}

	//Handle clicks on the Set and Cancel buttons.
	public void actionPerformed(ActionEvent e)
	{
		if ("Ok".equals(e.getActionCommand()))
		{
			ListDialog.value = list.getSelectedValue();
		}
		else if ("Cancel".equals(e.getActionCommand()))
		{
			ListDialog.value = null;
		}
		ListDialog.dialog.setVisible(false);
	}
}
