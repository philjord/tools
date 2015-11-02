package tools;

import java.util.HashMap;
import java.util.Map;

public class RequestStats
{
	private Map<String, ?> cacher;

	private HashMap<String, Stats> statsMap = new HashMap<String, Stats>();

	private long lastSOP = 0;

	public RequestStats(Map<String, ?> cacher)
	{
		this.cacher = cacher;
	}

	public void request(String request)
	{

		Stats stats = statsMap.get(request);
		if (stats == null)
		{
			stats = new Stats(request);
			statsMap.put(request, stats);
		}

		//update stats a new
		stats.lastRequest = System.currentTimeMillis();
		stats.requestCount++;
		if (stats.requestCount > 1)
		{
			if (cacher.get(request) != null)
				stats.requestHit++;
			else
				stats.requestMiss++;
		}

		if (System.currentTimeMillis() - lastSOP > 3000)
		{
			System.out.println("stats out!");
			int countOfSingles = 0;
			int countOfAllHits = 0;
			for (Stats s : statsMap.values())
			{
				if (s.requestCount == 1)
					countOfSingles++;
				else if (s.requestCount - 1 == s.requestHit)
					countOfAllHits++;
				else
					System.out.println("request " + s.request + " last request " + (System.currentTimeMillis() - s.lastRequest)
							+ " requestCount " + s.requestCount + " requestHit " + s.requestHit + " requestMiss " + s.requestMiss);
			}
			System.out.println("countOfSingles = " + countOfSingles);
			System.out.println("countOfAllHits = " + countOfAllHits);

			lastSOP = System.currentTimeMillis();
		}

	}

	private class Stats
	{
		public String request = "";

		public long lastRequest = -Long.MIN_VALUE;

		public long requestCount = 0;

		public long requestHit = 0;

		public long requestMiss = 0;

		public Stats(String request)
		{
			this.request = request;
		}

	}
}
