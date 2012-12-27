package com.googlecode.memcachefy.memcached;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Wrapper for caching generic objects
 *
 * @author bhlangonijr
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CacheWrapper<T> implements Serializable {

	private T object;

	public CacheWrapper() {

	}

	public CacheWrapper(final T object) {
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}


}
