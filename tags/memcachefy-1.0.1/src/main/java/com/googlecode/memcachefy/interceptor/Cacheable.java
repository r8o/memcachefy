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

import java.lang.annotation.*;

/**
 * Indicates a cached method
 *
 * @author bhlangonijr
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Cacheable {
	/**
	 * Enable/disable the cache
	 *
	 * @return if the cache is enabled
	 */
	boolean enabled() default true;

	/**
	 * Define the time-to-live of the given entry in the cache
	 *
	 * @return time to live in milliseconds
	 */
	int ttl() default 600;

	/**
	 * Hashkey generation strategy
	 *
	 * @return list of parameters and fields
	 */
	HashKeyGeneratorStrategy hashKeyGeneratorStrategy()
			default HashKeyGeneratorStrategy.REFLECTION;

	/**
	 * Enable the mechanism that disable the access to
	 * the caching mechanism, if the cache hits is too low
	 * given what was configured for that specific object
	 *
	 * @return true if min cache hit verification is enabled
	 */
	boolean enableMinCacheHitVerification() default false;

	/**
	 * Required ratio of cache hits on a given object
	 * to keep caching it. If the minimal number is not
	 * reached the caching mechanism is automaticaly
	 * disabled
	 *
	 * @return percentage of cache hits required
	 */
	double minCacheHitRatioRequired() default 0.20;

	/**
	 * Minimal number of cache requests to trigger
	 * minCacheHitRatioRequired verification
	 * on the type of object being cached
	 *
	 * @return min number of requests to cache
	 */
	int minCacheHitRatioCounting() default 50;

	/**
	 * Enable/disable negative caching
	 *
	 * @return
	 */
	boolean negativeCache() default false;

}
