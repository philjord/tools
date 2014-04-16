package tools.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class TableStringTransferHandler extends TransferHandler
{
	public TableStringTransferHandler()
	{
	}

	public int getSourceActions(JComponent c)
	{
		return COPY;
	}

	public Transferable createTransferable(JComponent comp)
	{
		if (comp instanceof JTable)
		{
			return new StringSelection(createTransferString((JTable) comp));
		}
		else
		{
			return null;
		}
	}

	protected String createTransferString(JTable table)
	{
		String selectionString = "";
		//Get the min and max ranges of selected cells
		int rowIndexStart = table.getSelectedRow();
		int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();
		int colIndexStart = table.getSelectedColumn();
		int colIndexEnd = table.getColumnModel().getSelectionModel().getMaxSelectionIndex();

		// Check each cell in the range
		for (int r = rowIndexStart; r <= rowIndexEnd; r++)
		{
			for (int c = colIndexStart; c <= colIndexEnd; c++)
			{
				if (table.isCellSelected(r, c))
				{
					selectionString += table.getValueAt(r, c);
				}
				if (c < colIndexEnd)
				{
					selectionString += "\t";
				}
			}
			if (r < rowIndexEnd)
			{
				selectionString += "\n";
			}
		}

		return selectionString;
	}

	public boolean canImport(TransferSupport supp)
	{
		// Check for String flavor
		if (!supp.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			return false;
		}
		if (supp.getComponent() instanceof JTable)
		{
			JTable table = (JTable) supp.getComponent();
			if (supp.isDrop())
			{
				// Fetch the drop location, must be a single editable cell
				JTable.DropLocation loc = (JTable.DropLocation) supp.getDropLocation();
				return table.isCellEditable(loc.getRow(), loc.getColumn());
			}
			else
			{
				//allow any paste as teh grid to paste may hit any cell and will sort out in the import
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	public boolean importData(TransferSupport supp)
	{
		if (!canImport(supp))
		{
			return false;
		}

		try
		{
			if (supp.getComponent() instanceof JTable)
			{

				JTable table = (JTable) supp.getComponent();
				int importRow = -1;
				int importCol = -1;
				if (supp.isDrop())
				{
					// Fetch the drop location
					JTable.DropLocation loc = (JTable.DropLocation) supp.getDropLocation();
					importRow = loc.getRow();
					importCol = loc.getColumn();
				}
				else
				{
					importRow = table.getSelectedRow();
					importCol = table.getSelectedColumn();
				}

				// Fetch the Transferable and its data
				Transferable t = supp.getTransferable();

				String data = (String) t.getTransferData(DataFlavor.stringFlavor);

				String[] rows = data.split("\n");
				for (int r = 0; r < rows.length; r++)
				{
					String[] cells = rows[r].split("\t");
					for (int c = 0; c < cells.length; c++)
					{
						int targetRow = r + importRow;
						int targetCol = c + importCol;

						if (targetRow < table.getRowCount() && targetCol < table.getColumnCount() && table.isCellEditable(targetRow, targetCol))
						{
							table.setValueAt(cells[c], targetRow, targetCol);
						}
					}
				}

				return true;
			}
		}
		catch (UnsupportedFlavorException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}
}
