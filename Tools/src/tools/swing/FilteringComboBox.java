package tools.swing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class FilteringComboBox extends JComboBox
{
	private FilteringComboBoxModel model = null;

	private String currentFilter = "";

	boolean caseSensitive = false;

	/**	 
	 * Constructor for AutoCompleteComboBox -
	 * type into the editor to filter the drop down whitespace represents OR operations and + (plus sign) represent AND operations
	 * so "fred joe+andy"  = contains fred OR contains (joe AND andy)
	 * @param items	 
	 */

	public FilteringComboBox(List<Object> items, boolean caseSensitive)
	{
		super();
		this.caseSensitive = caseSensitive;
		model = new FilteringComboBoxModel(items);
		setModel(model);
		setEditable(true);
		setEditor(new AutoCompleteEditor());
	}

	public FilteringComboBox(boolean caseSensitive)
	{
		this(new ArrayList<Object>(), caseSensitive);
	}

	public FilteringComboBox()
	{
		this(false);
	}

	/** for speed
	 * 
	 * @param objects
	 */
	public void addAll(List<?> objects)
	{
		model.addAll(objects);
	}

	/**
	 * AutoCompleteEditor.java	 
	 */
	public class AutoCompleteEditor extends BasicComboBoxEditor
	{
		protected JTextField createEditorComponent()
		{
			JTextField editor2 = new AutoCompleteEditorComponent();
			editor2.setBorder(null);
			return editor2;
		}
	}

	private void resetPopup()
	{
		if (this.isPopupVisible())
		{
			this.setPopupVisible(false);
			this.setPopupVisible(true);
		}
	}

	/**
	 * AutoCompleteEditorComponent.java	 
	 */
	public class AutoCompleteEditorComponent extends JTextField
	{
		public void setBorder(Border b)
		{
			// no border
		}

		/**
		 * overwritten to return custom PlainDocument which does the work
		 */
		protected Document createDefaultModel()
		{
			return new PlainDocument()
			{
				protected void fireInsertUpdate(DocumentEvent e)
				{
					super.fireInsertUpdate(e);
					model.filterModel(AutoCompleteEditorComponent.this.getText());
					resetPopup();
				}

				protected void fireChangedUpdate(DocumentEvent e)
				{
					super.fireChangedUpdate(e);
					model.filterModel(AutoCompleteEditorComponent.this.getText());
					resetPopup();
				}

				protected void fireRemoveUpdate(DocumentEvent e)
				{
					super.fireRemoveUpdate(e);
					model.filterModel(AutoCompleteEditorComponent.this.getText());
					resetPopup();
				}
			};
		}
	}

	/**
	 * ComboBoxModel.java	 
	 */
	public class FilteringComboBoxModel extends AbstractListModel implements MutableComboBoxModel, Serializable
	{

		private ArrayList<Object> allItems;

		private ArrayList<Object> currentItems = null;

		private Object selectedObject;

		public FilteringComboBoxModel(List<Object> items)
		{
			super();
			this.currentItems = new ArrayList<Object>();
			this.allItems = new ArrayList<Object>();
			currentItems.addAll(items);
			allItems.addAll(items);

			if (getSize() > 0)
			{
				selectedObject = getElementAt(0);
			}
		}

		// implements javax.swing.ComboBoxModel
		/**
		 * Set the value of the selected item. The selected item may be null.
		 * <p>
		 * @param anObject The combo box value or null for no selection.
		 */
		public void setSelectedItem(Object anObject)
		{
			if ((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null && anObject != null)
			{
				selectedObject = anObject;
				fireContentsChanged(this, -1, -1);
			}
		}

		// implements javax.swing.ComboBoxModel
		public Object getSelectedItem()
		{
			return selectedObject;
		}

		// implements javax.swing.ListModel
		public int getSize()
		{
			return currentItems.size();
		}

		// implements javax.swing.ListModel
		public Object getElementAt(int index)
		{
			if (index >= 0 && index < currentItems.size())
				return currentItems.get(index);
			else
				return null;
		}

		/**
		 * Returns the index-position of the specified object in the list.
		 *
		 * @param anObject  
		 * @return an int representing the index position, where 0 is 
		 *         the first position
		 */
		public int getIndexOf(Object anObject)
		{
			return currentItems.indexOf(anObject);
		}

		// implements javax.swing.MutableComboBoxModel
		public void addElement(Object anObject)
		{
			currentItems.add(anObject);
			allItems.add(anObject);
			fireIntervalAdded(this, currentItems.size() - 1, currentItems.size() - 1);
			if (currentItems.size() == 1 && selectedObject == null && anObject != null)
			{
				setSelectedItem(anObject);
			}
		}

		// implements javax.swing.MutableComboBoxModel
		public void insertElementAt(Object anObject, int index)
		{
			currentItems.add(index, anObject);
			allItems.add(index, anObject);
			fireIntervalAdded(this, index, index);
		}

		// implements javax.swing.MutableComboBoxModel
		public void removeElementAt(int index)
		{
			if (getElementAt(index) == selectedObject)
			{
				if (index == 0)
				{
					setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
				}
				else
				{
					setSelectedItem(getElementAt(index - 1));
				}
			}

			currentItems.remove(index);

			fireIntervalRemoved(this, index, index);
		}

		// implements javax.swing.MutableComboBoxModel
		public void removeElement(Object anObject)
		{
			int index = currentItems.indexOf(anObject);
			if (index != -1)
			{
				removeElementAt(index);
			}

			allItems.remove(anObject);
		}

		/**
		 * Empties the list.
		 */
		public void removeAllElements()
		{
			if (currentItems.size() > 0)
			{
				int firstIndex = 0;
				int lastIndex = currentItems.size() - 1;
				currentItems.clear();
				allItems.clear();
				selectedObject = null;
				fireIntervalRemoved(this, firstIndex, lastIndex);
			}
			else
			{
				selectedObject = null;
			}
		}

		/** for speed
		 * 
		 * @param objects
		 */
		public void addAll(List<?> objects)
		{
			currentItems.addAll(objects);
			allItems.addAll(objects);
		}

		private void clearFilter()
		{
			if (currentFilter != null && !currentFilter.equals(""))
			{
				currentFilter = "";
				currentItems.clear();
				currentItems.addAll(allItems);
			}
		}

		public void filterModel(String filter)
		{
			for (Object o : allItems)
			{
				//is the current filter actual an item in the list in which case no filtering at all
				if (filter.equals(o.toString()))
				{
					clearFilter();
					return;
				}
			}

			if (filter == null || filter.equals(""))
			{
				clearFilter();
			}
			else if (!filter.trim().equals(currentFilter))
			{
				currentFilter = filter.trim();
				if (!caseSensitive)
				{
					currentFilter = currentFilter.toLowerCase();
				}

				String[] spaceFilters = currentFilter.split("\\s");
				ArrayList<Object> newItems = new ArrayList<Object>();
				for (Object o : allItems)
				{
					for (String sf : spaceFilters)
					{
						String[] plusFilters = sf.split("\\+");
						boolean hasAll = true;
						for (String pf : plusFilters)
						{
							String ostr = o.toString();
							if (!caseSensitive)
							{
								ostr = ostr.toLowerCase();
							}
							if (ostr.indexOf(pf) == -1)
							{
								hasAll = false;
								break;
							}
						}

						if (hasAll)
						{
							newItems.add(o);
						}
					}
				}

				currentItems.clear();
				currentItems.addAll(newItems);

			}
		}

	}

}
