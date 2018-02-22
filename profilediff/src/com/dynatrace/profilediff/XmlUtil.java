package com.dynatrace.profilediff;

public class XmlUtil {
	
	public static XmlElement traverseToChange(XmlElement element) {
		assert element.structureDiff == DiffState.parentChanged;
		while (element.parent != null) {
			if (element.structureDiff == DiffState.changed) {
				break;
			}
			element = element.parent;
		}
		assert element.structureDiff == DiffState.changed;
		return element;
	}
	
	static boolean isDescendant(XmlElement parent, XmlElement child) {
		for (XmlElement e = child.parent; e != null; e = e.parent) {
			if (e == parent) {
				return true;
			}
		}
		return false;
	}

	public static boolean isFilteredVisible(XmlElement element) {
		return element.visible;
	}

	public static void resetFilter(XmlStruct xml) {
		for (XmlElement element : xml.elements) {
			element.visible = true;
		}
	}

	public static void filter(XmlStruct xml, String criterion, boolean showParentPeers) {
		for (XmlElement element : xml.elements) {
			element.visible = false;
		}
		
		for (XmlElement element : xml.elements) {
			if (element.toString().contains(criterion) || element.getContent().toString().contains(criterion)) {
				for (XmlElement e = element; e != null; e = e.parent) {
					e.visible = true;
				}
				if (showParentPeers && element.parent != null) {
					for (XmlElement e = element.parent.peer; e != null; e = e.parent) {
						e.visible = true;
					}
				}
			}
		}
	}
}
