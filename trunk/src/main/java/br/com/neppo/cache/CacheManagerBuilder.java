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
package br.com.neppo.cache;

import com.esotericsoftware.kryo.Kryo;
import br.com.neppo.cache.inmemory.InMemoryCacheManager;
import br.com.neppo.cache.memcached.CacheWrapper;
import br.com.neppo.cache.memcached.MemcachedManager;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Builder for creating new instances of {@link br.com.neppo.cache.CacheManager}
 */
public class CacheManagerBuilder {

	private static final Logger log = Logger.getLogger(CacheManagerBuilder.class);

	private final CacheManager cacheManager;

	/**
	 * Builder for constructing CacheManagerBuilder objects
	 */
	public static class Builder {

		private static final int DEFAULT_ENTRY_TTL = 600;
		private static final int INITIAL_MAX_ENTRIES = 5000;
		private static final CacheType DEFAULT_CACHE_TYPE = CacheType.ONHEAP;
		private static final CacheTranscoder DEFAULT_TRANSCODER = CacheTranscoder.KRYO;

		private String memcachedHosts;
		private JAXBContext jaxbContext;
		private Kryo kryo;
		private int defaultTtl = DEFAULT_ENTRY_TTL;
		private CacheTranscoder cacheTranscoder = DEFAULT_TRANSCODER;
		private int maxEntries = INITIAL_MAX_ENTRIES;
		private CacheType cacheType = DEFAULT_CACHE_TYPE;

		/**
		 * Memcached host or list of hosts
		 *
		 * @param memcachedHosts
		 */
		public Builder setMemcachedHosts(String memcachedHosts) {
			this.memcachedHosts = memcachedHosts;
			return this;
		}

		/**
		 * Set JAXB context in case of using the JAXB transcoder
		 *
		 * @param jaxbContext
		 * @return
		 */
		public Builder setJaxbContext(JAXBContext jaxbContext) {
			this.jaxbContext = jaxbContext;
			return this;
		}

		/**
		 * Set the Kryo instance in case of using Kryo transcoder
		 *
		 * @param kryo
		 * @return
		 */
		public Builder setKryo(Kryo kryo) {
			this.kryo = kryo;
			return this;
		}

		/**
		 * The default time-to-live of the cached objects
		 *
		 * @param defaultTtl
		 * @return
		 */
		public Builder setDefaultTtl(int defaultTtl) {
			this.defaultTtl = defaultTtl;
			return this;
		}

		/**
		 * The transcoder used in this instance. In case all cached objects
		 * are implementing {@link java.io.Serializable} you don't need
		 * to use any transcoder
		 *
		 * @param cacheTranscoder
		 * @return
		 */
		public Builder setCacheTranscoder(CacheTranscoder cacheTranscoder) {
			this.cacheTranscoder = cacheTranscoder;
			return this;
		}

		/**
		 * Initial max entries for cache storaging (Not applicable for
		 * {@code br.com.neppo.cache.CacheType.MEMCACHED})
		 *
		 * @param maxEntries number of max entries
		 * @return
		 */
		public Builder setMaxEntries(int maxEntries) {
			this.maxEntries = maxEntries;
			return this;
		}

		/**
		 * Cache type
		 *
		 * @param cacheType
		 */
		public Builder setCacheType(CacheType cacheType) {
			this.cacheType = cacheType;
			return this;
		}

		/**
		 * Build a CacheManagerBuilder instance
		 *
		 * @return
		 */
		public CacheManagerBuilder build() {
			CacheManager manager;
			if (CacheType.MEMCACHED.equals(cacheType)) {
				final MemcachedManager mcache = new MemcachedManager();
				mcache.setCacheTranscoder(cacheTranscoder);
				mcache.setDefaultTtl(defaultTtl);
				mcache.setHosts(memcachedHosts);

				if (CacheTranscoder.JAXB.equals(cacheTranscoder)) {
					Class[] jaxbClasses = new Class[]{CacheWrapper.class};
					try {
						mcache.setContext(JAXBContext.newInstance(jaxbClasses));
					} catch (JAXBException e) {
						log.error("Error setting jaxbcontext", e);
					}
				} else if (CacheTranscoder.KRYO.equals(cacheTranscoder)) {
					mcache.setKryo(new Kryo());
				}
				manager = mcache;
			} else {
				InMemoryCacheManager icache = new InMemoryCacheManager();
				icache.setMaxEntries(maxEntries);
				icache.setTtl(defaultTtl);
				manager = icache;
			}
			return new CacheManagerBuilder(manager);
		}

		protected Builder() {

		}

	}

	private CacheManagerBuilder(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		return getCacheManager().getCache(name);
	}
}
