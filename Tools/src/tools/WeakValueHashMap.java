package tools;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Set;

public class WeakValueHashMap<K, V>
{
	private final ReferenceQueue<WeakReferenceKey<V>> queue = new ReferenceQueue<WeakReferenceKey<V>>();

	private LinkedHashMap<K, WeakReferenceKey<V>> map = new LinkedHashMap<K, WeakReferenceKey<V>>();

	public V get(K key)
	{
		expungeStaleEntries();
		WeakReferenceKey<V> ref = map.get(key);
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
		WeakReferenceKey<V> kv = new WeakReferenceKey<V>(key, value, queue);
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

	public Set<K> keySet()
	{
		return map.keySet();
	}

	/**
	* Expunges stale entries from the table.
	*/
	@SuppressWarnings("unchecked")
	private void expungeStaleEntries()
	{
		WeakReferenceKey<V> ref;
		while ((ref = (WeakReferenceKey<V>) queue.poll()) != null)
		{
			map.remove(ref.key);
		}
	}

	private static class WeakReferenceKey<Z> extends WeakReference<Z>
	{
		public Object key;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public WeakReferenceKey(Object k, Z v, ReferenceQueue queue)
		{
			super(v, queue);
			this.key = k;
		}
	}

}
