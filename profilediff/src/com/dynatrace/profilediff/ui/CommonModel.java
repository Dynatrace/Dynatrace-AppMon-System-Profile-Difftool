package com.dynatrace.profilediff.ui;

import java.util.List;

import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.XmlUtil;

class CommonModel {

	static interface ChangeItemInterface {
		boolean containsAttributeChanges();
		boolean containsInsertions();
		boolean containsDeletions();
		boolean isChangesOnly();
		boolean isTwoWayModel();
	}
	
	static void collectChangedElements(XmlStruct xml, ChangeItemInterface item, List<XmlElement> deletions, List<XmlElement> insertions, List<XmlElement> attrChanges) {
		for (XmlElement element : xml.elements) {
			if (!XmlUtil.isFilteredVisible(element)) {
				continue;
			}
			if (element.hasDirectStructureChange() && element.isDeletion() && item.containsDeletions()) {
				deletions.add(element);
			} else if (element.hasDirectStructureChange() && element.isInsertion() && item.containsInsertions()) {
				insertions.add(element);
			} else if (element.hasDirectAttributeChange() && (element.isInsertion() || item.isTwoWayModel()) && item.containsAttributeChanges()) {
				attrChanges.add(element);
			}
		}
	}
}
