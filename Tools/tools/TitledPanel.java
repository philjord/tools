package tools;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class TitledPanel extends JPanel
{
	public TitledPanel(String title, Component comp)
	{
		setLayout(new GridLayout(1, 1));
		setBorder(new TitledBorder(title));
		add(comp);
	}
}
