package tools.swing;

//Auto complete or search in a JComboBox

import java.awt.Component;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class AutoCompleteComboBox extends JComboBox<Object>
{
	private static final Locale[] INSTALLED_LOCALES = Locale.getAvailableLocales();

	private ComboBoxModel model = null;

	public static void main(String[] args)
	{
		JFrame f = new JFrame("AutoCompleteComboBox");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		AutoCompleteComboBox box = new AutoCompleteComboBox(INSTALLED_LOCALES, false);
		f.getContentPane().add(box);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	/**	 
	 * Constructor for AutoCompleteComboBox -
	 * The Default Model is a TreeSet which is alphabetically sorted and doesnt allow duplicates.	 
	 * @param items	 
	 */
	public AutoCompleteComboBox(Object[] items, boolean caseSensitive)
	{
		super(items);
		model = new ComboBoxModel(items);
		setModel(model);
		setEditable(true);
		setEditor(new AutoCompleteEditor(this, caseSensitive));
	}

	/**	 
	 * Constructor for AutoCompleteComboBox -
	 * The Default Model is a TreeSet which is alphabetically sorted and doesnt allow
	 * duplicates.	 
	 * @param items
	 */
	public AutoCompleteComboBox(Vector<Object> items, boolean caseSensitive)
	{
		super(items);
		model = new ComboBoxModel(items);
		setModel(model);
		setEditable(true);
		setEditor(new AutoCompleteEditor(this, caseSensitive));
	}

	/**
	 * Constructor for AutoCompleteComboBox -
	 * This constructor uses JComboBox's Default Model which is a Vector.
	 * @param caseSensitive	 
	 */
	public AutoCompleteComboBox(boolean caseSensitive)
	{
		super();
		setEditable(true);
		setEditor(new AutoCompleteEditor(this, caseSensitive));
	}

	/**
	 * ComboBoxModel.java	 
	 */
	public class ComboBoxModel extends DefaultComboBoxModel<Object>
	{
		/**		 
		 * The TreeSet which holds the combobox's data (ordered no duplicates)
		 */
		private TreeSet<Object> values = null;

		public ComboBoxModel(List<Object> items)
		{
			super();
			this.values = new TreeSet<Object>();
			int i, c;
			for (i = 0, c = items.size(); i < c; i++)
			{
				values.add(items.get(i).toString());
			}
			Iterator<Object> it = values.iterator();
			while (it.hasNext())
				super.addElement(it.next().toString());
		}

		public ComboBoxModel(final Object items[])
		{
			this(Arrays.asList(items));
		}
	}

	/**
	 * AutoCompleteEditor.java	 
	 */
	public class AutoCompleteEditor extends BasicComboBoxEditor
	{
		private JTextField autoCompleteEditor = null;

		public AutoCompleteEditor(JComboBox<Object> combo, boolean caseSensitive)
		{
			super();
			autoCompleteEditor = new AutoCompleteEditorComponent(combo, caseSensitive);
		}

		/**
		 * overrides BasicComboBox's getEditorComponent to return custom TextField
		 (AutoCompleteEditorComponent)		 
		 */
		public Component getEditorComponent()
		{
			return autoCompleteEditor;
		}
	}

	/**
	 * AutoCompleteEditorComponent.java	 
	 */
	public class AutoCompleteEditorComponent extends JTextField
	{
		JComboBox<Object> combo = null;

		boolean caseSensitive = false;

		public AutoCompleteEditorComponent(JComboBox<Object> combo, boolean caseSensitive)
		{
			super();
			this.combo = combo;
			this.caseSensitive = caseSensitive;
		}

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
				public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
				{
					if (str == null || str.length() == 0)
						return;
					int size = combo.getItemCount();
					String text = getText(0, getLength());
					for (int i = 0; i < size; i++)
					{
						String item = combo.getItemAt(i).toString();
						if (getLength() + str.length() > item.length())
							continue;
						if (!caseSensitive)
						{
							if ((text + str).equalsIgnoreCase(item))
							{
								combo.setSelectedIndex(i);
								//if (!combo.isPopupVisible())
								//	combo.setPopupVisible(true);
								super.remove(0, getLength());
								super.insertString(0, item, a);
								return;
							}
							else if (item.substring(0, getLength() + str.length()).equalsIgnoreCase(text + str))
							{
								combo.setSelectedIndex(i);
								if (!combo.isPopupVisible())
									combo.setPopupVisible(true);
								super.remove(0, getLength());
								super.insertString(0, item, a);
								return;
							}
						}
						else if (caseSensitive)
						{
							if ((text + str).equals(item))
							{
								combo.setSelectedIndex(i);
								if (!combo.isPopupVisible())
									combo.setPopupVisible(true);
								super.remove(0, getLength());
								super.insertString(0, item, a);
								return;
							}
							else if (item.substring(0, getLength() + str.length()).equals(text + str))
							{
								combo.setSelectedIndex(i);
								if (!combo.isPopupVisible())
									combo.setPopupVisible(true);
								super.remove(0, getLength());
								super.insertString(0, item, a);
								return;
							}
						}
					}
				}
			};
		}
	}
}
