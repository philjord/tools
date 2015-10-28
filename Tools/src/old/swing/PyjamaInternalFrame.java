package old.swing;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * This does very little more than JInternalFrame other than try to give a decent default opening position
 * @author pj
 */
public class PyjamaInternalFrame extends JInternalFrame implements InternalFrameListener
{
	public static int openFrameCount = 0;

	public static final int xOffset = 30;

	public static final int yOffset = 30;

	public PyjamaInternalFrame(String title)
	{
		//name, resizable, closable, maximizable, iconifiable
		super(title, true, false, true, true);

		//set the window size probably to be overridden by the subclass
		setSize(300, 300);

		setDefaultLocation();

		addInternalFrameListener(this);
	}

	public void setDefaultLocation()
	{
		setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
	}

	public void internalFrameActivated(InternalFrameEvent e)
	{
	}

	public void internalFrameClosed(InternalFrameEvent e)
	{
		openFrameCount--;
	}

	public void internalFrameClosing(InternalFrameEvent e)
	{
	}

	public void internalFrameDeactivated(InternalFrameEvent e)
	{
	}

	public void internalFrameDeiconified(InternalFrameEvent e)
	{
	}

	public void internalFrameIconified(InternalFrameEvent e)
	{
	}

	public void internalFrameOpened(InternalFrameEvent e)
	{
		openFrameCount++;
	}
}
