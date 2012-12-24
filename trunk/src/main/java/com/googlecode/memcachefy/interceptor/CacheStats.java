/**
 *
 */
package com.googlecode.memcachefy.interceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache statistics
 *
 * @author bhlangonijr
 */
public class CacheStats {


	private final Map<String, CacheStatsInfo> cacheInfoMap =
			new ConcurrentHashMap<String, CacheStatsInfo>();

	/**
	 * CacheStatsInfo to keep track of all statistics for a given method/object
	 *
	 * @author bhlangonijr
	 */
	static class CacheStatsInfo {
		private final AtomicInteger totalCalls = new AtomicInteger(0);
		private final AtomicInteger cacheHits = new AtomicInteger(0);
		private AtomicLong firstTimestamp = new AtomicLong();
		private AtomicLong lastTimestamp = new AtomicLong();

		public CacheStatsInfo(long firstTimestamp) {
			super();
			this.firstTimestamp.set(firstTimestamp);
		}

		public AtomicInteger getTotalCalls() {
			return totalCalls;
		}

		public AtomicInteger getCacheHits() {
			return cacheHits;
		}

		public long getFirstTimestamp() {
			return firstTimestamp.get();
		}

		public void setFirstTimestamp(long firstTimestamp) {
			this.firstTimestamp.set(firstTimestamp);
		}

		public long getLastTimestamp() {
			return lastTimestamp.get();
		}

		public void setLastTimestamp(long lastTimestamp) {
			this.lastTimestamp.set(lastTimestamp);
		}

		@Override
		public String toString() {
			return "CacheStatsInfo [totalCalls=" + totalCalls.get() + ", cacheHits="
					+ cacheHits.get() + ", firstTimestamp=" + firstTimestamp.get()
					+ ", lastTimestamp=" + lastTimestamp.get() + "]\n";
		}


	}

	/**
	 * Get the cache info or create a new one if it doesn't exists
	 *
	 * @param object
	 * @return
	 */
	public CacheStatsInfo getCacheInfo(String object) {

		CacheStatsInfo info = cacheInfoMap.get(object);
		if (info == null) {
			info = createCacheStatsInfo(object);
		}

		return info;
	}

	/**
	 * Remove the cache info for a given object
	 *
	 * @param object
	 */
	public void removeCacheInfo(String object) {
		cacheInfoMap.remove(object);
	}

	/**
	 * Resets all statistics
	 */
	public void resetStats() {
		cacheInfoMap.clear();
	}

	private synchronized CacheStatsInfo createCacheStatsInfo(String object) {
		if (cacheInfoMap.containsKey(object)) {
			return cacheInfoMap.get(object);
		}
		cacheInfoMap.put(object, new CacheStatsInfo(System.currentTimeMillis()));
		return cacheInfoMap.get(object);

	}

	@Override
	public String toString() {
		return "CacheStats [cacheInfoMap=" + cacheInfoMap + "]";
	}

}
