package com.dynatrace.common.stringcache;

public class NullStringCache implements StringCacheInterface {
	
	public static final NullStringCache INSTANCE = new NullStringCache();
	
	private NullStringCache() {
	}

	@Override
	public String cache(String text) {
		return text;
	}

	@Override
	public boolean equal(String left, String right) {
		return left.equals(right);
	}

	@Override
	public void clear() {
	}

	@Override
	public void printStats(String desc) {
	}
}
