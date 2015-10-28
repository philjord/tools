/*
 * Created on Apr 23, 2006
 */
package old.actionsetaction;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import tools.GlobalKeyCatcher;

/**
 * @author Administrator
 * 
 */
public class ActionSetActionView extends JPanel implements KeyListener
{
	private Vector<RegisteredActionSetActionListener> registeredActionSetActionListeners = new Vector<RegisteredActionSetActionListener>();

	private static int ACTION_SET_1_KEY = KeyEvent.VK_F1;

	private static int ACTION_SET_2_KEY = KeyEvent.VK_F2;

	private static int ACTION_SET_3_KEY = KeyEvent.VK_F3;

	private static int ACTION_SET_4_KEY = KeyEvent.VK_F4;

	private JButton actionSet1Button = new JButton("1");

	private JButton actionSet2Button = new JButton("2");

	private JButton actionSet3Button = new JButton("3");

	private JButton actionSet4Button = new JButton("4");

	private int currentActionSet = 1;

	private JButton[] actionButtons = new JButton[10];

	private JPanel actionSetButtonPanel = new JPanel();

	private JPanel actionButtonPanel = new JPanel();

	public ActionSetActionView(GlobalKeyCatcher globalKeyCatcher)
	{
		globalKeyCatcher.addKeyListener(this);

		this.setLayout(new GridLayout(2, 1));
		actionSetButtonPanel.setLayout(new GridLayout(1, 0));
		actionButtonPanel.setLayout(new GridLayout(1, 0));

		actionSetButtonPanel.add(actionSet1Button);
		actionSetButtonPanel.add(actionSet2Button);
		actionSetButtonPanel.add(actionSet3Button);
		actionSetButtonPanel.add(actionSet4Button);

		for (int i = 0; i < actionButtons.length; i++)
		{
			JButton actionButton = new JButton();
			actionButtons[i] = actionButton;
			actionButtonPanel.add(actionButton);

			actionButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// which button but?
					for (int i2 = 0; i2 < actionButtons.length; i2++)
					{
						if (e.getSource() == actionButtons[i2])
						{
							fireAction(currentActionSet, i2);
							break;
						}
					}
				}
			});
		}
		// now to put the 0 button at the 0 end of the panel (like the keyboard)
		actionButtonPanel.remove(actionButtons[0]);
		actionButtonPanel.add(actionButtons[0]);

		this.add(actionSetButtonPanel);
		this.add(actionButtonPanel);

		actionSet1Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setActionSet(1);
			}
		});
		actionSet2Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setActionSet(2);
			}
		});
		actionSet3Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setActionSet(3);
			}
		});
		actionSet4Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setActionSet(4);
			}
		});
		setActionSet(1);

	}

	public void addActionSetActionListener(int actionSet, int action, String buttonText, ActionSetActionListener listener)
	{
		if (getListenerFor(actionSet, action) == null)
		{
			RegisteredActionSetActionListener rasal = new RegisteredActionSetActionListener();
			rasal.actionSet = actionSet;
			rasal.action = action;
			rasal.buttonText = buttonText;
			rasal.listener = listener;

			registeredActionSetActionListeners.add(rasal);

			// and update buttons
			setActionSet(currentActionSet);

		}
		else
		{
			new Exception("You can't register 2 listeners for " + actionSet + " " + action).printStackTrace();
		}

	}

	public void removeActionSetActionListener(int actionSet, int action, ActionSetActionListener listener)
	{
		RegisteredActionSetActionListener rasal = getListenerFor(actionSet, action);
		if (rasal != null && rasal.listener == listener)
		{
			registeredActionSetActionListeners.remove(rasal);

			// and update buttons
			setActionSet(currentActionSet);
		}
	}

	private RegisteredActionSetActionListener getListenerFor(int actionSet, int action)
	{
		for (RegisteredActionSetActionListener rasal : registeredActionSetActionListeners)
		{
			if (rasal.actionSet == actionSet && rasal.action == action)
			{
				return rasal;
			}
		}
		return null;
	}

	public void setActionSet(int actionSet)
	{
		this.currentActionSet = actionSet;

		for (int i = 0; i < actionButtons.length; i++)
		{
			// find a text if one exists
			RegisteredActionSetActionListener rasal = getListenerFor(currentActionSet, i);
			if (rasal != null)
			{
				actionButtons[i].setText(rasal.buttonText);
			}
			else
			{
				actionButtons[i].setText("");
			}
		}
	}

	private void fireAction(int actionSet, int action)
	{
		RegisteredActionSetActionListener rasal = getListenerFor(actionSet, action);
		if (rasal != null)
		{
			rasal.listener.actionPerformed(actionSet, action);
		}
	}

	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == ACTION_SET_1_KEY)
		{
			setActionSet(1);
		}
		else if (e.getKeyCode() == ACTION_SET_2_KEY)
		{
			setActionSet(2);
		}
		else if (e.getKeyCode() == ACTION_SET_3_KEY)
		{
			setActionSet(3);
		}
		else if (e.getKeyCode() == ACTION_SET_4_KEY)
		{
			setActionSet(4);
		}
		else if (e.getKeyCode() == KeyEvent.VK_0)
		{
			fireAction(currentActionSet, 0);
		}
		else if (e.getKeyCode() == KeyEvent.VK_1)
		{
			fireAction(currentActionSet, 1);
		}
		else if (e.getKeyCode() == KeyEvent.VK_2)
		{
			fireAction(currentActionSet, 2);
		}
		else if (e.getKeyCode() == KeyEvent.VK_3)
		{
			fireAction(currentActionSet, 3);
		}
		else if (e.getKeyCode() == KeyEvent.VK_4)
		{
			fireAction(currentActionSet, 4);
		}
		else if (e.getKeyCode() == KeyEvent.VK_5)
		{
			fireAction(currentActionSet, 5);
		}
		else if (e.getKeyCode() == KeyEvent.VK_6)
		{
			fireAction(currentActionSet, 6);
		}
		else if (e.getKeyCode() == KeyEvent.VK_7)
		{
			fireAction(currentActionSet, 7);
		}
		else if (e.getKeyCode() == KeyEvent.VK_8)
		{
			fireAction(currentActionSet, 8);
		}
		else if (e.getKeyCode() == KeyEvent.VK_9)
		{
			fireAction(currentActionSet, 9);
		}

	}

	public void keyTyped(KeyEvent e)
	{

	}

	public void keyPressed(KeyEvent e)
	{
	}

	private class RegisteredActionSetActionListener
	{
		public int actionSet = -1;

		public int action = -1;

		public String buttonText = "";

		public ActionSetActionListener listener;
	}

}
