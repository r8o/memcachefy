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
package com.googlecode.memcachefy.memcached;

import com.googlecode.memcachefy.Cache;
import com.googlecode.memcachefy.CacheException;
import com.googlecode.memcachefy.CacheTranscoder;
import com.googlecode.memcachefy.stats.CacheStatistics;
import com.googlecode.memcachefy.stats.CacheStatisticsImpl;
import net.spy.memcached.CASMutation;
import net.spy.memcached.CASMutator;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Cache {@link com.googlecode.memcachefy.Cache} implementation that wraps an {@link net.spy.memcached.MemcachedClient} instance.
 *
 * @author bhlangonijr
 */
public class Memcached<K, V> implements Cache<K, V> {

	private static final Logger log = Logger.getLogger(Memcached.class);

	/**
	 * The wrapped memcached instance.
	 */
	private static final int ENTRY_TTL = 10 * 60;
	private MemcachedClient cache;
	private final String name;
	private int ttl;
	private ThreadLocal<? extends Transcoder<?>> threadLocalTranscoder;
	private final CacheTranscoder cacheTranscoder;
	private final CacheStatisticsImpl cacheStatistics;

	@SuppressWarnings("unchecked")
	private Transcoder<CacheWrapper<V>> getTranscoder() {
		return (Transcoder<CacheWrapper<V>>) threadLocalTranscoder.get();
	}

	/**
	 * Constructs a new Memcached instance with the given cache.
	 *
	 * @param name
	 * @param cache
	 * @param ttl
	 */
	public Memcached(String name, MemcachedClient cache, int ttl, CacheTranscoder cacheTranscoder) {
		if (cache == null) {
			throw new IllegalArgumentException("Cache argument cannot be null.");
		}
		this.name = name;
		this.cache = cache;
		this.ttl = ttl;
		this.cacheTranscoder = cacheTranscoder;
		this.cacheStatistics = new CacheStatisticsImpl();
	}

	/**
	 * Constructs a new Memcached instance with the given cache.
	 *
	 * @param name
	 * @param cache
	 */
	public Memcached(String name, MemcachedClient cache) {
		this(name, cache, ENTRY_TTL, CacheTranscoder.NONE);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#get(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V get(K key) throws CacheException {

		String userKey = getCacheKey(key);

		try {
			if (log.isDebugEnabled()) {
				log.debug("Getting object from cache [" +
						cache.getAvailableServers() + "] for key [" +
						userKey + "] using transconding: [" + cacheTranscoder + "]");
			}
			boolean transcoding = !CacheTranscoder.NONE.equals(cacheTranscoder);

			Future<?> f = transcoding ?
					cache.asyncGet(userKey, getTranscoder()) :
					cache.asyncGet(userKey);

			CacheWrapper<V> entry = (CacheWrapper<V>) f.get(10, TimeUnit.SECONDS);

			if (entry == null) {
				if (log.isDebugEnabled()) {
					log.debug("Entry for [" + userKey + "] is null.");
				}
				cacheStatistics.cacheMissesIncAndGet();
				return null;
			}
			cacheStatistics.cacheHitsIncAndGet();
			return entry.getObject();
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#getAndTouch(java.lang.Object, int)
	 */
	@Override
	public V getAndTouch(K key, int ttl) throws CacheException {

		String userKey = getCacheKey(key);

		try {
			if (log.isDebugEnabled()) {
				log.debug("Getting (and touching) object from cache [" +
						cache.getAvailableServers() + "] for key [" + userKey + "] with ttl [" + ttl + "]");
			}
			boolean transcoding = !CacheTranscoder.NONE.equals(cacheTranscoder);

			Future<?> f = transcoding ?
					cache.asyncGetAndTouch(userKey, ttl, getTranscoder()) :
					cache.asyncGetAndTouch(userKey, ttl);

			CacheWrapper<V> entry = (CacheWrapper<V>) f.get(10, TimeUnit.SECONDS);

			if (entry == null) {
				if (log.isDebugEnabled()) {
					log.debug("Entry for [" + userKey + "] is null.");
				}
				cacheStatistics.cacheMissesIncAndGet();
				return null;
			}
			cacheStatistics.cacheHitsIncAndGet();
			return entry.getObject();
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#putAndGet(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public V putAndGet(final K key, final V value, int ttl) throws CacheException {

		String userKey = getCacheKey(key);

		if (log.isDebugEnabled()) {
			log.debug("Putting object in cache [" +
					cache.getAvailableServers() + "] for key [" + userKey + "]");
		}

		try {
			CASMutator<CacheWrapper<V>> mutator;
			if (CacheTranscoder.NONE.equals(cacheTranscoder)) {
				mutator = new CASMutator<CacheWrapper<V>>(cache, null);
			} else {
				mutator = new CASMutator<CacheWrapper<V>>(cache, getTranscoder());
			}
			CacheWrapper<V> r = mutator.cas(userKey, new CacheWrapper<V>(value), getTtl(),
					new CASMutation<CacheWrapper<V>>() {
						@Override
						public CacheWrapper<V> getNewValue(final CacheWrapper<V> current) {
							return new CacheWrapper<V>(value);
						}
					});

			if (r == null) {
				if (log.isDebugEnabled()) {
					log.debug("Entry for [" + userKey + "] is null.");
				}
				return null;
			}
			return r.getObject();
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#putAndGet(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V putAndGet(K key, V value) throws CacheException {
		return putAndGet(key, value, getTtl());
	}


	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void put(K key, V value) throws CacheException {
		put(key, value, getTtl());
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#put(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public void put(K key, V value, int ttl) throws CacheException {

		String userKey = getCacheKey(key);

		if (log.isDebugEnabled()) {
			log.debug("Putting object in cache [" + cache.getAvailableServers() +
					"] for key [" + userKey + "] " + cacheTranscoder);
		}
		try {
			if (log.isDebugEnabled()) {
				log.debug("Caching Serializable object");
			}

			if (CacheTranscoder.NONE.equals(cacheTranscoder)) {
				cache.set(userKey, getTtl(), new CacheWrapper<V>(value));
			} else {
				cache.set(userKey, getTtl(), new CacheWrapper<V>(value), getTranscoder());
			}
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#remove(java.lang.Object)
	 */
	@Override
	public V remove(K key) throws CacheException {

		String userKey = getCacheKey(key);

		if (log.isDebugEnabled()) {
			log.debug("Removing object from cache [" +
					cache.getAvailableServers() + "] for key [" + userKey + "]");
		}
		try {
			V previous = get(key);
			cache.delete(userKey);
			return previous;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#clear()
	 */
	@Override
	public void clear() throws CacheException {
		if (log.isDebugEnabled()) {
			log.debug("Clearing all objects from cache [" + cache.getAvailableServers() + "]");
		}
		try {
			cache.flush();
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.Cache#size()
	 */
	@Override
	public int size() throws CacheException {
		try {
			int size = 0;
			Map<SocketAddress, Map<String, String>> map = cache.getStats();

			for (SocketAddress socketAddress : map.keySet()) {

				String totalItems = readMemcachedProperty("curr_items", socketAddress);
				if (totalItems != null) {
					size += Integer.parseInt(totalItems);
				}
			}

			return size;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@Override
	public CacheStatistics getCacheStatistics() {
		return cacheStatistics;
	}

	private String readMemcachedProperty(String name, SocketAddress socketAddress) {
		String value = null;
		Map<String, String> map = cache.getStats().get(socketAddress);

		value = map.get(name);

		return value;
	}

	private String getCacheKey(K key) {
		return name + (key instanceof String ? (String) key : Integer.toString(key.hashCode()));
	}

	public String getName() {
		return name;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	/**
	 * Returns &quot;Memcached [&quot; + cache.getName() + &quot;]&quot;
	 *
	 * @return &quot;Memcached [&quot; + cache.getName() + &quot;]&quot;
	 */
	public String toString() {
		return "Memcached [" + cache.getAvailableServers() + "]";
	}

	public ThreadLocal<? extends Transcoder<?>> getThreadLocalTranscoder() {
		return threadLocalTranscoder;
	}

	public void setThreadLocalTranscoder(ThreadLocal<? extends Transcoder<?>> threadLocalTranscoder) {
		this.threadLocalTranscoder = threadLocalTranscoder;
	}

	public CacheTranscoder getCacheTranscoder() {
		return cacheTranscoder;
	}

	@Override
	public void close() {
		cache.shutdown();
	}


}
