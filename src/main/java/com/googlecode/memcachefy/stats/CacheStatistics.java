package com.googlecode.memcachefy.stats;

/**
 * Cache statistics holds some quantities that
 * are important to measure the cache efficiency
 *
 * @author bhlangonijr
 */
public interface CacheStatistics {

	/**
	 * Number of queries to the cache that successfuly retrieved an already stored value
	 * @return number of cache hits
	 */
	public long getCacheHits();

	/**
	 * Number of queries to the cache that didn't retrieve an already stored value
	 * @return
	 */
	public long getCacheMisses();

}
