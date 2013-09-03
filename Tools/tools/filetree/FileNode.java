package tools.filetree;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class FileNode
{
	protected File m_file;

	public FileNode(File file)
	{
		m_file = file;
	}

	public File getFile()
	{
		return m_file;
	}

	public String toString()
	{
		return m_file.getName().length() > 0 ? m_file.getName() : m_file.getPath();
	}

	public boolean expand(DefaultMutableTreeNode parent)
	{
		DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent.getFirstChild();
		if (flag == null) // No flag
			return false;
		Object obj = flag.getUserObject();
		if (!(obj instanceof Boolean))
			return false; // Already expanded

		parent.removeAllChildren(); // Remove Flag

		File[] files = listFiles();
		if (files == null)
			return true;

		ArrayList<FileNode> v = new ArrayList<FileNode>();

		for (int k = 0; k < files.length; k++)
		{
			File f = files[k];
			if (f.isDirectory())
			{
				FileNode newNode = new FileNode(f);

				if (!v.contains(newNode))
				{
					v.add(newNode);
				}
			}
		}

		for (int k = 0; k < files.length; k++)
		{
			File f = files[k];
			if (!f.isDirectory())
			{
				FileNode newNode = new FileNode(f);

				if (!v.contains(newNode))
				{
					v.add(newNode);
				}
			}
		}

		for (int i = 0; i < v.size(); i++)
		{
			FileNode nd = v.get(i);
			IconData idata = new IconData(FileTree.ICON_FOLDER, FileTree.ICON_EXPANDEDFOLDER, nd);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
			parent.add(node);

			if (nd.hasSubDirs())
				node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		return true;
	}

	public boolean hasSubDirs()
	{
		File[] files = listFiles();
		if (files == null || files.length == 0)
			return false;
		return true;

	}

	public boolean equals(Object o)
	{
		if (o != null && o instanceof FileNode)
		{
			return m_file.equals(((FileNode) o).m_file);
		}
		else
		{
			return false;
		}
	}

	protected File[] listFiles()
	{
		try
		{
			return m_file.listFiles();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "Error reading directory " + m_file.getAbsolutePath(), "Warning", JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}
}
