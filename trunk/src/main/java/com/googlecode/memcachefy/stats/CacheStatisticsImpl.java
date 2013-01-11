package com.googlecode.memcachefy.stats;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache statisticss default implementation
 *
 * @author bhlangonijr
 */
public class CacheStatisticsImpl implements  CacheStatistics {

	private final AtomicLong cacheHits = new AtomicLong(0);
	private final AtomicLong cacheMisses = new AtomicLong(0);

	public long cacheHitsIncAndGet() {
		return cacheHits.incrementAndGet();
	}

	public long cacheMissesIncAndGet() {
		return cacheMisses.incrementAndGet();
	}

	@Override
	public long getCacheHits() {
		return cacheHits.get();
	}

	@Override
	public long getCacheMisses() {
		return cacheMisses.get();
	}
}
