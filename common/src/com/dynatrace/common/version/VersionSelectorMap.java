package com.dynatrace.common.version;

import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * A special map implementation that uses the correct version that is closest to the desired version.
 * @author cwat-pgrasboe
 *
 * @param <T>
 */
public class VersionSelectorMap<T> {
	
	final NavigableMap<String, T> map = new TreeMap<>();
	
	public NavigableMap<String, T> getMap() {
		return map;
	}

	public void put(String key, T object) {
		map.put(key, object);
	}
	
	public T find(String key) {
		if (map.isEmpty()) {
			throw new NoSuchElementException();
		}
		T result = map.get(key);
		if (result != null) {
			return result;
		}
		
		Map.Entry<String, T> curr = map.ceilingEntry(key);
		if (curr == null) {
			curr = map.lastEntry();
		}
		Map.Entry<String, T> prev = map.lowerEntry(curr.getKey());
		Map.Entry<String, T> resultEntry;
		
		if (prev == null) {
			resultEntry = curr;
		} else {		
			int currPrefixLength = commonPrefixLength(key, curr.getKey());
			int prevPrefixLength = commonPrefixLength(key, prev.getKey());
			if (currPrefixLength == prevPrefixLength) {
				resultEntry = key.compareTo(curr.getKey()) > 0 ? curr : prev;
			} else {
				resultEntry = currPrefixLength > prevPrefixLength ? curr : prev;
			}
		}
		
		return remap(key, resultEntry.getKey(), resultEntry.getValue());
	}
	
	protected T remap(String requestedKey, String newKey, T result) {
		return result;
	}
	
	static int commonPrefixLength(String left, String right) {
		int len = Math.min(left.length(), right.length());
		int i;
		for (i = 0; i < len; i++) {
			if (left.charAt(i) != right.charAt(i)) {
				break;
			}
		}
		return i;
	}
	
	public static class WithRemapping<T> extends VersionSelectorMap<T> {
		@Override
		protected T remap(String requestedKey, String newKey, T result) {
			map.put(requestedKey, result);
			return result;
		}
	}
}
