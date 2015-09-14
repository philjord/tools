package tools.swing;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

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

	public static File requestFolderName(String title, String defaultFolder, Component parent)
	{
		TitledJFileChooser fc = new TitledJFileChooser(defaultFolder);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//fc.setCurrentDirectory(new File(defaultFolder));
		fc.setDialogTitle(title);
		fc.setApproveButtonText("Set");
		fc.showOpenDialog(parent); // note MUST be open or mac osx gives odd result
		File sf = fc.getSelectedFile();
		return sf;
	}
}
