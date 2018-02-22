package com.dynatrace.profilediff;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import com.dynatrace.common.conf.DebugManager;
import com.dynatrace.common.stringcache.StringCacheInterface;

/**
 * More sophisticated approach where String metrics are applied to names.
 * The algorithm works tree-wise, not flattened, and tries to find a closest match
 * for every child element (i.e., the one with minimum distance).
 * Unlike {@link XmlDiffer}, ambiguity detection is not done eagerly by calculating ambiguousPaths,
 * but by maintaining a {@link BitSet} that contains minimum distance candidates.
 * 
 * Actual distance computation is abstracted to the {@link Metric} functional interface.
 * Mapping elements (by parent) to concrete {@link Metric} instances is done with the 
 * {@link MetricResolver} functional interface.
 * A <code>null</code> {@link Metric} is allowed, it will behave like an equality metric.
 * 
 * Algorithmic tweaks:
 * - try-null-metric
 * - sort/binarySearch
 *
 * @author cwat-pgrasboe
 */
public class StringMetricXmlDiffer extends XmlDiffer {
	
	private static boolean debug = DebugManager.isFlagEnabled("debug", false);
	
	private static final String SUBCHILDREN_SEPARATOR = "->";
	private static final MetricResolver equalityMetricResolver = (XmlElement) -> null;
	
	public static MetricResolver getEqualityMetricResolver() {
		return equalityMetricResolver;
	}
	
	public static MetricResolver getSubTreeMetricResolver(Metric metric, String... paths) {
		String[] rawPaths = convertPaths(paths);
		return (XmlElement element) -> {
			for (String rawPath : rawPaths) {
				if (element.rawPath.startsWith(rawPath)) {
					return metric;
				}
			}
		
			return null;
		};
	}
	
	private static String[] convertPaths(String[] paths) {
		String[] result = new String[paths.length];
		for (int i = 0; i < result.length; i++) {
			if (!paths[i].startsWith(XmlLexer.LEVEL_SEPARATOR)) {
				throw new IllegalArgumentException("Only absolute paths starting with '/' are allowed; invalid string: " + paths[i]);
			}
			result[i] = paths[i].substring(XmlLexer.LEVEL_SEPARATOR.length());
		}
		return result;
	}
	
	@FunctionalInterface
	public static interface MetricResolver {
		Metric getMetric(XmlElement parent);
	}
	
	@FunctionalInterface
	public static interface Metric {
		int distance(String left, String right, int threshold);
	}
	
	private final MetricResolver metricResolver;
	private final int threshold;
	
	StringMetricXmlDiffer(List<String> ignoreAttributeNames) {
		this(ignoreAttributeNames, equalityMetricResolver, -1);
	}
	
	protected StringMetricXmlDiffer(List<String> ignoreAttributeNames, MetricResolver metricResolver, int threshold) {
		super(ignoreAttributeNames);
		if (metricResolver == null) {
			throw new IllegalArgumentException("metricResolver must not be null");
		}
		if (threshold == 0) {
			throw new IllegalArgumentException("threshold must be non-zero");
		}
		this.metricResolver = metricResolver;
		this.threshold = threshold;
	}
	
	@Override
	protected void findStructualChanges(XmlStruct base, XmlStruct mod, Result result, StringCacheInterface stringCache) {
		assert base.root().name.equals(mod.root().name); // provided by base class
		peer(base.root(), mod.root(), 0, 1);
		diffWithMetric(base.root(), mod.root(), stringCache);
	}
	
	@Override
	protected void resetPeer(XmlElement element) {
		super.resetPeer(element);
		element.distance = -1;
		element.cardinality = -1;
	}
	
	private void peer(XmlElement left, XmlElement right, int distance, int cardinality) {
		left.peer = right;
		right.peer = left;
		left.distance = right.distance = distance;
		left.cardinality = right.cardinality = cardinality;
	}
	
	protected List<XmlElement> sort(List<XmlElement> list) {
		return list; // subclassing hook
	}
	
	protected boolean binarySearch(List<XmlElement> list, XmlElement element, StringCacheInterface stringCache, BitSet result) {
		/*
		 * we could perform a linear search for equal elements in the list, but this
		 * optimization is already covered by the try-null-metric trick (and that even works for depth > 0),
		 * so we do nothing.
		 */
		return false;  // subclassing hook
	}
	
	private static boolean checkEquality(List<XmlElement> list, XmlElement element, StringCacheInterface stringCache, BitSet result) {
		for (int i = 0; i < list.size(); i++) {
			XmlElement e = list.get(i);
			boolean expected = result.get(i);
			boolean actual = stringCache.equal(element.name, e.name);
			if (expected != actual) {
				return false;
			}
		}
		return true;
	}
	
	private void diffWithMetric(XmlElement left, XmlElement right, StringCacheInterface stringCache) {
		diffWithMetric0(left.children, right.children, metricResolver.getMetric(left), stringCache);
	}
	
	private void diffWithMetric0(List<XmlElement> left, List<XmlElement> right, Metric metric, StringCacheInterface stringCache) {
		if (left.size() <= right.size()) {
			diffWithMetric1(sort(left), sort(right), metric, stringCache);
		} else {
			diffWithMetric1(sort(right), sort(left), metric, stringCache);
		}
	}
	
	/**
	 * the global maximum of all minDistances found.
	 * This is the minimum threshold that must be configured for the same result.
	 */
	static int globalMaxMinDistance = -1;
	static int globalMaxMaxDistance = -1;
	static int globalMaxDepth = -1;
	static int globalMetricInvokeCount = 0;
	
	private void diffWithMetric1(final List<XmlElement> leftIn, final List<XmlElement> rightIn, final Metric metric, final StringCacheInterface stringCache) {
		assert leftIn.size() <= rightIn.size();
		
		BitSet takenIndices = new BitSet(); // into right list. indices of elements that have found a peer on the left side.
		List<XmlElement> left = leftIn;
		List<XmlElement> right = rightIn;
		List<XmlElement> nextLeft = new ArrayList<>();
		
		//TODO make minDistance() function.
		
		do { // re-visit loop. usually only one iteration, at most 2.
			for (int i = 0; i < left.size(); i++) {
				XmlElement leftElement = left.get(i);
				assert leftElement.peer == null;
				
				BitSet minIndices = new BitSet(); // into right list. indices of minimum candidates.
				BitSet prevMinIndices = null;     // into right list. indices of minimum candidates of previous round.
				int depth = 0;                    // depth to construct children names
				int maxDepth = leftElement.xml.depth - leftElement.level;
				assert maxDepth > 0;
				int c, t, minDistance, maxDistance = -1;
				Metric m = null; // try-null-metric trick
				
				do { // depth/try-null-metric loop.
					assert depth <= maxDepth;
					if (depth > globalMaxDepth) {
						globalMaxDepth = depth;
					}
					search: {
						if (depth == 0 && binarySearch(right, leftElement, stringCache, minIndices)) {
							assert minIndices.cardinality() > 0;
							assert checkEquality(right, leftElement, stringCache, minIndices);
							/*
							 * the shortcut can only be used if the things checked with normal search also apply:
							 * (1) takenIndices
							 * (2) peer can be overridden since better (not equal) match.
							 */
							for (int j = minIndices.nextSetBit(0); j != -1; j = minIndices.nextSetBit(j + 1)) {
								if (takenIndices.get(j)) {
									minIndices.clear(j);
								} else {
									XmlElement rightElement = right.get(j);
									if (rightElement.peer != null && !checkOverridePeer(leftElement, rightElement, /*distance*/ 0, left, nextLeft)) {
										minIndices.clear(j);// already peered up with less or equal distance = better match. don't overwrite
									}
								}
							}
							c = minIndices.cardinality();
							if (c > 0) {
								minDistance = 0;
								break search; // we can actually use binary search shortcut result
							}
						}
						String leftString = stringCache.cache(childrenNames(leftElement, depth));
						minDistance = Integer.MAX_VALUE;
						t = threshold;
						
						for (int j = 0; j < right.size(); j++) {
							if (takenIndices.get(j) || prevMinIndices != null && !prevMinIndices.get(j)) {
								/*
								 * if the takenIndices contains us, it's a bug to check that index again.
								 * the prevMinIndices check is just an optimization so we don't have to check candidates again that have 
								 * already been ruled out in the previous round. 
								 */
								continue;
							}
							
							XmlElement rightElement = right.get(j);
							if (!stringCache.equal(rightElement.rawName, leftElement.rawName)) {
								continue; // it's pointless and wrong to compare non-matching raw names.
							}
							String rightString = stringCache.cache(childrenNames(rightElement, depth));
							int distance = distance0(leftString, rightString, t, m, stringCache);
							assert t < 0 || distance <= t;
		//					assert distance == distance0(rightString, leftString, t, metric, stringCache) : "unsymmetric metric";
							if (distance < 0) {
								continue; // not comparable
							}
							if (rightElement.peer != null && !checkOverridePeer(leftElement, rightElement, distance, left, nextLeft)) {
								continue; // already peered up with less or equal distance = better match. don't overwrite
							}
							assert rightElement.peer == null;
							
							if (distance < minDistance) { // new minimum found
								t = minDistance = distance; // also reduce threshold for this round - huge performance gain.
								minIndices.clear();
								minIndices.set(j);
							} else if (distance <= minDistance) { // new co-minimum found
								minIndices.set(j);
							}
							if (distance > maxDistance) {
								maxDistance = distance;
							}
						} // for (right)
						c = minIndices.cardinality();
					} // search block
					
					assert c == minIndices.cardinality();
					if (c == 0 && m == null && metric != null) {
						m = metric;
						depth = 0;
						continue; // try-null-metric didn't work, try with actual metric
					}
					if (c <= 1 || depth == maxDepth) {
						break; // zero match = don't continue, one match = perfect, or depth exceeded
					}
					prevMinIndices = minIndices;
					minIndices = new BitSet();
					depth++;
				} while (true); // depth/try-null-metric loop
				
				assert c == minIndices.cardinality();
				if (c > 0) {
					int minIndex = smallestComparable(right, minIndices);
					assert minIndex != -1;
					assert minIndex == smallestComparable0(right, minIndices);
					if (minDistance == 0) { // only block perfect matched indices from further use
						takenIndices.set(minIndex);
					}
					XmlElement rightElement = right.get(minIndex);
					
					if (minDistance > globalMaxMinDistance) {
						globalMaxMinDistance = minDistance;
					}
					if (maxDistance > globalMaxMaxDistance) {
						globalMaxMaxDistance = maxDistance;
					}
					
					if (debug) {
						String leftString = stringCache.cache(childrenNames(leftElement, depth));
						String rightString = stringCache.cache(childrenNames(rightElement, depth));
						
						System.out.printf("[ELEM] #%d %s (%s) @%d%n", leftElement.index, leftString, leftElement.parent.path, System.identityHashCode(leftElement));
						System.out.printf("[PEER] #%d %s (%s) @%d%n", rightElement.index, rightString, rightElement.parent.path, System.identityHashCode(rightElement));
						System.out.printf("       depth=%d minDistance=%d maxDistance=%d globalMaxMinDistance=%d globalMaxMaxDistance=%d minIndex=%d minIndices=%s cardinality=%d peered=%d left-children=%d right-children=%d%n", depth, minDistance, maxDistance, globalMaxMinDistance, globalMaxMaxDistance, minIndex, minIndices, minIndices.cardinality(), takenIndices.cardinality(), left.size(), right.size());
						System.out.printf("---------------------------------------------------------------------%n");
					}
					peer(leftElement, rightElement, minDistance, c);
				} else {
					resetPeer(leftElement);
					assert leftElement.peer == null;
					if (debug) {
						String leftString = stringCache.cache(childrenNames(leftElement, depth));
						
						System.out.printf("[ELEM] #%d %s (%s) @%d%n", leftElement.index, leftString, leftElement.parent.path, System.identityHashCode(leftElement));
						System.out.printf("nopeer depth=%d minDistance=%d maxDistance=%d globalMaxMaxDistance=%d minIndices=%s peered=%d left-children=%d right-children=%d%n", depth, minDistance, maxDistance, globalMaxMaxDistance, minIndices, takenIndices.cardinality(), left.size(), right.size());
						System.out.printf("---------------------------------------------------------------------%n");
					}
				}
			} // for (left)
			
			/*
			 * let's recurse.
			 */
			for (int i = 0; i < left.size(); i++) {
				XmlElement element = left.get(i);
				if (element.peer != null) {
					diffWithMetric(element, element.peer, stringCache);
				}
			}
			
			left = nextLeft;
			nextLeft = null; // only one re-visit round for better matches that have been overridden.
		} while (left != null);
	}
	
	private boolean checkOverridePeer(XmlElement leftElement, XmlElement rightElement, int distance, List<XmlElement> left, List<XmlElement> nextLeft) {
		assert rightElement.peer != null;
		assert rightElement.distance >= 0;
		assert rightElement.peer.peer != null;
		assert leftElement.xml == rightElement.peer.xml; // on the same side
		if (distance >= rightElement.distance || nextLeft == null) {
			return false; // already peered up with less or equal distance = better match. don't overwrite
		}
		/*
		 * We're gonna have to overwrite the rightElement's peer.
		 * In order to have deterministic result, we store the element in question for a re-visit.
		 */
		assert nextLeft != null; // in the second round distances should be all lesser so above continue is entered.
		assert nextLeft.size() < left.size();
		nextLeft.add(rightElement.peer);
		
		if (debug) {
			System.out.printf("[REVISIT] old mapping: %s <--%d--> %s%n", rightElement.peer.name, rightElement.distance, rightElement.peer.peer.name);
			System.out.printf("[REVISIT] new mapping: %s <--%d--> %s%n", leftElement.name, distance, rightElement.name);
			System.out.printf("---------------------------------------------------------------------%n");
		}
		/*
		 * it's quite important to reset the peer's peer and peer for the next round.
		 */
		resetPeer(rightElement.peer);
		resetPeer(rightElement);
		return true;
	}
	
	private static int distance0(String left, String right, int threshold, Metric metric, StringCacheInterface stringCache) {
		int d = distance1(left, right, threshold, metric, stringCache);
//		System.out.printf("%s <%d> %s %n", left, d, right);
		return d;
	}
		
	private static int distance1(String left, String right, int threshold, Metric metric, StringCacheInterface stringCache) {		
		if (stringCache.equal(left, right)) {
			return 0; // huge performance gain - we don't bother the potentially expensive metric with equal strings
		}
		if (metric != null && threshold != 0) { // a zero threshold would give us only a zero distance that we just ruled out
			globalMetricInvokeCount++;
			return metric.distance(left, right, threshold);
		}
		return -1; // not comparable
	}
	
	protected <T extends Comparable<T>> int smallestComparable(List<T> list, BitSet indices) {
		return smallestComparable0(list, indices);
	}
	
	/**
	 * Gets the smallest {@link Comparable} among the indices set in the {@link BitSet}.
	 * Very handy algorithm to have deterministic picking among matches with equal distance, like the list were sorted.
	 */
	protected static <T extends Comparable<T>> int smallestComparable0(List<T> list, BitSet indices) {
		T result = null;
		int resultIndex = -1;
		for (int i = indices.nextSetBit(0); i != -1; i = indices.nextSetBit(i + 1)) {
			T candidate = list.get(i);
			if (result == null || candidate.compareTo(result) < 0) {
				result = candidate;
				resultIndex = i;
			}
		}
		return resultIndex;
	}
	
	static String childrenNames(XmlElement element, int maxDepth) {
		return childrenNamesRecursive(element, maxDepth);
	}
	
	private static String childrenNamesRecursive(XmlElement element, int maxDepth) {
		if (maxDepth < 0) {
			throw new IllegalArgumentException("maxDepth must be positive");
		}
		if (maxDepth == 0) {
			return element.name;
		}
		StringBuilder buf = new StringBuilder(element.name);
		String sep = SUBCHILDREN_SEPARATOR;
		childrenNames0(buf, sep, element, 0, maxDepth);
		return buf.toString();
	}
	
	private static void childrenNames0(StringBuilder buf, String sep, XmlElement element, int depth, int maxDepth) {
		for (XmlElement child : element.children) {
			buf.append(sep).append(child.level);
			sep = CHILDREN_SEPARATOR;
			buf.append(sep).append(child.name);
			if (depth + 1 < maxDepth) {
				childrenNames0(buf, sep, child, depth + 1, maxDepth);
			}
		}
	}

	@SuppressWarnings("unused") // recursive impl.
	private static String childrenNamesIterative(XmlElement element, int maxDepth) {
		if (maxDepth < 0) {
			throw new IllegalArgumentException("maxDepth must be positive");
		}
		if (maxDepth == 0) {
			return element.name;
		}
		StringBuilder buf = new StringBuilder(element.name);
		String sep = SUBCHILDREN_SEPARATOR;
		LinkedList<XmlElement> toAnalyze = new LinkedList<>(element.children);
		int depth = 0;
		
		while (!toAnalyze.isEmpty()) {
			XmlElement child = toAnalyze.removeFirst();
			buf.append(sep).append(child.level);
			sep = CHILDREN_SEPARATOR;
			buf.append(sep).append(child.name);
			if (depth + 1 < maxDepth && !child.children.isEmpty()) {
				/*
				 * depth counting not really "correct" but needed that way...
				 */
				depth++;
				toAnalyze.addAll(child.children);
			}
		}
		return buf.toString();
	}
}
