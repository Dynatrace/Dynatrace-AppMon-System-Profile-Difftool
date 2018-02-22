package com.dynatrace.common.stringcache;

import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;

public class StringCacheTest {
	
	@Test
	public void StringCache() {
		StringCacheInterface cache = new StringCache(new HashMap<>());
		String cached1 =  cache.cache(new String("string"));
		String cached2 =  cache.cache(new String("string"));
		Assert.assertTrue(cached1 == cached2);
		Assert.assertTrue(cache.equal(cached1, cached2));
		cache.clear();
		String cached3 =  cache.cache(new String("string"));
		String cached4 =  cache.cache(new String("string"));
		Assert.assertFalse(cached3 == cached2);
		Assert.assertTrue(cached3 == cached4);
		Assert.assertTrue(cache.equal(cached4, cached3));
		Assert.assertFalse(cache.equal(cached1, cached3));
		cache.printStats("StringCache");
	}
	
	@Test
	public void NullStringCache() {
		StringCacheInterface cache = NullStringCache.INSTANCE;
		String cached1 =  cache.cache(new String("string"));
		String cached2 =  cache.cache(new String("string"));
		Assert.assertFalse(cached1 == cached2);
		Assert.assertTrue(cache.equal(cached1, cached2));
		cache.printStats("NullStringCache");
	}
}
