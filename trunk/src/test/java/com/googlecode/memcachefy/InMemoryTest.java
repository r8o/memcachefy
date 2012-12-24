package com.googlecode.memcachefy;

import com.googlecode.memcachefy.inmemory.InMemoryCache;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: bhlangonijr
 * Date: 12/19/12
 * Time: 4:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryTest {


	@Test
	public void test() throws CacheException {


		InMemoryCache<String, String> cache = new InMemoryCache<String, String>(500);


		cache.put("aa", "bb", 10);

		System.out.println("Value: " + cache.get("aa"));


	}


}
