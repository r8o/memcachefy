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

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * JAXBSerializer is used for serializing java non-serializable objects
 * using XML
 *
 * @param <T>
 * @author bhlangonijr
 */
public class JAXBSerializer<T> {

	private static final Logger log = Logger.getLogger(JAXBSerializer.class);

	private static final int COMPRESSION_TRESHOLD = 1024; // 1 kB

	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	public JAXBSerializer(Marshaller marshaller, Unmarshaller unmarshaller) {
		this.marshaller = marshaller;
		this.unmarshaller = unmarshaller;
	}

	public T decodeObject(byte[] data) {
		if (data != null && data.length > 0) {
			final byte[] unzippedData;
			if (!isInGZIPFormat(new ByteArrayInputStream(data))) {
				unzippedData = data;
			} else {
				unzippedData = unzip(data);
			}
			return decodeBytes(unzippedData);
		}
		return null;
	}

	public byte[] encodeObject(T object) {
		byte[] data = encodeBytes(object);
		if (data == null || data.length <= 0) {
			return null;
		} else {
			boolean compress = shouldCompress(data);
			if (compress) {
				data = zip(data);
			}
			return data;
		}
	}

	public boolean shouldCompress(byte[] data) {
		return data.length >= COMPRESSION_TRESHOLD;
	}

	@SuppressWarnings("unchecked")
	public T decodeBytes(byte[] input) {
		try {
			if (input != null && input.length > 0) {
				return (T) unmarshaller.unmarshal(new ByteArrayInputStream(input));
			}
		} catch (JAXBException e) {
			log.error("Failed to unmarshal data", e);
		}
		return null;
	}

	public byte[] encodeBytes(T object) {
		byte[] data = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			marshaller.marshal(object, bos);
			data = bos.toByteArray();
		} catch (JAXBException e) {
			log.error("Failed to marshal object", e);
		}
		return data;
	}

	public static byte[] unzip(byte[] data) {
		ByteArrayOutputStream bos = null;
		if (data != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			bos = new ByteArrayOutputStream();
			GZIPInputStream gis = null;
			try {
				gis = new GZIPInputStream(bis);
				byte[] buf = new byte[8192];
				int r = -1;
				while ((r = gis.read(buf)) > 0) {
					bos.write(buf, 0, r);
				}
			} catch (IOException e) {
				log.error("Failed to decompress data", e);
			} finally {
				close(gis);
				close(bos);
			}
		}
		if (bos == null) {
			return null;
		}
		return bos.toByteArray();
	}

	public static byte[] zip(byte[] data) {
		ByteArrayOutputStream bos = null;
		if (data != null && data.length > 0) {
			bos = new ByteArrayOutputStream();
			GZIPOutputStream gos = null;
			try {
				gos = new GZIPOutputStream(bos);
				gos.write(data);
			} catch (IOException e) {
				log.error("Failed to zip data", e);
				bos = null;
			} finally {
				close(gos);
				close(bos);
			}
		} else {
			log.error("No data to zip");
		}
		if (bos == null) {
			return null;
		}
		return bos.toByteArray();
	}

	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.warn(String.format("Unable to close %s", closeable), e);
			}
		}
	}

	public static boolean isInGZIPFormat(final ByteArrayInputStream data) {
		try {
			CheckedInputStream in = new CheckedInputStream(data, new CRC32());
			int b1 = in.read();
			int b2 = in.read();
			if (b1 == -1 || b2 == -1) {
				return false;
			}
			return ((b2 << 8) | b1) == GZIPInputStream.GZIP_MAGIC;
		} catch (IOException e) {
			log.error("Failed to read from a ByteArrayInputStream", e);
		}
		return false;
	}

}
