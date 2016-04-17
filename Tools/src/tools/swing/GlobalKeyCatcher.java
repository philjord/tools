package tools.swing;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

public class GlobalKeyCatcher implements KeyEventDispatcher
{
	private Vector<KeyListener> keyListeners = new Vector<KeyListener>();

	private boolean enabled = true;

	public GlobalKeyCatcher()
	{
		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kfm.addKeyEventDispatcher(this);
	}

	public boolean dispatchKeyEvent(KeyEvent e)
	{
		if (enabled)
		{
			for (KeyListener keyListener : keyListeners)
			{
				int id = e.getID();
				if (id == KeyEvent.KEY_PRESSED)
				{
					keyListener.keyPressed(e);
				}
				else if (id == KeyEvent.KEY_RELEASED)
				{
					keyListener.keyReleased(e);
				}
				else if (id == KeyEvent.KEY_TYPED)
				{
					keyListener.keyTyped(e);
				}
			}
		}

		// Note we return false to allow for the normal key handling to occur
		return false;
	}

	public void setEnabled(boolean newState)
	{
		enabled = newState;
	}

	public void addKeyListener(KeyListener keyListener)
	{
		if (!keyListeners.contains(keyListener))
		{
			keyListeners.add(keyListener);
		}
	}

	public void removeKeyListener(KeyListener keyListener)
	{
		keyListeners.remove(keyListener);
	}
}
