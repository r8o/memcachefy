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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Cache Factory for easily creating new instances of {@link Cache}.
 * It will read the cache configuration in a property file named <tt>memcachefy.properties</tt>
 * in the classpath.
 *
 * @author bhlangonijr
 */
public class CacheFactory {
	private static final Logger log = Logger.getLogger(CacheFactory.class);
	private static final int DEFAULT_ENTRY_TTL = 600;
	private static final int INITIAL_MAX_ENTRIES = 5000;
	private static final CacheType DEFAULT_CACHE_TYPE = CacheType.ONHEAP;

	public static final String DEFAULT_PROPERTIES = "memcachefy.properties";
	public static final String DEFAULT_PROPERTIES_XML = "memcachefy-properties.xml";

	private static CacheManager cacheManager;

	private static Properties properties = new Properties();

	/**
	 * Retrieve a cache object from a {@link CacheManager} using the parameters
	 * present in a default configuration file {memcachefy.properties, memcachefy-properties.xml}.
	 *
	 * @param name Cache name
	 * @param <K>  Key type
	 * @param <V>  Value type
	 * @return A Cache object
	 * @throws Exception
	 */
	public static <K, V> Cache<K, V> getCache(String name) throws Exception {
		if (cacheManager == null) {
			loadDefaultProperties();
			cacheManager = getCacheManager(properties);
		}
		return cacheManager.getCache(name);
	}

	/**
	 *  Retrieve a cache object from a {@link CacheManager} using the cache parameters
	 *  from the properties file
	 * @param name Cache name
	 * @param propertiesFile Path + name of the properties file (*.properties, *.xml)
	 * @param <K>  Key type
	 * @param <V>  Value type
	 * @return A Cache object
	 * @throws Exception
	 */
	public static <K, V> Cache<K, V> getCache(String name, String propertiesFile) throws Exception {

		final Properties properties = new Properties();
		if (propertiesFile.toLowerCase().endsWith(".xml")) {
			properties.loadFromXML(new FileInputStream(propertiesFile));
		} else {
			properties.load(new FileInputStream(propertiesFile));
		}

		CacheManager cacheManager = getCacheManager(properties);

		return cacheManager.getCache(name);
	}

	private static synchronized void loadDefaultProperties() throws IOException {

		properties.clear();
		final String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		File fileProps = new File(path+"/"+DEFAULT_PROPERTIES);
		if (!fileProps.exists()) {
			fileProps = new File(path+"/"+DEFAULT_PROPERTIES_XML);
			properties.loadFromXML(new FileInputStream(fileProps));
		} else {
			properties.load(new FileInputStream(fileProps));
		}

		if (fileProps.exists()) {
			log.info("Found configuration file at: "+fileProps.getAbsolutePath());
		}


	}

	private static synchronized CacheManager getCacheManager(Properties properties) throws IOException {

		CacheManager manager;

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
			manager = mcache;
		} else {
			InMemoryCacheManager icache = new InMemoryCacheManager();
			icache.setMaxEntries(maxEntries);
			icache.setTtl(ttl);
			manager = icache;
		}

		return manager;

	}

	private CacheFactory() {

	}

}
