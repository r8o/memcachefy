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

import com.googlecode.memcachefy.Cache;
import com.googlecode.memcachefy.CacheException;
import com.googlecode.memcachefy.CacheManager;

/**
 * In-memory cache manager
 *
 * @author bhlangonijr
 */
public class InMemoryCacheManager implements CacheManager {
	private static final int DEFAULT_TTL = 600;
	private int maxEntries;
	private int ttl = DEFAULT_TTL;

	// no reason for transcoding, inmemory cache is on heap
	/* (non-Javadoc)
	 * @see com.googlecode.memcachefy.CacheManager#getCache(java.lang.String, com.googlecode.memcachefy.CacheTranscoder)
	 */
	@Override
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		return new InMemoryCache<K, V>(maxEntries);
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
}
