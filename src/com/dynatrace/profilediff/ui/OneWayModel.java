package com.dynatrace.profilediff.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.ui.SpecialTableCell.Kind;

class OneWayModel extends CommonModel {
	
	static enum ChangeItem implements ChangeItemInterface {
		  changes("Show all changes")
		, deletions("Show deletions")
		, insertions("Show insertions")
		, structural("Show insertions and deletions")
		, additions("Show insertions and attributes")
		, attributes("Show attributes changes")
		, normal("Show whole document")
	;
		
		private final String text;
		
		ChangeItem(String text) {
			this.text = text;
		}
		
		@Override
		public String toString() {
			return text;
		}
		
		@Override
		public boolean containsAttributeChanges() {
			return this == changes || this == attributes || this == additions || this == normal;
		}
		
		@Override
		public boolean containsInsertions() {
			return this == changes || this == insertions || this == additions|| this == structural || this == normal;
		}
		
		@Override
		public boolean containsDeletions() {
			return this == changes || this == deletions || this == structural || this == normal;
		}
		
		@Override
		public boolean isChangesOnly() {
			return this != normal;
		}

		@Override
		public boolean isTwoWayModel() {
			return false;
		}
	}
	
	static XmlStructTreeModel createTreeModel(ChangeItem item, XmlStruct modXml, UserObjectLogic userObjectLogic) {
		switch (item) {
			case changes: return XmlStructTreeModelFactory.combinedChangesOnly(modXml, userObjectLogic);
			case attributes: return XmlStructTreeModelFactory.combinedAttributeChangesOnly(modXml, userObjectLogic);
			case deletions: return XmlStructTreeModelFactory.combinedDeletionsOnly(modXml, userObjectLogic);
			case insertions: return XmlStructTreeModelFactory.combinedInsertionsOnly(modXml, userObjectLogic);
			case additions: return XmlStructTreeModelFactory.combinedAdditionsOnly(modXml, userObjectLogic);
			case structural: return XmlStructTreeModelFactory.combinedStructuralChangesOnly(modXml, userObjectLogic);
			case normal: return XmlStructTreeModelFactory.combined(modXml, userObjectLogic);
		}
		return null;
	}
	
	private static SortedSet<String> getAllKeys(SortedMap<String, String> attributes, SortedMap<String, String> peerAttributes) {
		if (peerAttributes == null) {
			return (SortedSet<String>) attributes.keySet(); // SortedMap.keySet() does not return SortedSet 
		} else {
			SortedSet<String> allKeys;
			allKeys = new TreeSet<>();
			allKeys.addAll(attributes.keySet());
			allKeys.addAll(peerAttributes.keySet());
			return allKeys;
		}
	}
	
	private static void enumAttributesCompare(XmlElement element, boolean changesOnly, boolean allAttributes, boolean includeChildren, List<Object[]> result, boolean showPeerAttributes) {
		if (element.peer == null && !showPeerAttributes) {
			return;
		}
		boolean compare = true;
		boolean headerAdded = false;
		SortedMap<String, String> attributes = allAttributes || !compare ? element.attributes : element.selectedAttributes;
		SortedMap<String, String> peerAttributes = element.peer == null ? null : allAttributes || !compare ? element.peer.attributes : element.peer.selectedAttributes;

		assert attributes != null;
		
		SortedSet<String> allKeys = getAllKeys(attributes, peerAttributes);
		
		for (String key : allKeys) {
			String value = attributes.get(key);
			Object[] row = null;
			
			String peerValue = peerAttributes == null ? null : peerAttributes.get(key);
			if (peerValue == null || value == null) {
				SpecialTableCell.Kind kind = peerValue == null && element.isInsertion() ? Kind.AttributeAdded : Kind.AttributeRemoved;
				row = new Object[] {
						new SpecialTableCell(key, kind, element)
					  , value == null ? null : new SpecialTableCell(value, kind, element)
					  , peerValue == null ? null : new SpecialTableCell(peerValue, kind, element)
				};
			} else if (!peerValue.equals(value)) {
				SpecialTableCell.Kind kind = Kind.AttributeValueDifferent;
				row = new Object[] { 
						new SpecialTableCell(key, kind, element)
					  , new SpecialTableCell(value, kind, element)
					  , new SpecialTableCell(peerValue, kind, element)
				};
			} else if (!changesOnly) {
				SpecialTableCell.Kind kind = Kind.AttributeValueEqual;
				row = new Object[] { 
						new SpecialTableCell(key, kind, element)
					  , new SpecialTableCell(value, kind, element)
					  , new SpecialTableCell(peerValue, kind, element)
				};
			}
			if (row != null) {
				if (!headerAdded) {
					headerAdded = true;
					SpecialTableCell.Kind kind = Kind.ElementHeader;
					result.add(new Object[] { 
						new SpecialTableCell(element.name, kind, element)
						, new SpecialTableCell(element.path, kind, element) 
						, new SpecialTableCell("", kind, element) 
					});
				}
				result.add(row);
			}
		}
		
		if (includeChildren) {
			for (XmlElement child : element.children) {
				enumAttributesCompare(child, changesOnly, allAttributes, includeChildren, result, showPeerAttributes);
			}
		}
	}
	private static void enumAttributesNoCompare(XmlElement element, boolean changesOnly, boolean allAttributes, boolean includeChildren, List<Object[]> result, boolean showPeerAttributes) {
		if (element.peer == null && !showPeerAttributes) {
			return;
		}
		boolean compare = false;
		boolean headerAdded = false;
		SortedMap<String, String> attributes = allAttributes || !compare ? element.attributes : element.selectedAttributes;
		SortedMap<String, String> peerAttributes = element.peer == null ? null : allAttributes || !compare ? element.peer.attributes : element.peer.selectedAttributes;
		
		assert attributes != null;
		SortedSet<String> allKeys = getAllKeys(attributes, peerAttributes);
		
		for (String key : allKeys) {
			String value = attributes.get(key);
			Object[] row = {
					key
					, value
					, null
			};
			if (!headerAdded) {
				headerAdded = true;
				SpecialTableCell.Kind kind = Kind.ElementHeader;
				result.add(new Object[] { 
						new SpecialTableCell(element.name, kind, element)
						, new SpecialTableCell(element.path, kind, element) 
						, new SpecialTableCell("", kind, element) 
				});
			}
			result.add(row);
		}
		
		if (includeChildren) {
			for (XmlElement child : element.children) {
				enumAttributesNoCompare(child, changesOnly, allAttributes, includeChildren, result, showPeerAttributes);
			}
		}
	}
	
	static Object[][] enumAttributes(XmlElement element, boolean compare, boolean changesOnly, boolean allAttributes, boolean includeChildren) {
		List<Object[]> result = new ArrayList<>();
		if (compare) {
			enumAttributesCompare(element, changesOnly, allAttributes, includeChildren, result, /*showPeerAttributes*/ element.peer == null);
		} else {
			enumAttributesNoCompare(element, changesOnly, allAttributes, includeChildren, result, /*showPeerAttributes*/ element.peer == null);
		}
		return result.toArray(new Object[result.size()][]);
	}
	
	static final Object[] ATTR_COLS =  { "Name", "New value", "Old value" };
	static final Object[][] ATTR_EMPTY = {};
}
