package com.dynatrace.profilediff;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;

import com.dynatrace.common.stringcache.NullStringCache;
import com.dynatrace.common.stringcache.StringCache;
import com.dynatrace.common.stringcache.StringCacheInterface;
import com.dynatrace.profilediff.StringMetricXmlDiffer.MetricResolver;
import com.dynatrace.profilediff.lib.StringMetrics;

public abstract class TestBase {
	
	protected static final List<String> SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES = Arrays.asList(
			  "id"
			, "key"
			, "measure:metricgroupid" 
			, "measure:metricid"
			, "refmeasure" 
			, "refmetricgroup" 
			, "refmetric" 
			, "measure:transactionname"
			, "sourcegroup:sourcegroupid"
			, "/dynatrace/systemprofile/technology:type"
			, "/dynatrace/systemprofile/configurations/configuration/sensorconfig:refagentgroup"
			, "/dynatrace/systemprofile/uemconfiguration/sotwcategoryindex/categories/sotwcategory:apdexcategory"
			, "/dynatrace/systemprofile/uemconfiguration/uemdomainconfig/uemdomainthirdparty/uemdomain:domain"
			, "/dynatrace/systemprofile/uemconfiguration/uemhealthcheckconfig/uemapplicationhealthcheckconfigs/uemapplicationhealthcheckconfig:name"
			
			, "capabilities:type"
			
			// new ones found - but will break tests. we don't need to include them anyway.
//			, "class:pattern"
//			, "class/method:pattern"
//			, "class/method/argument:pattern"
//			, "rule:rulekey"
//			, "/dynatrace/systemprofile/uemconfiguration/applications/application:name"
	);
	
	protected static final List<String> SYSTEM_PROFILE_IGNORED_ATTRIBUTES = Arrays.asList(
			  "createdtimestamp"
			, "rev"
			, "measure/color:color.red"
			, "measure/color:color.green"
			, "measure/color:color.blue"
	);
	
	protected static final List<String> LEVENSHTEIN_DISCRIMINATOR_ATTRIBUTES = (Arrays.asList(
			  "class:pattern"
			, "class/method:pattern"
			, "class/method/argument:pattern"
	));
	
	private static final String[] LEVENSHTEIN_PATHS = {
			  "/plugin/extension/sensorpack"
			, "/dynatrace/systemprofile/sensorgroups"
	};
	
	/** 
	 * prove StringCache point by provoking OOM using "memtest" with a small -Xmx
	 * and setting this to {@link NullStringCache#INSTANCE}.
	 */
	protected static final StringCacheInterface stringCache = new StringCache(new HashMap<>());
//	protected static final StringCacheInterface stringCache = NullStringCache.INSTANCE;

	protected static final MetricResolver levenshteinMetricResolver = StringMetricXmlDiffer.getSubTreeMetricResolver(StringMetrics::getLevenshteinDistance, LEVENSHTEIN_PATHS);
		
	protected static <T> List<T> concatLists(List<T> dest, Collection<T> left, Collection<T> right) {
		dest.addAll(left);
		dest.addAll(right);
		return dest;
	}

	

	static String escapeString(String message) { 
		// order of replace()s is important!
		// \ => \\
		// " => \"
		// \n => \n (escaped)
		// \r => \r (escaped)
		return message == null ? null : message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
	}

	private static void printList(List<String> list) {
		String sep = "  ";
		for (String elem : list) {
			System.out.println(sep + qq(escapeString(elem)));
			sep = ", ";
		}
		System.out.println();
	}

	protected static void checkList(List<String> expected, List<String> actual) {
		boolean ok = false;
		try {
			ok = checkList0(expected, actual);
//		} catch (AssertionError e) {
		} finally {
			if (!ok) {
				printList(actual);
			}
		}
	}

	private static boolean eq(Object left, Object right) {
		return left == null ? right == null : left.equals(right);
	}

	private static boolean checkList0(List<String> expected, List<String> actual) {
		String msg = "";
		Assert.assertNotNull(msg, actual);
		Assert.assertEquals(msg + " list size", expected.size(), actual.size());

		StringBuilder buf = new StringBuilder();
		int errors = 0;

		for (int i = 0; i < actual.size(); i++) {
			if (!eq(expected.get(i), actual.get(i))) {
				buf.append("\nIndex#" + i + " expected: " + q(expected.get(i)) +  ", actual: " + q(actual.get(i)));
				errors++;
			}
		}

		Assert.assertTrue(msg + buf, errors == 0);
		return true;
	}
	
	static String q(String s) {
		return s == null ? null : "'" + s + "'";
	}

	static String qq(String s) {
		return s == null ? null : '"' + s + '"';
	}
	
	static List<String> toPathStrings(List<XmlElement> list) {
		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return list.get(index).toPathString();
			}

			@Override
			public int size() {
				return list.size();
			}
		};
	}

	static List<String> toOffsetStrings(List<XmlElement> list) {
		return new AbstractList<String>() {
			
			@Override
			public String get(int index) {
				return list.get(index).toOffsetString();
			}
			
			@Override
			public int size() {
				return list.size();
			}
		};
	}
	
	static List<String> toRawPathStrings(List<XmlElement> list) {
		return new AbstractList<String>() {
			
			@Override
			public String get(int index) {
				return list.get(index).rawPath;
			}
			
			@Override
			public int size() {
				return list.size();
			}
		};
	}
	
	static List<String> toStateStrings(List<XmlElement> list) {
		return new AbstractList<String>() {
			
			@Override
			public String get(int index) {
				return list.get(index).toStateString();
			}
			
			@Override
			public int size() {
				return list.size();
			}
		};
	}
	
	static List<String> toChildrenNamesStrings(List<XmlElement> list, int maxDepth) {
		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return StringMetricXmlDiffer.childrenNames(list.get(index) , maxDepth);
			}

			@Override
			public int size() {
				return list.size();
			}
		};
	}
	
	protected static void resetGlobalMaxDistances() {
		StringMetricXmlDiffer.globalMaxMinDistance = -1;
		StringMetricXmlDiffer.globalMaxMaxDistance = -1;
		StringMetricXmlDiffer.globalMaxDepth = -1;
		StringMetricXmlDiffer.globalMetricInvokeCount = 0;
	}
	
	protected static void expectGlobalMaxDistances(String desc, int expectedMin, int expectedMax, int expectedDepth, int expectedMetricInvokeCount) {
		System.out.printf("[%s] StringMetricXmlDiffer.globalMaxMinDistance=%d%n", desc, StringMetricXmlDiffer.globalMaxMinDistance);
		System.out.printf("[%s] StringMetricXmlDiffer.globalMaxMaxDistance=%d%n", desc, StringMetricXmlDiffer.globalMaxMaxDistance);
		System.out.printf("[%s] StringMetricXmlDiffer.globalMaxDepth=%d%n", desc, StringMetricXmlDiffer.globalMaxDepth);
		System.out.printf("[%s] StringMetricXmlDiffer.globalMetricInvokeCount=%d%n", desc, StringMetricXmlDiffer.globalMetricInvokeCount);
		if (expectedMin != -1) {
			Assert.assertEquals(desc + ".globalMaxMinDistance", expectedMin, StringMetricXmlDiffer.globalMaxMinDistance);
		}
		if (expectedMax != -1) {
			Assert.assertEquals(desc + ".globalMaxMaxDistance", expectedMax, StringMetricXmlDiffer.globalMaxMaxDistance);
		}
		if (expectedDepth != -1) {
			Assert.assertEquals(desc + ".globalMaxDepth", expectedDepth, StringMetricXmlDiffer.globalMaxDepth);
		}
		if (expectedMetricInvokeCount != -1) {
			Assert.assertEquals(desc + ".globalMetricInvokeCount", expectedMetricInvokeCount, StringMetricXmlDiffer.globalMetricInvokeCount);
		}
	}
	
	protected static final StringMetricXmlDifferFactory shufflingStringMetricDifferFactory = ShufflingStringMetricDiffer::new; 
	protected static final XmlDifferFactory shufflingStringMetricDifferBaseFactory = ShufflingStringMetricDiffer::new; 
	
	private static class ShufflingStringMetricDiffer extends StringMetricXmlDiffer {

		protected ShufflingStringMetricDiffer(List<String> ignoreAttributeNames, MetricResolver metricResolver, int threshold) {
			super(ignoreAttributeNames, metricResolver, threshold);
		}
		
		protected ShufflingStringMetricDiffer(List<String> ignoreAttributeNames) {
			super(ignoreAttributeNames);
		}
		
		@Override
		protected List<XmlElement> sort(List<XmlElement> list) {
			return shuffle(list);
		}
	}
	
	protected static final StringMetricXmlDifferFactory shufflingSortingStringMetricDifferFactory = ShufflingSortingStringMetricDiffer::new;
	protected static final XmlDifferFactory shufflingSortingStringMetricDifferBaseFactory = ShufflingSortingStringMetricDiffer::new;
	
	private static class ShufflingSortingStringMetricDiffer extends SortingStringMetricXmlDiffer {
		
		protected ShufflingSortingStringMetricDiffer(List<String> ignoreAttributeNames, MetricResolver metricResolver, int threshold) {
			super(ignoreAttributeNames, metricResolver, threshold);
		}
		
		protected ShufflingSortingStringMetricDiffer(List<String> ignoreAttributeNames) {
			super(ignoreAttributeNames);
		}
		
		@Override
		protected List<XmlElement> sort(List<XmlElement> list) {
			return shuffle(list);
		}
		
		@Override
		protected boolean binarySearch(List<XmlElement> list, XmlElement element, StringCacheInterface stringCache, BitSet result) {
			return false; // can't 
		}
		
		@Override
		protected <T extends Comparable<T>> int smallestComparable(List<T> list, BitSet indices) {
			return smallestComparable0(list, indices); // since not sorted, need to have base class behavior (not the optimized that relies on sortedness)
		}
	}
	
	protected static final StringMetricXmlDifferFactory nonBinarySearchSortingStringMetricDifferFactory = NonBinarySearchSortingStringMetricDiffer::new;
	protected static final XmlDifferFactory nonBinarySearchSortingStringMetricDifferBaseFactory = NonBinarySearchSortingStringMetricDiffer::new;
	
	/**
	 * quite handy for finding bugs in the binarySearch shortcut.
	 */
	private static class NonBinarySearchSortingStringMetricDiffer extends SortingStringMetricXmlDiffer {
		
		protected NonBinarySearchSortingStringMetricDiffer(List<String> ignoreAttributeNames, MetricResolver metricResolver, int threshold) {
			super(ignoreAttributeNames, metricResolver, threshold);
		}
		
		protected NonBinarySearchSortingStringMetricDiffer(List<String> ignoreAttributeNames) {
			super(ignoreAttributeNames);
		}
		
		@Override
		protected boolean binarySearch(List<XmlElement> list, XmlElement element, StringCacheInterface stringCache, BitSet result) {
			return false; // won't
		}
	}
	
	private static List<XmlElement> shuffle(List<XmlElement> list) {
		List<XmlElement> result = new ArrayList<>(list);
		Collections.shuffle(result);
		return result;
	}
	
	protected static boolean isDifferImplStringMetric(XmlDiffer differ) {
		return differ instanceof StringMetricXmlDiffer;
	}
	
	protected static boolean isDifferImplShufflingStringMetric(XmlDiffer differ) {
		return differ instanceof ShufflingStringMetricDiffer || differ instanceof ShufflingSortingStringMetricDiffer;
	}
}
