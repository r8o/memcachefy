package com.googlecode.memcachefy;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: bhlangonijr
 * Date: 12/21/12
 * Time: 3:53 PM
 */
public class CacheFactoryTest {

	@Test
	public void testMemcachedFactory() throws CacheException {

		Cache<String, String> myCache = CacheFactory.getCache("myCache");

		myCache.put("myKey", "myValue");

		System.out.println(myCache.get("myKey"));

		assertTrue(myCache.get("myKey").equals("myValue"));

	}

}
