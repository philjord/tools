package old;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.JRootPane;

import tools.swing.StringStore;

public class ComponentPositionsPrefsPersistor implements ComponentListener
{
	public static String UNIQUE_PREFIX = "COMPONENT_RECTANGLE_";

	/**
	 * This class will write all component events out to the registry (in win case) and will listen to the entire tree
	 * of components from the root down Each component MUST have a unique name set by setName() or it will be ignored
	 */
	private HashMap<String, Component> components = new HashMap<String, Component>();

	private Preferences prefs = Preferences.userNodeForPackage(ComponentPositionsPrefsPersistor.class);

	public ComponentPositionsPrefsPersistor(Component root)
	{
		// find all named components under this root
		resolveTree(root);
	}

	/**
	 * This must be called before listening is started or saved values will be overwritten
	 */
	public void loadPositions()
	{
		// lets see if we can get component rec out of the prefs
		for (Component com : components.values())
		{
			String key = UNIQUE_PREFIX + com.getName();
			String value = prefs.get(key, "");
			if (!value.equals(""))
			{
				Rectangle newRec = StringStore.getRectangleFromComponentParamString(value);
				com.setBounds(newRec);
			}
		}
	}

	public void startListening()
	{
		for (Component com : components.values())
		{
			com.addComponentListener(this);
		}
	}

	public void stopListening()
	{
		for (Component com : components.values())
		{
			com.removeComponentListener(this);
		}
	}

	private void resolveTree(Component com)
	{
		if (com.getName() != null)
		{
			if (!components.containsKey(com.getName()))
			{
				components.put(com.getName(), com);
			}
			else
			{
				new Exception("chaos and bedlam, duplicate component names! " + com).printStackTrace();
			}
		}

		// now resolve children
		if (com instanceof Container)
		{
			Container con = (Container) com;
			Component[] comps;
			if (con instanceof JRootPane)
			{
				comps = ((JRootPane) con).getContentPane().getComponents();
			}
			else
			{
				comps = con.getComponents();
			}

			for (int i = 0; i < comps.length; i++)
			{
				resolveTree(comps[i]);
			}
		}
	}

	private void store(ComponentEvent e)
	{
		String key = UNIQUE_PREFIX + e.getComponent().getName();
		String value = e.paramString();
		prefs.put(key, value);
	}

	public void componentHidden(ComponentEvent e)
	{
		// nothing
	}

	public void componentMoved(ComponentEvent e)
	{
		store(e);
	}

	public void componentResized(ComponentEvent e)
	{
		store(e);
	}

	public void componentShown(ComponentEvent e)
	{
		// nothing
	}
}