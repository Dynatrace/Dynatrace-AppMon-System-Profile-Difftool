package com.dynatrace.common.stringcache;

import java.util.ArrayList;
import java.util.Map;

public class StringCache implements StringCacheInterface {
	
	private final Map<String, String> map;
	
	private int hits;
	
	public StringCache(Map<String, String> map) {
		this.map = map;
	}
	
	/* (non-Javadoc)
	 * @see com.compuware.logtree.data.StringCacheInterface#cache(java.lang.String)
	 */
	@Override
	public String cache(String text) {
		String result = map.get(text);
		if (result == null) {
			map.put(text, result = text);
		} else {
			hits++;
		}
		
		return result;
	}
	
	@Override
	public void printStats(String desc) {
		int misses = map.size();
		System.out.printf("StringCache '%s' hits: %d, misses: %d, hit ratio: %.2f%n", desc, hits, misses, (double) hits / misses);
	}
		
	public void printSomeStrings(int max) {
		int n = 0;
		ArrayList<String> strings = new ArrayList<>(map.values());
		for (String string : strings) {
			System.out.println(string);
			if (++n == max) {
				break;
			}
		}
	}
	 
	@Override
	public void clear() {
		hits = 0;
		map.clear();
	}

	@Override
	public boolean equal(String left, String right) {
//		assert map.containsKey(left) : "uncached string: " + left;
//		assert map.containsKey(right) : "uncached string: " + right;
		return left == right;
	}
}
