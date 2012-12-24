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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * A Thread-local version of {@link JAXBTLTranscoder}
 * @param <T>
 */
public class JAXBTLTranscoder<T> extends ThreadLocal<JAXBTranscoder<T>> {

	private JAXBContext jaxbContext;

	public JAXBTLTranscoder(JAXBContext jaxbContext) {
		super();
		this.jaxbContext = jaxbContext;
	}

	@Override
	protected JAXBTranscoder<T> initialValue() {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return new JAXBTranscoder<T>(marshaller, unmarshaller);
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to create JAXB (un)marshaller", e);
		}
	}

}