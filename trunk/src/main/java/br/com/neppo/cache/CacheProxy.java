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

import br.com.neppo.cache.interceptor.CacheInfo;
import br.com.neppo.cache.interceptor.CacheInterceptor;
import br.com.neppo.cache.interceptor.Cacheable;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * A dynamic proxy for intercepting cached method calls
 *
 * @author bhlangonijr
 */
public class CacheProxy extends CacheInterceptor implements InvocationHandler {
	private static final Logger log = Logger.getLogger(CacheProxy.class);

	private Object obj;
	private Map<String, CacheInfo> cacheInfo;

	/**
	 * Create a new instance of the proxied class
	 *
	 * @param obj       The proxied object
	 * @param cacheInfo Cache information of the methods
	 * @param cache     A cache instance
	 * @return
	 */
	public static Object newInstance(Object obj, Map<String, CacheInfo> cacheInfo, Cache<String, Object> cache) {
		return java.lang.reflect.Proxy.newProxyInstance(
				obj.getClass().getClassLoader(),
				obj.getClass().getInterfaces(),
				new CacheProxy(obj, cacheInfo, cache));
	}

	/**
	 * Create a new instance of the proxied class
	 *
	 * @param obj
	 * @return
	 */
	public static Object newInstance(Object obj) {
		return newInstance(obj, null, null);
	}

	/**
	 * Create a new instance for the proxied class
	 *
	 * @param obj
	 * @param cache
	 * @return
	 */
	public static Object newInstance(Object obj, Cache<String, Object> cache) {
		return newInstance(obj, null, cache);
	}

	public static Object newInstance(Object obj, Map<String, CacheInfo> cacheInfo) {
		return newInstance(obj, cacheInfo, null);
	}

	private CacheProxy(Object obj, Map<String, CacheInfo> cacheInfo, Cache<String, Object> cache) {
		this.obj = obj;
		this.cacheInfo = cacheInfo;
		if (cache != null) {
			this.setCache(cache);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object object, Method method, Object[] args)
			throws Throwable {
		Object result;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Proxying method: " + method.getName() + ": " + Arrays.toString(args));
			}
			CacheInfo info = null;
			if (cacheInfo != null) {
				info = cacheInfo.get(method.getName());
			}
			if (info == null) {
				Method objMethod = obj.getClass().getMethod(method.getName(), method.getParameterTypes());
				if (objMethod.isAnnotationPresent(Cacheable.class)) {
					Cacheable cacheable = objMethod.getAnnotation(Cacheable.class);
					info = new CacheInfo(cacheable);
				}
			}
			result = this.intercept(info, object, method, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (Exception e) {
			throw new RuntimeException("unexpected invocation exception: ", e);
		}
		return result;
	}

	@Override
	protected Object proceed(Object object, Method method, Object[] args) throws Exception {
		return method.invoke(obj, args);
	}

}
