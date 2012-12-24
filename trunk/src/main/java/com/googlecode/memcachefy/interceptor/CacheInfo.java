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
package com.googlecode.memcachefy.interceptor;

import com.googlecode.memcachefy.hashkey.HashKeyGeneratorStrategy;

/**
 * Cache information for cached objects
 *
 * @author bhlangonijr
 */
public class CacheInfo {

	private boolean enabled;
	private int ttl;
	private HashKeyGeneratorStrategy hashKeyGeneratorStrategy;
	private boolean enableMinCacheHitVerification;
	private double minCacheHitRatioRequired;
	private int minCacheHitRatioCounting;
	private boolean negativeCache;

	public CacheInfo(Cacheable cacheable) {
		this.enabled = cacheable.enabled();
		this.ttl = cacheable.ttl();
		this.hashKeyGeneratorStrategy = cacheable.hashKeyGeneratorStrategy();
		this.enableMinCacheHitVerification = cacheable.enableMinCacheHitVerification();
		this.minCacheHitRatioRequired = cacheable.minCacheHitRatioRequired();
		this.minCacheHitRatioCounting = cacheable.minCacheHitRatioCounting();
		this.negativeCache = cacheable.negativeCache();
	}

	public CacheInfo() {
		this.enabled = true;
		this.ttl = 600;
		this.hashKeyGeneratorStrategy = HashKeyGeneratorStrategy.REFLECTION;
		this.enableMinCacheHitVerification = false;
		this.minCacheHitRatioRequired = 0.20;
		this.minCacheHitRatioCounting = 50;
		this.negativeCache = false;
	}

	public CacheInfo(int ttl, HashKeyGeneratorStrategy hashKeyGeneratorStrategy) {
		this.enabled = true;
		this.ttl = ttl;
		this.hashKeyGeneratorStrategy = hashKeyGeneratorStrategy;
		this.enableMinCacheHitVerification = false;
		this.minCacheHitRatioRequired = 0.20;
		this.minCacheHitRatioCounting = 50;
		this.negativeCache = false;
	}

	public CacheInfo(int ttl) {
		this.enabled = true;
		this.ttl = ttl;
		this.hashKeyGeneratorStrategy = HashKeyGeneratorStrategy.REFLECTION;
		this.enableMinCacheHitVerification = false;
		this.minCacheHitRatioRequired = 0.20;
		this.minCacheHitRatioCounting = 50;
		this.negativeCache = false;
	}

	public CacheInfo(boolean enabled, int ttl,
					 HashKeyGeneratorStrategy hashKeyGeneratorStrategy,
					 boolean enableMinCacheHitVerification,
					 double minCacheHitRatioRequired, int minCacheHitRatioCounting,
					 boolean negativeCache) {
		this.enabled = enabled;
		this.ttl = ttl;
		this.hashKeyGeneratorStrategy = hashKeyGeneratorStrategy;
		this.enableMinCacheHitVerification = enableMinCacheHitVerification;
		this.minCacheHitRatioRequired = minCacheHitRatioRequired;
		this.minCacheHitRatioCounting = minCacheHitRatioCounting;
		this.negativeCache = negativeCache;
	}

	/**
	 * Whether cache info is enabled
	 *
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enable/disable cache info
	 *
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Time-to-live
	 *
	 * @return
	 */
	public int getTtl() {
		return ttl;
	}

	/**
	 * Set the time-to-live
	 *
	 * @param ttl
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	/**
	 * Hashkey generation strategy
	 *
	 * @return
	 */
	public HashKeyGeneratorStrategy getHashKeyGeneratorStrategy() {
		return hashKeyGeneratorStrategy;
	}

	/**
	 * Set the hashkey generation strategy
	 *
	 * @param hashKeyGeneratorStrategy
	 */
	public void setHashKeyGeneratorStrategy(
			HashKeyGeneratorStrategy hashKeyGeneratorStrategy) {
		this.hashKeyGeneratorStrategy = hashKeyGeneratorStrategy;
	}

	/**
	 * Whether min cache hit verification is enabled
	 *
	 * @return
	 */
	public boolean isEnableMinCacheHitVerification() {
		return enableMinCacheHitVerification;
	}

	/**
	 * Enable/disable min cache hit verification
	 *
	 * @param enableMinCacheHitVerification
	 */
	public void setEnableMinCacheHitVerification(
			boolean enableMinCacheHitVerification) {
		this.enableMinCacheHitVerification = enableMinCacheHitVerification;
	}

	/**
	 * Get ratio of min cache hit
	 *
	 * @return
	 */
	public double getMinCacheHitRatioRequired() {
		return minCacheHitRatioRequired;
	}

	/**
	 * Define ratio of min cache hit
	 *
	 * @param minCacheHitRatioRequired
	 */
	public void setMinCacheHitRatioRequired(double minCacheHitRatioRequired) {
		this.minCacheHitRatioRequired = minCacheHitRatioRequired;
	}

	/**
	 * Get min cache hit ratio counting
	 *
	 * @return
	 */
	public int getMinCacheHitRatioCounting() {
		return minCacheHitRatioCounting;
	}

	/**
	 * Set min cache hit ratio counting
	 *
	 * @param minCacheHitRatioCounting
	 */
	public void setMinCacheHitRatioCounting(int minCacheHitRatioCounting) {
		this.minCacheHitRatioCounting = minCacheHitRatioCounting;
	}

	/**
	 * Whether negative caching is enabled
	 *
	 * @return
	 */
	public boolean isNegativeCache() {
		return negativeCache;
	}

	/**
	 * Enable/disable negative caching
	 *
	 * @param negativeCache
	 */
	public void setNegativeCache(boolean negativeCache) {
		this.negativeCache = negativeCache;
	}

}
