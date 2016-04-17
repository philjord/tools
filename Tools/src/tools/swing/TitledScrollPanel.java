package tools.swing;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

/**
 * @author pj
 */
public class TitledScrollPanel extends JPanel
{
	public TitledScrollPanel(String title, Component comp)
	{
		setLayout(new GridLayout(1, 1));
		setBorder(new TitledBorder(title));
		add(new JScrollPane(comp));
	}
}
