package tools;

import java.util.ArrayList;

/**
 * To be used for inter thread events processing, generally a list of event to be processed.
 * This class ensures minimum synchronization to avoid concurrent modification expection.
 * Concurrently calling get curretn will be bad
 * Notice this is list back so doubles and nulls will be allowed, pending map should be created is needed
 * @author philip
 *
 */
//TODO: use this for statemodel updates
//TODO: use this for comms thread send and recieve
public class PendingList<T>
{
	private ArrayList<T> pendingList = new ArrayList<T>();

	private ArrayList<T> currentPendingList = new ArrayList<T>();

	public void add(T p)
	{
		synchronized (pendingList)
		{
			pendingList.add(p);
		}
	}

	public ArrayList<T> getCurrentPendingList()
	{
		synchronized (pendingList)
		{
			currentPendingList.clear();
			currentPendingList.addAll(pendingList);
			pendingList.clear();
			return currentPendingList;
		}
	}

}
