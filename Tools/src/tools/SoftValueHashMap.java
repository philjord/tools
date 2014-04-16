package tools;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

public class SoftValueHashMap<K, V>
{
	private final ReferenceQueue<SoftReferenceKey<V>> queue = new ReferenceQueue<SoftReferenceKey<V>>();

	private LinkedHashMap<K, SoftReferenceKey<V>> map = new LinkedHashMap<K, SoftReferenceKey<V>>();

	public V get(K key)
	{
		expungeStaleEntries();
		SoftReferenceKey<V> ref = map.get(key);
		if (ref != null)
		{
			V v = ref.get();

			return v;
		}
		return null;

	}

	public V put(K key, V value)
	{
		expungeStaleEntries();
		V oldV = get(key);
		SoftReferenceKey<V> kv = new SoftReferenceKey<V>(key, value, queue);
		map.put(key, kv);
		return oldV;
	}

	public void clear()
	{
		map.clear();
	}

	public int size()
	{
		return map.size();
	}

	/**
	* Expunges stale entries from the table.
	*/
	@SuppressWarnings("unchecked")
	private void expungeStaleEntries()
	{
		SoftReferenceKey<V> ref;
		while ((ref = (SoftReferenceKey<V>) queue.poll()) != null)
		{
			map.remove(ref.key);
		}
	}

	private static class SoftReferenceKey<Z> extends SoftReference<Z>
	{
		public Object key;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public SoftReferenceKey(Object k, Z v, ReferenceQueue  queue)
		{
			super(v, queue);
			this.key = k;
		}
	}

}
