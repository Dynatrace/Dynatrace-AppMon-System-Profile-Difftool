package com.dynatrace.profilediff;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dynatrace.common.stringcache.StringCacheInterface;

public class XmlStruct {
	
	public final List<XmlElement> elements;
	public final CharSequence data;
	final StringCacheInterface stringCache;
	
	public int depth;
	Boolean isBase; // differ state
	
	@Deprecated
	String[] lines;
	
	XmlStruct(List<XmlElement> elements, CharSequence data, StringCacheInterface stringCache) {
		this.elements = elements;
		this.data = data;
		this.stringCache = stringCache;
	}
	

	public CharSequence getElementData(XmlElement element) {
		int i = element.openTag.start;
		while (i > 0 && data.charAt(i - 1) != '\n') {
			i--; // also include the indent
		}
		return data.subSequence(i, element.closeTag.end);
	}
	
	public XmlElement root() {
		return elements.get(0);
	}
	
	private static class OffsetComparator implements Comparator<XmlElement> {
		private int offset;
	
		@Override
		public int compare(XmlElement o1, XmlElement o2) {
			assert o1 != null;
			assert o2 == null;  // we ignore o2
			return o1.openTag.start - offset;
		}
		
		OffsetComparator with(int offset) {
			this.offset = offset;
			return this;
		}
	}
	
	private final OffsetComparator offsetComparator = new OffsetComparator();
	
	public XmlElement findElementAtOffset(int offset) {
		int index = Collections.binarySearch(elements, /*key*/ null, offsetComparator.with(offset));
		
		if (index < 0) {
			index = - index - 2; // one index before insertion point
			if (index == elements.size()) { // after last
				index--;
			} else if (index == -1) { // before first
				index++;
			}
		}
		
		assert 0 <= index && index < elements.size();
		
		XmlElement element = elements.get(index);
		
		while (offset > element.closeTag.end && element.parent != null) {
			element = element.parent;
		}
		
		assert element != null;
		return element;
	}
	
	public List<XmlElement> getAllDescendants(XmlElement element) {
		/*
		 * it's way cheaper like this, than having to recursively traversing nested children.
		 */
		int level = element.level;
		int firstIndex = element.index + 1;
		int index = firstIndex;
		for (; index < elements.size(); index++) {
			XmlElement e = elements.get(index);
			if (e.level <= level) {
				break;
			}
		}
		int size = index - firstIndex;
		if (size == 0) {
			return Collections.emptyList();
		}
		
		return new AbstractList<XmlElement>() {
			@Override
			public XmlElement get(int i) {
				return elements.get(firstIndex + i);
			}
			
			@Override
			public int size() {
				return size;
			}
		};
	}
	
	public List<XmlElement> findByRawPath(String rawPath) {
		List<XmlElement> result = new ArrayList<>();
		for (XmlElement element : elements) {
			if (element.rawPath.equals(rawPath)) {
				result.add(element);
			}
		}
		return result;
	}
	
	public XmlElement findByPath(String path) {
		for (XmlElement element : elements) {
			if (element.path.equals(path)) {
				return element;
			}
		}
		return null;
	}
}
