package tools;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class WeakListenerList<E> implements Iterable<E>
{
	private ArrayList<WeakReference<E>> refs = new ArrayList<WeakReference<E>>();

	private final ReferenceQueue<E> queue = new ReferenceQueue<E>();

	public WeakListenerList()
	{
	}

	public void add(E e)
	{
		expungeStaleEntries();
		if (indexOf(e) == -1)
		{
			refs.add(new WeakReference<E>(e, queue));
		}
	}

	public void remove(E e)
	{
		expungeStaleEntries();
		if (indexOf(e) != -1)
		{
			refs.remove(indexOf(e));
		}
	}

	public void clear()
	{
		refs.clear();
		// clear the queue
		while (queue.poll() != null)
			;
	}

	public int size()
	{
		expungeStaleEntries();
		return refs.size();
	}

	public E get(int index)
	{
		expungeStaleEntries();
		return refs.get(index).get();
	}

	private int indexOf(E e)
	{
		for (int i = 0; i < refs.size(); i++)
		{
			WeakReference<E> wr = refs.get(i);
			if (wr != null && wr.get() == e)
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Expunges stale entries from the table.
	 */
	private void expungeStaleEntries()
	{
		Object e;
		while ((e = queue.poll()) != null)
		{
			refs.remove(e);
		}
	}

	/**
	 * Only for for each convinence!!!
	 */
	public Iterator<E> iterator()
	{
		return new Itr();
	}

	private class Itr implements Iterator<E>
	{
		private int currentIteratorIndex = 0;

		public boolean hasNext()
		{
			if (currentIteratorIndex < refs.size())
			{
				WeakReference<E> wr = refs.get(currentIteratorIndex);
				E e = wr == null ? null : wr.get();
				while (e == null && currentIteratorIndex < refs.size() - 1)
				{
					currentIteratorIndex++;
					wr = refs.get(currentIteratorIndex);
					e = wr == null ? null : wr.get();					
				}
				return e != null;
			}
			return false;
		}

		public E next()
		{			
			WeakReference<E> wr = refs.get(currentIteratorIndex);
			E e = wr == null ? null : wr.get();
			while (e == null && currentIteratorIndex < refs.size() - 1)
			{
				currentIteratorIndex++;
				wr = refs.get(currentIteratorIndex);
				e = wr == null ? null : wr.get();	
			}
			// now take it forward by 1
			currentIteratorIndex++;
			return e;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}
}
