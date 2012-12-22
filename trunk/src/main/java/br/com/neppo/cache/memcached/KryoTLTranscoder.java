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

/**
 * A thread-local version of {@link KryoTLTranscoder}
 * @param <T>
 */
public class KryoTLTranscoder<T> extends ThreadLocal<KryoTranscoder<T>> {

	private Kryo kryo;

	public KryoTLTranscoder(Kryo kryo) {
		super();
		this.kryo = kryo;
	}

	@Override
	protected KryoTranscoder<T> initialValue() {
		try {
			return new KryoTranscoder<T>(kryo);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Kryo transcoder", e);
		}
	}

}