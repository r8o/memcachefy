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
package com.googlecode.memcachefy;


/**
 * A Cache efficiently stores temporary objects primarily to improve an application's performance.
 *
 * @author bhlangonijr
 */

/*
This interface is based on the apache shiro (http://shiro.apache.org/) cache model
 */
public interface Cache<K, V> {

	/**
	 * Returns the Cached value stored under the specified {@code key} or
	 * {@code null} if there is no Cache entry for that {@code key}.
	 *
	 * @param key the key that the value was previous added with
	 * @return the cached object or {@code null} if there is no entry for the specified {@code key}
	 * @throws com.googlecode.memcachefy.CacheException
	 *          if there is a problem accessing the underlying cache system
	 */
	public V get(K key) throws CacheException;

	/**
	 * Returns the Cached value stored under the specified {@code key} or
	 * {@code null} if there is no Cache entry for that {@code key}.
	 *
	 * @param key the key that the value was previous added with
	 * @param ttl time to live
	 * @return the cached object or {@code null} if there is no entry for the specified {@code key}
	 * @throws com.googlecode.memcachefy.CacheException
	 *
	 */
	public V getAndTouch(K key, int ttl) throws CacheException;

	/**
	 * Adds a Cache entry and return the previous value.
	 *
	 * @param key   the key used to identify the object being stored.
	 * @param value the value to be stored in the cache.
	 * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
	 * @throws com.googlecode.memcachefy.CacheException
	 *          if there is a problem accessing the underlying cache system
	 */
	public V putAndGet(K key, V value) throws CacheException;

	/**
	 * Adds a Cache entry and return the previous value.
	 *
	 * @param key   the key used to identify the object being stored.
	 * @param value the value to be stored in the cache.
	 * @param ttl   time-to-live for the entry
	 * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
	 * @throws com.googlecode.memcachefy.CacheException
	 *          if there is a problem accessing the underlying cache system
	 */
	public V putAndGet(K key, V value, int ttl) throws CacheException;

	/**
	 * Adds a Cache entry.
	 *
	 * @param key   the key used to identify the object being stored.
	 * @param value the value to be stored in the cache.
	 * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
	 * @throws com.googlecode.memcachefy.CacheException
	 *          if there is a problem accessing the underlying cache system
	 */
	public void put(K key, V value) throws CacheException;

	/**
	 * Adds a Cache entry.
	 *
	 * @param key   the key used to identify the object being stored.
	 * @param value the value to be stored in the cache.
	 * @param ttl   time-to-live for the entry
	 * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
	 * @throws com.googlecode.memcachefy.CacheException
	 *          if there is a problem accessing the underlying cache system
	 */
	public void put(K key, V value, int ttl) throws CacheException;

	/**
	 * Remove the cache entry corresponding to the specified key.
	 *
	 * @param key the key of the entry to be removed.
	 * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
	 * @throws com.googlecode.memcachefy.CacheException
	 *          if there is a problem accessing the underlying cache system
	 */
	public V remove(K key) throws CacheException;

	/**
	 * Clear all entries from the cache.
	 *
	 * @throws com.googlecode.memcachefy.CacheException
	 *          if there is a problem accessing the underlying cache system
	 */
	public void clear() throws CacheException;

	/**
	 * Returns the number of entries in the cache.
	 *
	 * @return the number of entries in the cache.
	 * @throws com.googlecode.memcachefy.CacheException
	 *
	 */
	public int size() throws CacheException;

	/**
	 * Close and release de resources for current cache instance
	 */
	public void close();


}
