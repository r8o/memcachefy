/*
 * Copyright 2012 neppo.com.br. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.memcachefy.inmemory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.memcachefy.Cache;
import com.googlecode.memcachefy.CacheException;
import com.googlecode.memcachefy.stats.CacheStatistics;
import com.googlecode.memcachefy.stats.CacheStatisticsImpl;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory (on heap) cache implementation
 *
 * @author bhlangonijr
 */
public class InMemoryCache<K, V> implements Cache<K, V> {
	private static final Logger log = Logger.getLogger(InMemoryCache.class);
	private final ConcurrentLinkedHashMap<K, V> map;
	private final ConcurrentLinkedHashMap<K, EntryInfo> expireMap;
	private final ExecutorService service = Executors.newSingleThreadExecutor();
	private final AtomicInteger executions = new AtomicInteger();
	private static final int PURGE_COUNT_THRESHOLD = 1000;
	private static final int PURGE_TIME_THRESHOLD = 5000;
	private static final int DEFAULT_MAX_ENTRIES = 5000;
	private static final int DEFAULT_TTL = 600;
	private int ttl = DEFAULT_TTL;
	private AtomicLong purgetLastTimestamp = new AtomicLong(System.currentTimeMillis());

	private final CacheStatisticsImpl cacheStatistics;

	public InMemoryCache(int maxEntries) {
		map = new ConcurrentLinkedHashMap.Builder().
				maximumWeightedCapacity(maxEntries).build();
		expireMap = new ConcurrentLinkedHashMap.Builder().
				maximumWeightedCapacity(maxEntries).build();
		cacheStatistics = new CacheStatisticsImpl();
	}

	public InMemoryCache() {
		this(DEFAULT_MAX_ENTRIES);
	}

	class EntryInfo {
		int ttl;
		long timestamp;
		K key;

		public EntryInfo(int ttl, long timestamp, K key) {
			super();
			this.ttl = ttl;
			this.timestamp = timestamp;
			this.key = key;
		}

		public EntryInfo() {

		}

	}

	@Override
	public V get(K key) throws CacheException {
		if (purgeExpiredEntry(key)) {
			//expired
			return null;
		}
		final V value = map.get(key);

		if (value == null) {
			cacheStatistics.cacheMissesIncAndGet();
		} else {
			cacheStatistics.cacheHitsIncAndGet();
		}

		return value;
	}

	@Override
	public V getAndTouch(K key, int ttl) throws CacheException {
		if (purgeExpiredEntry(key)) {
			//expired
			return null;
		}
		V e = map.get(key);
		EntryInfo info = expireMap.get(key);
		if (info != null) {
			info.timestamp = System.currentTimeMillis(); // touch
		}

		if (e == null) {
			cacheStatistics.cacheMissesIncAndGet();
		} else {
			cacheStatistics.cacheHitsIncAndGet();
		}

		return e;
	}

	@Override
	public V putAndGet(K key, V value) throws CacheException {
		return putAndGet(key, value, getTtl());
	}

	@Override
	public V putAndGet(K key, V value, int ttl) throws CacheException {
		purgeExpired();
		expireMap.put(key, new EntryInfo(ttl, System.currentTimeMillis(), key));
		return map.put(key, value);
	}

	@Override
	public void put(K key, V value) throws CacheException {
		purgeExpired();
		put(key, value, getTtl());
	}

	@Override
	public void put(K key, V value, int ttl) throws CacheException {
		expireMap.put(key, new EntryInfo(ttl, System.currentTimeMillis(), key));
		map.put(key, value);
	}

	@Override
	public V remove(K key) throws CacheException {
		expireMap.remove(key);
		return map.remove(key);
	}

	@Override
	public void clear() throws CacheException {
		expireMap.clear();
		map.clear();
	}

	@Override
	public int size() throws CacheException {
		return map.size();
	}

	@Override
	public CacheStatistics getCacheStatistics() {
		return cacheStatistics;
	}

	private void purgeExpired() {

		if (executions.incrementAndGet() % PURGE_COUNT_THRESHOLD == 0 &&
				System.currentTimeMillis() - purgetLastTimestamp.get() >= PURGE_TIME_THRESHOLD) {
			service.execute(new Runnable() {
				@Override
				public void run() {
					long init = System.currentTimeMillis();
					log.info("Purging entries...");
					int count = 0;
					for (EntryInfo entry : expireMap.values()) {
						if (purgeExpiredEntry(entry.key)) {
							count++;
						}
					}
					log.info("Purged " + count + " expired entries in " +
							((double) (System.currentTimeMillis() - init) / 1000.0) + " seconds.");
				}
			});
		}

	}

	private boolean purgeExpiredEntry(K key) {
		boolean result = false;
		EntryInfo entry = expireMap.get(key);
		if (entry != null &&
				System.currentTimeMillis() - entry.timestamp >= entry.ttl * 1000) {
			map.remove(entry.key);
			expireMap.remove(entry.key);
			result = true;
		}
		return result;
	}

	@Override
	public void close() {
		service.shutdownNow();
		map.clear();
		expireMap.clear();
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
}
