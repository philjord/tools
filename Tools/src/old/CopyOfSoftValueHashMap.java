package old;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

public class CopyOfSoftValueHashMap<K, V>
{
	private final ReferenceQueue<KandV<K, V>> queue = new ReferenceQueue<KandV<K, V>>();

	private LinkedHashMap<K, SoftReference<KandV<K, V>>> map = new LinkedHashMap<K, SoftReference<KandV<K, V>>>();

	public V get(K key)
	{
		expungeStaleEntries();
		SoftReference<KandV<K, V>> ref = map.get(key);
		if (ref != null)
		{
			KandV<K, V> kv = ref.get();
			if (kv != null)
			{
				return kv.v;
			}
		}
		return null;

	}

	public V put(K key, V value)
	{
		expungeStaleEntries();
		V oldV = get(key);
		KandV<K, V> kv = new KandV<K, V>(key, value);
		SoftReference<KandV<K, V>> ref = new SoftReference<KandV<K, V>>(kv, queue);
		map.put(key, ref);
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
	private void expungeStaleEntries()
	{
		Reference<? extends KandV<K, V>> ref;
		while ((ref = queue.poll()) != null)
		{
			map.remove(ref.get().k);
		}
	}

	private static class KandV<K, V>
	{
		public K k;

		public V v;

		public KandV(K k, V v)
		{
			this.k = k;
			this.v = v;
		}
	}

}
