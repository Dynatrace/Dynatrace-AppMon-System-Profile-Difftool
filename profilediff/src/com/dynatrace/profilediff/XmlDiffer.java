package com.dynatrace.profilediff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.dynatrace.common.conf.DebugManager;
import com.dynatrace.common.stringcache.StringCacheInterface;

/**
 * Basic approach to XML structure diffing where both documents are flattened to a list of strings.
 * Those strings, called "paths", have discrimator attributes include (from all nesting levels).
 * This differ then simply checks for every path, if the same path is present in the other document.
 * 
 * @author cwat-pgrasboe
 */
public class XmlDiffer {
	
	private static boolean printMissingDiscriminators = DebugManager.isFlagEnabled("printMissingDiscriminators", false);

	protected static final String CHILDREN_SEPARATOR = ";";

	public static class Result {
		public int nAdded;
		public int nRemoved;
		public int nAttributeChanged;
		private Map<String, List<XmlElement>> ambiguousPaths; // Could be a Set<String>; but maybe we need the elements in the future.
		private Set<String> missingDiscriminators = new LinkedHashSet<>();

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Result [nAdded=").append(nAdded)
					.append(", nRemoved=").append(nRemoved)
					.append(", nAttributeChanged=").append(nAttributeChanged)
					.append("]");
			return builder.toString();
		}
	}
	
	private final Set<String> ignoreAttributeNames;
	
	XmlDiffer(List<String> ignoreAttributeNames) {
		this.ignoreAttributeNames = new HashSet<>(ignoreAttributeNames);
	}
	
	private static String childrenNames(Map<String, List<XmlElement>> ambiguousPaths, XmlElement element) {
		StringBuilder buf = new StringBuilder();
		String sep = CHILDREN_SEPARATOR;
		LinkedList<XmlElement> toAnalyze = new LinkedList<>(element.children);
		
		while (!toAnalyze.isEmpty()) {
			XmlElement child = toAnalyze.removeFirst();
			buf.append(sep).append(child.name);
			if (ambiguousPaths.containsKey(child.path)) {
				toAnalyze.addAll(child.children);
			}
		}
		return buf.toString();
	}

	private static void findAmbiguousPaths(Map<String, List<XmlElement>> ambiguousPaths, Iterable<XmlElement> elements) {
		for (XmlElement element : elements) {
			String key = element.path;
			List<XmlElement> list = ambiguousPaths.get(key);
			if (list == null) {
				ambiguousPaths.put(key, list = new ArrayList<>());
			}
			list.add(element);
		}
		
		for (Iterator<List<XmlElement>> i = ambiguousPaths.values().iterator(); i.hasNext(); ) {
			if (i.next().size() == 1) {
				i.remove();
			}
		}
	}
	
	private static void makeUniquePaths(Map<String, List<XmlElement>> ambiguousPaths, Iterable<XmlElement> elements, StringCacheInterface stringCache) {
		for (XmlElement element : elements) {
			element.uniquePath = stringCache.cache(element.path);
			if (ambiguousPaths.containsKey(element.path)) {
				element.uniquePath = stringCache.cache(element.path + childrenNames(ambiguousPaths, element));
			}
		}
	}
	
	/**
	 * Processes the two XmlStruct instances and returns all changes
	 * @param base will contain removed elements, if any
	 * @param mod will contain added elements if any
	 * @return all added and removed elements
	 */
	public Result diff(XmlStruct base, XmlStruct mod) {
		if (base.stringCache == null || base.stringCache != mod.stringCache) {
			throw new IllegalStateException("Invalid StringCache state. Can only diff documents created with same lexer instance");
		}
		Result result = new Result();
		StringCacheInterface stringCache = base.stringCache;
		resetDiffState(base);
		resetDiffState(mod);
		
		base.isBase = Boolean.TRUE;
		mod.isBase = Boolean.FALSE;

		Map<String, List<XmlElement>> ambiguousPaths = new HashMap<>();
		result.ambiguousPaths = ambiguousPaths;
		if (base.root().path.equals(mod.root().path)) { // Attention: don't use pathsEqual(), StringCache might get cleared
			findStructualChanges(base, mod, result, stringCache);
			markStructuralChanges(mod, /*isAddRun*/ true, result);
			markStructuralChanges(base, /*isAddRun*/ false, result);
			findAttributeChanges(mod, result);
			
			if (printMissingDiscriminators && !result.missingDiscriminators.isEmpty()) {
				System.out.println("There are attribute changes on ambiguous paths. Printing suggested discriminators to fix that.");
				for (String s : result.missingDiscriminators) {
					System.out.println(s);
				}
			}
		} else { // different roots. what the heck...! let's not waste time using the quadratic complexity diffing algorithm, we know the result already..
			result.nAdded = 1;
			result.nRemoved = 1;
			base.root().nStructureChanged = 1;
			mod.root().nStructureChanged = 1;
			for (XmlElement element : base.elements) {
				element.structureDiff = DiffState.parentChanged;
			}
			for (XmlElement element : mod.elements) {
				element.structureDiff = DiffState.parentChanged;
			}
			base.root().structureDiff = DiffState.changed;
			mod.root().structureDiff = DiffState.changed;
		}
		
		/*
		 * when two diffs run concurrently, we will fail here:
		 */
		assert result.nAdded == mod.root().nStructureChanged;
		assert result.nRemoved == base.root().nStructureChanged;
		assert result.nAttributeChanged == mod.root().nAttributeChanged;
		assert result.nAttributeChanged == base.root().nAttributeChanged;
		
		return result;
	}
	
	protected void findStructualChanges(XmlStruct base, XmlStruct mod, Result result, StringCacheInterface stringCache) {
		Map<String, List<XmlElement>> ambiguousPaths = new HashMap<>();
		result.ambiguousPaths = ambiguousPaths;
		findAmbiguousPaths(ambiguousPaths, base.elements);
		findAmbiguousPaths(ambiguousPaths, mod.elements);
		makeUniquePaths(ambiguousPaths, base.elements, stringCache);
		makeUniquePaths(ambiguousPaths, mod.elements, stringCache);
		
		if (base.elements.size() <= mod.elements.size()) {
			findStructuralChanges(base, new ArrayList<>(mod.elements), stringCache);
		} else {
			findStructuralChanges(mod, new ArrayList<>(base.elements), stringCache);
		}
	}

	protected static boolean pathsEqual(String left, String right, StringCacheInterface stringCache) {
		return stringCache.equal(left, right);
	}
	
	private void resetDiffState(XmlStruct xml) {
		for (XmlElement element : xml.elements) {
			element.nStructureChanged = 0;
			element.nAttributeChanged = 0;
			element.structureDiff = DiffState.unchanged;
			element.attributeDiff = DiffState.unchanged;
			element.selectedAttributes = filterAttributes(element, ignoreAttributeNames);
			resetPeer(element);
		}
	}
	
	protected void resetPeer(XmlElement element) {
		element.peer = null;
	}
	
	private void findStructuralChanges(XmlStruct xml, List<XmlElement> lookup, StringCacheInterface stringCache) {
		for (XmlElement element : xml.elements) {
			XmlElement peer = find(element, lookup, stringCache);
			if (peer != null) {
				element.peer = peer;
				peer.peer = element;
			}
		}
	}
	
	private void markStructuralChanges(XmlStruct xml, boolean isAddRun, Result result) {
		for (XmlElement element : xml.elements) {
			/* 
			 * if the peer is null, then it's a structural change, otherwise, it might be an attribute change - handled later!
			 */
			if (element.peer == null) {
				markChanged(element, isAddRun, result);
			}
			element.uniquePath = null;
		}
	}
	
	private void findAttributeChanges(XmlStruct xml, Result result) {
		for (XmlElement element : xml.elements) {
			if (element.peer != null) {
				assert element.selectedAttributes != null && element.peer.selectedAttributes != null;
				/*
				 * we're using AbstractMap.equals() on the selectedAttributes which has ignoredAttributeNames removed.
				 */
				if (!element.selectedAttributes.equals(element.peer.selectedAttributes)) {
					if (printMissingDiscriminators) {
						if (result.ambiguousPaths.containsKey(element.path)) {
							for (String key : element.selectedAttributes.keySet()) {
								result.missingDiscriminators.add(XmlLexer.LEVEL_SEPARATOR + element.rawPath + XmlLexer.ATTRIBUTE_SEPARATOR + key);
							}
						}
					}
					markAttributesChanged(element);
					markAttributesChanged(element.peer);
					result.nAttributeChanged++;
				}
			}
		}
	}
	
	private XmlElement find(XmlElement element, List<XmlElement> lookup, StringCacheInterface stringCache) {
		for (Iterator<XmlElement> i = lookup.iterator(); i.hasNext(); ) {
			XmlElement e = i.next();
			if (pathsEqual(element.uniquePath, e.uniquePath, stringCache)) {
				i.remove();
				return e;
			}
		}
		
		return null;
	}
	
	private static void markChanged(XmlElement element, boolean isAddRun, Result result) {
		if (element.parent != null && (element.parent.structureDiff == DiffState.changed || element.parent.structureDiff == DiffState.parentChanged)) {
			element.structureDiff = DiffState.parentChanged;
			return; // don't include, parent already included
		}
		element.structureDiff = DiffState.changed;
		element.nStructureChanged++;
		while (element.parent != null) {
			element.parent.nStructureChanged++;
			element = element.parent;
		}
		if (isAddRun) {
			result.nAdded++;
		} else {
			result.nRemoved++;
		}
	}
	
	private static void markAttributesChanged(XmlElement element) {
		element.attributeDiff = DiffState.changed;
		element.nAttributeChanged++;
		while (element.parent != null) {
			element.parent.nAttributeChanged++;
			element = element.parent;
		}
	}
	
	private static SortedMap<String, String> filterAttributes(XmlElement element, Set<String> ignoreAttributeNames) {
		if (filterAttributes0(element, /*selectedAttributes*/ null, ignoreAttributeNames)) {
			SortedMap<String, String> selectedAttributes = new TreeMap<>();
			filterAttributes0(element, selectedAttributes, ignoreAttributeNames);
			return selectedAttributes;
		}
		return element.attributes;
	}
	
	private static boolean filterAttributes0(XmlElement element, Map<String, String> selectedAttributes, Set<String> ignoreAttributeNames) {
		for (Map.Entry<String, String> attribute : element.attributes.entrySet()) {
			String key = attribute.getKey();
			String value = attribute.getValue();
			if (isIgnored(key, value, element.rawPath, ignoreAttributeNames)) {
				if (selectedAttributes == null) {
					return true; // just checking: found an attribute to filter.
				}
				continue;
			}
			if (selectedAttributes == null) {
				continue; // just checking 
			}
			selectedAttributes.put(key, value);
		}
		
		return false; // just checking: all attributes the same
	}
	
	private static boolean isIgnored(String key, String value, String rawPath, Set<String> ignoreAttributeNames) {
		// we ignore empty attribute values
		return value.isEmpty() || AttributeSpecifier.containsKey(ignoreAttributeNames, key, rawPath);
	}
}
