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

import com.esotericsoftware.kryo.Kryo;
import com.googlecode.memcachefy.inmemory.InMemoryCacheManager;
import com.googlecode.memcachefy.memcached.CacheWrapper;
import com.googlecode.memcachefy.memcached.MemcachedManager;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.Properties;


/**
 * Cache Factory for easily creating new instances of {@link Cache}.
 * It will read the cache configuration in a property file named <tt>cache-manager.properties</tt>
 * in the classpath.
 *
 * @author bhlangonijr
 */
public class CacheFactory {
	private static final Logger log = Logger.getLogger(CacheFactory.class);
	private static final int DEFAULT_ENTRY_TTL = 600;
	private static final int INITIAL_MAX_ENTRIES = 5000;
	private static final CacheType DEFAULT_CACHE_TYPE = CacheType.ONHEAP;
	private static final String propertiesFile = "cache-manager.properties";
	private static CacheManager cacheManager;

	private static Properties properties;

	public static <K, V> Cache<K, V> getCache(String name) {
		if (cacheManager == null) {
			initialize();
		}
		Cache<K, V> c = null;
		try {
			c = cacheManager.getCache(name);
		} catch (CacheException e) {
			log.error("Couldn't create a new cache instance: ", e);
		}
		return c;
	}

	private static synchronized void initialize() {
		if (properties == null) {
			try {
				Thread thread = Thread.currentThread();
				ClassLoader classLoader = thread.getContextClassLoader();
				properties = new Properties();
				properties.load(classLoader.getResourceAsStream(propertiesFile));
			} catch (Exception e) {
				log.error("Error while loading " + propertiesFile + " file.", e);
			}
		}
		int ttl = DEFAULT_ENTRY_TTL;
		try {
			ttl = Integer.parseInt(properties.getProperty("cache.entry.ttl", "600"));
		} catch (Exception e) {
			log.error("Error reading ttl", e);
		}

		int maxEntries = INITIAL_MAX_ENTRIES;
		try {
			maxEntries = Integer.parseInt(properties.getProperty("cache.initialMaxEntries", "5000"));
		} catch (Exception e) {
			log.error("Error reading maxEntries", e);
		}

		CacheType type = DEFAULT_CACHE_TYPE;
		try {
			type = CacheType.valueOf(properties.getProperty("cache.type", "ONHEAP"));
		} catch (Exception e) {
			log.error("Error reading cache type", e);
		}

		CacheTranscoder transcoder = CacheType.MEMCACHED.equals(type) ?
				CacheTranscoder.KRYO : CacheTranscoder.NONE;
		try {
			transcoder = CacheTranscoder.valueOf(properties.getProperty("memcached.transcoder", "KRYO"));
		} catch (Exception e) {
			log.error("Error reading transcoder", e);
		}
		if (CacheType.MEMCACHED.equals(type)) {
			final MemcachedManager mcache = new MemcachedManager();
			mcache.setCacheTranscoder(transcoder);
			mcache.setDefaultTtl(ttl);
			mcache.setHosts(properties.getProperty("memcached.hosts", ""));

			if (CacheTranscoder.JAXB.equals(transcoder)) {
				Class[] jaxbClasses = new Class[]{CacheWrapper.class};
				try {
					mcache.setContext(JAXBContext.newInstance(jaxbClasses));
				} catch (JAXBException e) {
					log.error("Error setting jaxbcontext", e);
				}
			} else if (CacheTranscoder.KRYO.equals(transcoder)) {
				mcache.setKryo(new Kryo());
			}
			cacheManager = mcache;
		} else {
			InMemoryCacheManager icache = new InMemoryCacheManager();
			icache.setMaxEntries(maxEntries);
			icache.setTtl(ttl);
			cacheManager = icache;
		}

	}

	private CacheFactory() {

	}

}
