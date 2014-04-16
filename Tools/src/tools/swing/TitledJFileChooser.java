package tools.swing;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class TitledJFileChooser extends JFileChooser
{
	private String dialogTitle = null;

	public TitledJFileChooser(String currentDirectoryPath)
	{
		super(currentDirectoryPath);
	}

	public String getDialogTitle()
	{
		return dialogTitle;
	}

	public void setDialogTitle(String dialogTitle)
	{
		this.dialogTitle = dialogTitle;
	}

	@Override
	protected JDialog createDialog(Component parent) throws HeadlessException
	{
		JDialog dialog = super.createDialog(parent);
		if (dialogTitle != null)
		{
			dialog.setTitle(dialogTitle);
		}
		return dialog;
	}
}
