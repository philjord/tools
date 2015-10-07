package tools;

import java.util.ArrayList;

public class QueuingThread extends Thread
{
	private boolean shouldRun = true;

	private ArrayList<Object> queue = new ArrayList<Object>();

	private CallBack callBack;
	
	private boolean newestOnly = false;

	public QueuingThread(CallBack callBack)
	{
		this.callBack = callBack;
	}

	public void addToQueue(Object parameter)
	{
		synchronized (queue)
		{
			queue.add(parameter);
			// wake up the send thread if asleep
			queue.notify();
		}
	}

	public boolean isNewestOnly()
	{
		return newestOnly;
	}

	public void setNewestOnly(boolean newestOnly)
	{
		this.newestOnly = newestOnly;
	}
	
	public void run()
	{
		while (shouldRun)
		{
			synchronized (queue)
			{
				if (queue.size() == 0)
				{
					try
					{
						queue.wait();
					}
					catch (InterruptedException e1)
					{
						if (shouldRun == false)
						{
							// obviously we've been woken in order to end us so just return
							return;
						}
						else
						{
							e1.printStackTrace();
						}
					}
				}
			}

			// note vectors are synchronized so this remove won't interfere with add above
			if (queue.size() > 0)
			{
				Object parameter = null;
				if (newestOnly)
				{
					parameter = queue.get(queue.size() - 1);
					queue.clear();
				}
				else
				{
					parameter = queue.remove(0);
				}
			 
				try
				{
					callBack.run(parameter);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void stopNow()
	{
		shouldRun = false;
		// stop waiting on pops and finish the run method
		interrupt();
	}

	public static interface CallBack
	{
		public void run(Object parameter);
	}
}
