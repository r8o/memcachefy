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

/**
 * A CacheManager provides and maintains the lifecycles of {@link Cache Cache} instances.
 *
 * @author bhlangonijr
 */
/*
This interface is based on the apache shiro (http://shiro.apache.org/) cache model
 */

public interface CacheManager {

	/**
	 * Acquires the cache with the specified <code>name</code>.  If a cache does not yet exist with that name, a new one
	 * will be created with that name and returned.
	 *
	 * @param name the name of the cache to acquire.
	 * @return the Cache with the given name
	 * @throws CacheException
	 *          if there is an error acquiring the Cache instance.
	 */
	public <K, V> Cache<K, V> getCache(String name) throws CacheException;

}

