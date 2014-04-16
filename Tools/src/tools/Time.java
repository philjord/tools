package tools;

/**
 * This is the class that keeps track of the virtual time versus real time It has day nigt cycles as well as seasonal
 * cycles. Eventually a weather system might be tied into this.
 * 
 * @author pj
 */
public class Time
{

	private static final int minuteLengthSeconds = 60;

	private static final int hourLengthMinutes = 60;

	private static final int dayLengthHours = 24;

	private static final int weekLengthDays = 7;

	private static final int seasonLengthMonths = 3;

	private static final int yearLengthDays = 364;

	public static final String[] months = new String[]
	{ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	public static final String[] weekDays = new String[]
	{ "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

	public static final int[] monthLengths = new int[]
	{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	public static final String[] seasons = new String[]
	{ "Summer", "Autumn", "Winter", "Spring" };

	public static long getSecondsElapsed(long ms)
	{
		return ms / 1000;
	}

	public static long getMintuesElapsed(long ms)
	{
		return ms / 60000;
	}

	public static long getHoursElapsed(long ms)
	{
		return getMintuesElapsed(ms) / hourLengthMinutes;
	}

	public static long getDaysElapsed(long ms)
	{
		return getHoursElapsed(ms) / dayLengthHours;
	}

	public static int getSecondOfMinute(long ms)
	{
		return (int) getSecondsElapsed(ms) % minuteLengthSeconds;
	}

	public static int getMinuteOfHour(long ms)
	{
		return (int) getMintuesElapsed(ms) % hourLengthMinutes;
	}

	public static int getHourOfDay(long ms)
	{
		return (int) getHoursElapsed(ms) % dayLengthHours;
	}

	public static String getDayOfWeekStr(long ms)
	{
		return weekDays[getDayOfWeek(ms)];
	}

	public static int getDayOfWeek(long ms)
	{
		return (int) getDaysElapsed(ms) % weekLengthDays;
	}

	public static long getDayOfYear(long ms)
	{
		return getDaysElapsed(ms) % yearLengthDays;
	}

	public static long getYear(long ms)
	{
		return getDaysElapsed(ms) / yearLengthDays;
	}

	public static int getDayOfMonth(long ms)
	{
		int month = 0;
		long days = getDayOfYear(ms) - monthLengths[month];

		while (days > 0)
		{
			month++;
			days -= monthLengths[month];
		}
		// take back the final months days (the one that got it below 0)
		return (int) days + monthLengths[month];

	}

	public static String getDayOfMonthStr(long ms)
	{
		long dom = getDayOfMonth(ms);
		return dom + (dom % 10 == 1 ? "st" : dom % 10 == 2 ? "nd" : dom % 10 == 3 ? "rd" : "th");
	}

	public static int getMonth(long ms)
	{
		int month = 0;
		long days = getDayOfYear(ms) - monthLengths[month];

		while (days > 0)
		{
			month++;
			days -= monthLengths[month];
		}
		return month;
	}

	public static String getMonthStr(long ms)
	{
		return months[getMonth(ms)];
	}

	public static int getSeason(long ms)
	{
		return getMonth(ms) / seasonLengthMonths;
	}

	public static String getSeasonStr(long ms)
	{
		return seasons[getSeason(ms)];
	}

	/**
	 * @return
	 */
	public static String getDateString(long ms)
	{
		return getDayOfMonthStr(ms) + " of " + getMonthStr(ms) + ", " + getYear(ms) + " (" + getSeasonStr(ms) + ")";
	}

	/**
	 * @return
	 */
	public static String getTimeString(long ms)
	{
		return "" + (getHourOfDay(ms) < 10 ? "0" : "") + getHourOfDay(ms) + ":" + (getMinuteOfHour(ms) < 10 ? "0" : "") + getMinuteOfHour(ms) + ":"
				+ (getSecondOfMinute(ms) < 10 ? "0" : "") + getSecondOfMinute(ms);
	}
}
