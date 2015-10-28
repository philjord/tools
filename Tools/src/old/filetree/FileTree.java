package old.filetree;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

public class FileTree extends JFrame
{
	public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");

	public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");

	public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");

	public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon("expandedfolder.gif");

	protected JTree m_tree;

	protected DefaultTreeModel m_model;

	protected JTextField m_display;

	public FileTree()
	{
		this(null);

	}

	public FileTree(String initialSelection)
	{
		super("Directories Tree");
		setSize(400, 300);

		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(ICON_COMPUTER, null, "Computer"));

		DefaultMutableTreeNode node;
		File[] roots = File.listRoots();
		for (int k = 0; k < roots.length; k++)
		{
			node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null, new FileNode(roots[k])));
			top.add(node);
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		m_model = new DefaultTreeModel(top);
		m_tree = new JTree(m_model);

		m_tree.putClientProperty("JTree.lineStyle", "Angled");

		TreeCellRenderer renderer = new IconCellRenderer();
		m_tree.setCellRenderer(renderer);

		m_tree.addTreeExpansionListener(new DirExpansionListener());

		m_tree.addTreeSelectionListener(new DirSelectionListener());

		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(true);
		m_tree.setEditable(false);

		JScrollPane s = new JScrollPane();
		s.getViewport().add(m_tree);
		getContentPane().add(s, BorderLayout.CENTER);

		m_display = new JTextField();
		m_display.setEditable(false);
		getContentPane().add(m_display, BorderLayout.NORTH);

		setVisible(true);
	}

	public JTree getTree()
	{
		return m_tree;
	}

	public static FileNode getFileNode(DefaultMutableTreeNode node)
	{
		if (node == null)
			return null;
		Object obj = node.getUserObject();
		if (obj instanceof IconData)
			obj = ((IconData) obj).getObject();
		if (obj instanceof FileNode)
			return (FileNode) obj;
		else
			return null;
	}

	// Make sure expansion is threaded and updating the tree model
	// only occurs within the event dispatching thread.
	class DirExpansionListener implements TreeExpansionListener
	{
		public void treeExpanded(TreeExpansionEvent event)
		{
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) (event.getPath().getLastPathComponent());
			final FileNode fnode = getFileNode(node);

			Thread runner = new Thread()
			{
				public void run()
				{
					if (fnode != null && fnode.expand(node))
					{
						Runnable runnable = new Runnable()
						{
							public void run()
							{
								m_model.reload(node);
							}
						};
						SwingUtilities.invokeLater(runnable);
					}
				}
			};
			runner.start();
		}

		public void treeCollapsed(TreeExpansionEvent event)
		{
		}
	}

	class DirSelectionListener implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent event)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) (event.getPath().getLastPathComponent());
			FileNode fnode = getFileNode(node);
			if (fnode != null)
				m_display.setText(fnode.getFile().getAbsolutePath());
			else
				m_display.setText("");
		}
	}

}
