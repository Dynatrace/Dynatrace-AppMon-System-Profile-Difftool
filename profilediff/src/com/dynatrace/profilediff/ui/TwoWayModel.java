package com.dynatrace.profilediff.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.ui.SpecialTableCell.Kind;

class TwoWayModel extends CommonModel {
	
	static enum ChangeItem implements ChangeItemInterface {
		  changes("Show all changes")
		, structural("Show insertions/deletions")
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
			return this == changes || this == attributes || this == normal;
		}
		
		@Override
		public boolean containsInsertions() {
			return containsStructuralChanges();
		}
		
		@Override
		public boolean containsDeletions() {
			return containsStructuralChanges();
		}
		
		private boolean containsStructuralChanges() {
			return this == changes || this == structural || this == normal;
		}
		
		@Override
		public boolean isChangesOnly() {
			return this != normal;
		}
		
		@Override
		public boolean isTwoWayModel() {
			return true;
		}
	}
	
	static XmlStructTreeModel createTreeModel(ChangeItem item, XmlStruct xml, UserObjectLogic userObjectLogic) {
		switch (item) {
			case changes: return XmlStructTreeModelFactory.normalChangesOnly(xml, userObjectLogic);
			case structural: return XmlStructTreeModelFactory.normalStructuralChangesOnly(xml, userObjectLogic);
			case attributes: return XmlStructTreeModelFactory.normalAttributeChangesOnly(xml, userObjectLogic);
			case normal: return XmlStructTreeModelFactory.normal(xml, userObjectLogic);
		}
		return null;
	}

	
	static final Object[] ATTR_COLS =  { "Name", "Value" };
	static final Object[][] ATTR_EMPTY = {};
	
	private static void enumAttributesCompare(XmlElement element, boolean changesOnly, boolean allAttributes, boolean includeChildren, List<Object[]> result) {
		boolean headerAdded = false;
		boolean compare = true;
		
		SortedMap<String, String> attributes = allAttributes || !compare ? element.attributes : element.selectedAttributes;
		assert attributes != null;
		
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			Object[] row = null;
			
			String peerValue = element.peer == null ? null : element.peer.attributes.get(key);
			if (peerValue == null) {
				SpecialTableCell.Kind kind = element.isInsertion() ? Kind.AttributeAdded : Kind.AttributeRemoved;
				row = new Object[] {
						new SpecialTableCell(key, kind, element)
					  , new SpecialTableCell(value, kind, element)
				};
			} else if (!peerValue.equals(value)) {
				SpecialTableCell.Kind kind = Kind.AttributeValueDifferent;
				 row = new Object[] { 
						new SpecialTableCell(key, kind, element)
					  , new SpecialTableCell(value, kind, element)
				};
			} else if (!changesOnly) {
				SpecialTableCell.Kind kind = Kind.AttributeValueEqual;
				 row = new Object[] { 
						new SpecialTableCell(key, kind, element)
					  , new SpecialTableCell(value, kind, element)
				};
			}
			if (row != null) {
				if (!headerAdded) {
					headerAdded = true;
					SpecialTableCell.Kind kind = Kind.ElementHeader;
					result.add(new Object[] { 
						new SpecialTableCell(element.name, kind, element)
						, new SpecialTableCell(element.path, kind, element) 
					});
				}
				result.add(row);
			}
		}
		
		if (includeChildren) {
			for (XmlElement child : element.children) {
				enumAttributesCompare(child, changesOnly, allAttributes, includeChildren, result);
			}
		}
	}
	
	private static void enumAttributesNoCompare(XmlElement element, boolean changesOnly, boolean allAttributes, boolean includeChildren, List<Object[]> result) {
		boolean headerAdded = false;
		boolean compare = false;
		
		SortedMap<String, String> attributes = allAttributes || !compare ? element.attributes : element.selectedAttributes;
		assert attributes != null;
		
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			Object[] row = {
					key
					, value 
			};
			if (!headerAdded) {
				headerAdded = true;
				SpecialTableCell.Kind kind = Kind.ElementHeader;
				result.add(new Object[] { 
						new SpecialTableCell(element.name, kind, element)
						, new SpecialTableCell(element.path, kind, element) 
				});
			}
			result.add(row);
		}
		
		if (includeChildren) {
			for (XmlElement child : element.children) {
				enumAttributesNoCompare(child, changesOnly, allAttributes, includeChildren, result);
			}
		}
	}
	
	static Object[][] enumAttributes(XmlElement element, boolean compare, boolean changesOnly, boolean allAttributes, boolean includeChildren) {
		List<Object[]> result = new ArrayList<>();
		if (compare) {
			enumAttributesCompare(element, changesOnly, allAttributes, includeChildren, result);
		} else {
			enumAttributesNoCompare(element, changesOnly, allAttributes, includeChildren, result);
		}
		return result.toArray(new Object[result.size()][]);
	}
}
