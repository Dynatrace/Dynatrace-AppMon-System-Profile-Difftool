package com.dynatrace.profilediff;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import com.dynatrace.common.stringcache.StringCacheInterface;

public class SortingStringMetricXmlDiffer extends StringMetricXmlDiffer {

	SortingStringMetricXmlDiffer(List<String> ignoreAttributeNames) {
		super(ignoreAttributeNames);
	}

	protected SortingStringMetricXmlDiffer(List<String> ignoreAttributeNames, MetricResolver metricResolver, int threshold) {
		super(ignoreAttributeNames, metricResolver, threshold);
	}
	
	@Override
	protected List<XmlElement> sort(List<XmlElement> list) {
		List<XmlElement> result = new ArrayList<>(list);
		Collections.sort(result);
		return result;
	}
	
	@Override
	protected boolean binarySearch(List<XmlElement> list, XmlElement element, StringCacheInterface stringCache, BitSet result) {
		int index = Collections.binarySearch(list, element);
		if (index < 0) {
			/*
			 * insertion position not helpful since the string metric is not aligned with sort order.
			 */
			return false;
		}
		int fromIndex; // inclusive
		int toIndex; // exclusive
		for (fromIndex = index; fromIndex > 0; fromIndex--) {
			if (!stringCache.equal(element.name, list.get(fromIndex - 1).name)) {
				break;
			}
		}
		for (toIndex = index + 1; toIndex < list.size(); toIndex++) {
			if (!stringCache.equal(element.name, list.get(toIndex).name)) {
				break;
			}
		}
		result.set(fromIndex, toIndex);
		return true;
	}
	
	@Override
	protected <T extends Comparable<T>> int smallestComparable(List<T> list, BitSet indices) {
		return indices.nextSetBit(0); // just take the first element that automatically is the smallest comparable since we sorted the list.
	}
}
