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

/**
 * Cache transcoding (serializing) method
 *
 * @author bhlangonijr
 */
public enum CacheTranscoder {
	/**
	 * Don't use serialization methods
	 */
	NONE,
	/**
	 * Kryo serialization
	 */
	KRYO,
	/**
	 * JAXB serialization
	 */
	JAXB
}
