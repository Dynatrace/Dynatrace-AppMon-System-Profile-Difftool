package com.dynatrace.common.stringcache;

/**
 * Interface for a string cache implementation.
 * Similar to intern()ing strings, this is meant to reduce memory overhead by huge numbers of String instances.
 * 
 * @author Philipp
 */
public interface StringCacheInterface {

	public String cache(String text);
	
	boolean equal(String left, String right);

	public void clear();

	public void printStats(String desc);
}