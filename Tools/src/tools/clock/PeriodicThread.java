package tools.clock;

public class PeriodicThread implements Runnable
{
	private boolean shouldRun = true;

	private boolean paused = false;

	private String clockName;

	private long tickSizeMS = Long.MAX_VALUE;

	private PeriodicallyUpdated tickable;

	private Thread thread;

	public PeriodicThread(String clockName, long tickSizeMS, PeriodicallyUpdated tickable)
	{
		this.clockName = clockName;
		this.tickable = tickable;
		this.tickSizeMS = tickSizeMS;

	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, clockName);
			thread.setDaemon(true);
			thread.start();
		}
	}

	public void stop()
	{
		shouldRun = false;
	}

	@Override
	public void run()
	{
		shouldRun = true;

		// long runStartNS = System.currentTimeMillis();
		long runStartNS = System.nanoTime();
		long totalElapsedTimeMS = 0;
		long timeInSteps = 0;
		long sleepTime = 0;

		int stepCount = 0;

		while (shouldRun)
		{
			//pausing just stops step counts and tick calls, the actualy clock just runs on
			if (!paused)
			{
				try
				{
					tickable.runUpdate();
				}
				catch (Exception e)
				{
					System.err.println("Exception caught in clock " + this.clockName);
					e.printStackTrace();
				}

				stepCount++;
			}

			try
			{
				// now sleep for the right time so it is real time.
				totalElapsedTimeMS = (System.nanoTime() - runStartNS) / 1000000;

				timeInSteps = stepCount * tickSizeMS;
				sleepTime = timeInSteps - totalElapsedTimeMS;

				if (sleepTime > 0)
				{
					// System.out.println(clockName + " sleeping for " + sleepTime);
					Thread.sleep(sleepTime);
				}
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	public void pause()
	{
		paused = true;
	}

	public void unpause()
	{
		paused = false;
	}

	public boolean isPaused()
	{
		return paused;
	}

	public void setName(String name)
	{
		if (thread != null)
		{
			thread.setName(name);
		}
	}
}