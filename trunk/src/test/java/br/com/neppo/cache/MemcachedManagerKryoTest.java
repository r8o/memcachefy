package br.com.neppo.cache;

import com.esotericsoftware.kryo.Kryo;
import br.com.neppo.cache.memcached.MemcachedManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.Serializable;

import static org.junit.Assert.assertNotNull;

public class MemcachedManagerKryoTest {

	private MemcachedManager cacheManager;

	@Before
	public void setUp() throws JAXBException {
		cacheManager = new MemcachedManager();
		cacheManager.setDefaultTtl(60); // default time to live
		cacheManager.setHosts("localhost:11211"); // list of memcached hosts
		cacheManager.setCacheTranscoder(CacheTranscoder.KRYO);
		cacheManager.setKryo(new Kryo());
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testCacheManagerNonSerializable() throws InterruptedException, CacheException {

		DummyNonSerializable d = new DummyNonSerializable("Mickey_laskmdkasmdlaksmdkasdasdasdasdasdasdasasmdlaskmdlaksmdlaskmdaklsmmdaskmldasklmdsakasoijdasoidjasidjaoisjdoasijdsaoidjaoisdj",
				"Donald_askdadasdasdasdasdasdsadasdasdmaklsmdlaksmdalskmdaasdassdslkmdaslkdmaslkdmaskldmaklsmdalskmdalksmdlaksdmalskdmalskdmalskdmalksdmalskdmalskdmalskdmalkdm");
		String key = "plutinho";
		Cache<String, DummyNonSerializable> cache = cacheManager.getCache("test1");

		cache.put(key, d, 60);

		long init = System.currentTimeMillis();
		for (int i = 1; i <= 1000; i++) {
			DummyNonSerializable iamBack = cache.get(key);
			assertNotNull(iamBack);
			/*System.out.println("size: " + cache.size());
			System.out.println(iamBack);*/
		}

		System.out.println("Time(Non Serializable): " + (System.currentTimeMillis() - init));
	}

	@Test
	public void testCacheManager() throws InterruptedException, CacheException {

		Dummy d = new Dummy("Mickey_laskmdkasmdlaksmdkasdasdasdasdasdasdasasmdlaskmdlaksmdlaskmdaklsmmdaskmldasklmdsakasoijdasoidjasidjaoisjdoasijdsaoidjaoisdj",
				"Donald_askdadasdasdasdasdasdsadasdasdmaklsmdlaksmdalskmdaasdassdslkmdaslkdmaslkdmaskldmaklsmdalskmdalksmdlaksdmalskdmalskdmalskdmalksdmalskdmalskdmalskdmalkdm");
		String key = "plutinho";
		Cache<String, Dummy> cache = cacheManager.getCache("test2");

		cache.put(key, d);

		long init = System.currentTimeMillis();
		for (int i = 1; i <= 1000; i++) {
			Dummy iamBack = cache.get(key);
			assertNotNull(iamBack);
			//System.out.println("size: " + cache.size());
			//System.out.println(iamBack);
		}

		System.out.println("Time(Serializable): " + (System.currentTimeMillis() - init));

		cache.clear(); // flush the cache
	}

	static class DummyNonSerializable {

		protected String a;
		protected String b;

		protected DummyNonSerializable() {

		}

		public DummyNonSerializable(String a, String b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return a + "_plus_" + b;
		}

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}

	}

	static class Dummy implements Serializable {

		private static final long serialVersionUID = -8036246229455199519L;
		protected String a;
		protected String b;

		protected Dummy() {

		}

		public Dummy(String a, String b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return a + "_plus_" + b;
		}

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}

	}


}
