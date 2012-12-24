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
package br.com.neppo.cache.interceptor;

import br.com.neppo.cache.Cache;
import br.com.neppo.cache.CacheFactory;
import br.com.neppo.cache.hashkey.HashKeyGenerator;
import br.com.neppo.cache.hashkey.HashKeyGeneratorStrategy;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *  The intercetor is intended to be used by extending it with concrete implementations of
 *  dynamic proxies (reflection API) or EJB3 interceptors. Once we have a method intercepted
 *  {@code CacheInterceptor} will change its default behaviour by caching objects returned
 *  by it. A hash key based on the parameters of the calling method will be used
 *  as the key of the cache entry.
 *
 * @author bhlangonijr
 */
public abstract class CacheInterceptor {

	private static final Logger log = Logger.getLogger(CacheInterceptor.class);
	private static final int DISABLE_CACHE_ACCUMULATED_ERROR_THRESHOLD = 15;
	private static final int RE_ENABLE_CACHE_AFTER_ATTEMPTS = 100;
	private static final int STATS_PRINTING_THRESHOLD = 500;

	private Cache<String, Object> cache;
	private CacheStats stats = new CacheStats();

	static class EmptyData implements Serializable {
		private static final long serialVersionUID = -2932485306129411735L;
	}

	private AtomicInteger totalCalls = new AtomicInteger(0);
	private AtomicInteger accumulatedErrors = new AtomicInteger(0);
	private AtomicInteger cacheDisabledCount = new AtomicInteger(0);

	private ExecutorService service = Executors.newSingleThreadExecutor();

	public CacheInterceptor() {
		this.cache = CacheFactory.getCache("DefaultCache");
	}

	public CacheInterceptor(Cache<String, Object> cache) {
		this.cache = cache;
	}

	/**
	 *
	 * @param source Source class
	 * @param method Intercepted method
	 * @param parameters Method's parameters
	 * @return
	 * @throws Exception
	 */
	protected Object intercept(Object source, Method method, Object[] parameters) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Intercepting method: " + method.getName() +
					" - Annotations: " + Arrays.toString(method.getAnnotations()));
			Method sourceMethod = source.getClass().getMethod(method.getName(), method.getParameterTypes());
			log.debug("Source method: " + sourceMethod.getName() +
					" Annot:  " + Arrays.toString(sourceMethod.getAnnotations()));
		}

		CacheInfo info = null;
		if (method.isAnnotationPresent(Cacheable.class)) {
			Cacheable cacheable = method.getAnnotation(Cacheable.class);
			info = new CacheInfo(cacheable);
		}

		return intercept(info, source, method, parameters);
	}

	/**
	 *
	 * @param info Caching information
	 * @param source Source class
	 * @param method Intercepted method
	 * @param parameters Method's parameters
	 * @return
	 * @throws Exception
	 */
	protected Object intercept(CacheInfo info, Object source, Method method, Object[] parameters) throws Exception {
		try {
			if (info != null) {
				checkStats();
				if (method.getReturnType().equals(Void.TYPE)) {
					log.warn("Cannot cache methods with return type VOID!");
				} else if (getCache() == null || getCacheDisabledCount().get() > 0) {
					if (getCacheDisabledCount().getAndIncrement() > RE_ENABLE_CACHE_AFTER_ATTEMPTS) {
						getCacheDisabledCount().set(0);
					}
					log.warn("Cache is disabled! Check the cache servers and configurations.");
				} else if (info.isEnabled()) {
					boolean skipCache = false;
					if (info.isEnableMinCacheHitVerification()) {
						final int cacheHits = getStats().getCacheInfo(method.getName()).getCacheHits().get();
						final int cacheRequests = getStats().getCacheInfo(method.getName()).getTotalCalls().get();
						if (cacheRequests > info.getMinCacheHitRatioCounting() &&
								(double) cacheHits / (double) cacheRequests < info.getMinCacheHitRatioRequired()) {
							skipCache = true;
						}
					}
					if (!skipCache) {
						getStats().getCacheInfo(method.getName()).
								setLastTimestamp(System.currentTimeMillis());
						getStats().getCacheInfo(method.getName()).
								getTotalCalls().incrementAndGet();

						int ttl = info.getTtl();

						HashKeyGeneratorStrategy st = info.getHashKeyGeneratorStrategy();

						int hashCode = (st.equals(HashKeyGeneratorStrategy.REFLECTION) ?
								HashKeyGenerator.reflectionHashKey(method, parameters) :
								HashKeyGenerator.defaultHashKey(method, parameters));

						String key = Integer.toString(hashCode);

						Object entry = getCache().get(key);

						if (log.isDebugEnabled()) {
							log.debug("Processing method [" + method.getName() + "]=" +
									parameters);
						}

						if (entry != null) {
							getStats().getCacheInfo(method.getName()).getCacheHits().incrementAndGet();
							if (log.isDebugEnabled()) {
								log.debug("Found cache entry [" + key + "]=" + entry.toString());
							}
							if (entry instanceof EmptyData) {
								entry = null;
							}
							getAccumulatedErrors().set(0);
						} else {
							if (log.isDebugEnabled()) {
								log.debug("Not found cache entry [" + key + "]");
							}

							entry = proceed(source, method, parameters);

							if (entry != null) {
								if (log.isDebugEnabled()) {
									log.debug("Adding cache entry [" + key + "]=" + entry.toString());
								}
								getCache().put(key, entry, ttl);
							} else if (info.isNegativeCache()) {
								if (log.isDebugEnabled()) {
									log.debug("Adding negative cache entry [" + key + "]=null");
								}
								getCache().put(key, new EmptyData(), ttl);
							}
							getAccumulatedErrors().set(0);

						}
						return entry;
					}
				}
			}
		} catch (Throwable e) {
			if (getAccumulatedErrors().getAndIncrement() >= DISABLE_CACHE_ACCUMULATED_ERROR_THRESHOLD) {
				getCacheDisabledCount().incrementAndGet();
				getAccumulatedErrors().set(0);
			}
			log.error("Error while intercepting method: ", e);
		}

		return proceed(source, method, parameters);
	}

	/**
	 * Dynamic proxies and EJB3 interceptors proceed/invoke wrapper method
	 *
	 * @param obj
	 * @param args
	 * @return Result of the method execution executed through reflection
	 */
	protected abstract Object proceed(Object obj, Method method, Object[] args) throws Exception;

	/**
	 * @return the cache
	 */
	public Cache<String, Object> getCache() {
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(Cache<String, Object> cache) {
		this.cache = cache;
	}

	/**
	 * @return the stats
	 */
	public CacheStats getStats() {
		return stats;
	}

	/**
	 * @param stats the stats to set
	 */
	public void setStats(CacheStats stats) {
		this.stats = stats;
	}

	/**
	 * @return the totalCalls
	 */
	public AtomicInteger getTotalCalls() {
		return totalCalls;
	}

	/**
	 * @param totalCalls the totalCalls to set
	 */
	public void setTotalCalls(AtomicInteger totalCalls) {
		this.totalCalls = totalCalls;
	}

	/**
	 * @return the accumulatedErrors
	 */
	public AtomicInteger getAccumulatedErrors() {
		return accumulatedErrors;
	}

	/**
	 * @param accumulatedErrors the accumulatedErrors to set
	 */
	public void setAccumulatedErrors(AtomicInteger accumulatedErrors) {
		this.accumulatedErrors = accumulatedErrors;
	}

	/**
	 * @return the cacheDisabledCount
	 */
	public AtomicInteger getCacheDisabledCount() {
		return cacheDisabledCount;
	}

	/**
	 * @param cacheDisabledCount the cacheDisabledCount to set
	 */
	public void setCacheDisabledCount(AtomicInteger cacheDisabledCount) {
		this.cacheDisabledCount = cacheDisabledCount;
	}

	/**
	 * @return the service
	 */
	public ExecutorService getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(ExecutorService service) {
		this.service = service;
	}

	/*
	 * Check & print cache stats
	 */
	private void checkStats() {
		if (totalCalls.getAndIncrement() % STATS_PRINTING_THRESHOLD == 0) {
			service.execute(new Runnable() {
				@Override
				public void run() {
					log.info("Cache Statistics - Begin ------------------------------");
					log.info("Total Cache Calls:         " + totalCalls.get());
					log.info("Accumulated Cache Erros:   " + accumulatedErrors.get());
					log.info("Calls with Cache Disabled: " + cacheDisabledCount.get());
					log.info("Cached objects: " + stats);
					log.info("Cache Statistics - End --------------------------------");
				}

			});
		}
	}
}
