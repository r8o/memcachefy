/**
 *
 */
package com.googlecode.memcachefy;

import com.googlecode.memcachefy.interceptor.CacheInfo;
import com.googlecode.memcachefy.interceptor.Cacheable;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;

import static org.junit.Assert.assertNotNull;

/**
 * @author bhlangonijr
 */
public class PojoCacheProxyTest {

	Map<String, CacheInfo> infoMap;

	@Before
	public void init() throws Exception {
		infoMap = new HashMap<String, CacheInfo>();

		infoMap.put("listDummy", new CacheInfo(60));

		CacheFactory.getCache("DefaultCache").clear();
	}

	@Test
	public void testPojoProxy() throws Exception {
		BasicConfigurator.configure();

		Action action = (Action) CacheProxy.newInstance(new MyAction(), infoMap);

		System.out.println("First Call...");
		List<Dummy> d = action.listDummy("test");
		System.out.println("Value: " + d.get(0));
		for (int i = 0; i < 10; i++) {
			System.out.println("Cache HIT Calls...");
			d = action.listDummy("test");
			System.out.println("Value: " + d.get(0));
		}

		assertNotNull(d);

		System.out.println("Cache hits:   "+CacheFactory.getCache("DefaultCache").getCacheStatistics().getCacheHits());
		System.out.println("Cache misses: "+CacheFactory.getCache("DefaultCache").getCacheStatistics().getCacheMisses());

	}

	@Test
	public void testPojoProxyWithAnnotations() throws Exception {
		BasicConfigurator.configure();

		Action action = (Action) CacheProxy.newInstance(new MyActionWithAnnotation());

		System.out.println("First Call...");
		List<Dummy> d = action.listDummy("test");
		System.out.println("Value: " + d.get(0));
		for (int i = 0; i < 10; i++) {
			System.out.println("Cache HIT Calls...");
			d = action.listDummy("test");
			System.out.println("Value: " + d.get(0));
		}

		assertNotNull(d);

		System.out.println("Cache hits:   "+CacheFactory.getCache("DefaultCache").getCacheStatistics().getCacheHits());
		System.out.println("Cache misses: "+CacheFactory.getCache("DefaultCache").getCacheStatistics().getCacheMisses());

	}

	static interface Action {
		List<Dummy> listDummy(String str);
	}

	static class MyAction implements Action {
		@Override
		public List<Dummy> listDummy(String str) {
			List<Dummy> l = new ArrayList<Dummy>();
			Dummy s1 = new Dummy(DateFormat.getDateInstance().format(new Date()), str);
			l.add(s1);
			System.out.println("Calling the REAL slow method.....");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
			return l;
		}

	}

	static class MyActionWithAnnotation implements Action {

		@Cacheable(ttl = 60, negativeCache = true)
		@Override
		public List<Dummy> listDummy(String str) {
			List<Dummy> l = new ArrayList<Dummy>();
			Dummy s1 = new Dummy(DateFormat.getDateInstance().format(new Date()), str);
			l.add(s1);
			System.out.println("Calling the REAL slow method 2.....");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
			return l;
		}

	}

	static class Dummy implements Serializable {
		private static final long serialVersionUID = -313566357479556460L;
		public String a;
		public String b;

		public Dummy(String a, String b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "Dummy [a=" + a + ", b=" + b + "]";
		}
	}


}
