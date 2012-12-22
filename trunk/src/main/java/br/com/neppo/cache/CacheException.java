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
 * Cache Exception
 *
 * @author bhlangonijr
 */
public class CacheException extends Exception {

	private static final long serialVersionUID = -8919014424448869852L;

	public CacheException() {
	}

	/**
	 * @param description
	 */
	public CacheException(String description) {
		super(description);
	}

	/**
	 * @param throwable
	 */
	public CacheException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param description
	 * @param throwable
	 */
	public CacheException(String description, Throwable throwable) {
		super(description, throwable);
	}

}
