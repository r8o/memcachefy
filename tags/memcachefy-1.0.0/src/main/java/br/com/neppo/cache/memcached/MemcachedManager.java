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
package br.com.neppo.cache.memcached;

import com.esotericsoftware.kryo.Kryo;
import br.com.neppo.cache.Cache;
import br.com.neppo.cache.CacheException;
import br.com.neppo.cache.CacheManager;
import br.com.neppo.cache.CacheTranscoder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.spring.MemcachedClientFactoryBean;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache Manager {@code CacheManager} implementation utilizing the Memcached framework for all cache functionality.
 * <p/>
 */
public class MemcachedManager implements CacheManager {

	private static final Logger log = Logger.getLogger(MemcachedManager.class);
	@SuppressWarnings("rawtypes")
	private final Map<String, Cache> cacheMap =
			new ConcurrentHashMap<String, Cache>();

	private MemcachedClientFactoryBean factory;

	private String hosts;
	private JAXBContext context;
	private Kryo kryo;
	private int defaultTtl;
	private CacheTranscoder cacheTranscoder;

	/**
	 * Default no argument constructor
	 */
	public MemcachedManager() {
		factory = new MemcachedClientFactoryBean();
		factory.setFailureMode(FailureMode.Redistribute);
		factory.setProtocol(Protocol.BINARY);
		factory.setOpTimeout(1000);
		factory.setHashAlg(DefaultHashAlgorithm.KETAMA_HASH);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		if (log.isDebugEnabled()) {
			log.debug("Acquiring Memcached instance named [" + name + "]");
		}

		if (cacheMap.containsKey(name)) {
			return cacheMap.get(name);
		}

		try {

			final MemcachedClient client = (MemcachedClient) factory.getObject();
			Cache<K, V> cache = new Memcached<K, V>(name, client, getDefaultTtl(), getCacheTranscoder());
			if (CacheTranscoder.JAXB.equals(getCacheTranscoder())) {
				final JAXBTLTranscoder<CacheWrapper<V>> transcoder =
						new JAXBTLTranscoder<CacheWrapper<V>>(getContext());
				((Memcached<K, V>) cache).setThreadLocalTranscoder(transcoder);
			} else if (CacheTranscoder.KRYO.equals(getCacheTranscoder())) {
				final KryoTLTranscoder<CacheWrapper<V>> transcoder =
						new KryoTLTranscoder<CacheWrapper<V>>(getKryo());
				((Memcached<K, V>) cache).setThreadLocalTranscoder(transcoder);
			}

			cacheMap.put(name, cache);

			return cache;
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public final void init() throws CacheException {

	}

	@SuppressWarnings("rawtypes")
	public void destroy() {
		for (Cache cache : cacheMap.values()) {
			try {
				cache.close();
			} catch (Exception e) {
				log.error("Error while trying to close cache instance: ", e);
			}
		}
	}

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
		factory.setServers(getHosts());

	}

	public JAXBContext getContext() {
		return context;
	}

	public void setContext(JAXBContext context) {
		this.context = context;
	}

	public int getDefaultTtl() {
		return defaultTtl;
	}

	public void setDefaultTtl(int defaultTtl) {
		this.defaultTtl = defaultTtl;
	}

	public CacheTranscoder getCacheTranscoder() {
		return cacheTranscoder;
	}

	public void setCacheTranscoder(CacheTranscoder cacheTranscoder) {
		this.cacheTranscoder = cacheTranscoder;
	}

	public Kryo getKryo() {
		return kryo;
	}

	public void setKryo(Kryo kryo) {
		this.kryo = kryo;
	}
}
