package com.dynatrace.profilediff.ui;

import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;

import com.dynatrace.common.swing.ui.TreeCheckboxManager.ModelCheckbox;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.ui.XmlStructTreeModel.ModelChildren;

class UserObjectLogic {
	
	private static final int MODEL_CHILDREN = 0;
	private static final int MODEL_CHECKBOX = 1;
	private static final int TEXT_HIGHLIGHT = 2;
	private static final int MAX = 3;
	
	private UserObjectLogic() {
	}
	
	static UserObjectLogic newDefault() {
		return new UserObjectLogic();
	}

	private Object get(XmlElement element, int index) {
		return element.userObjects == null ? null : element.userObjects[index];
	}

	private Object[] set(XmlElement element) {
		if (element.userObjects == null) {
			element.userObjects = new Object[MAX];
		}
		return element.userObjects;
	}
	
	ModelChildren getModelChildren(XmlElement element) {
		return (ModelChildren) get(element, MODEL_CHILDREN);
	}
	
	void setModelChildren(XmlElement element, Object value) {
		set(element)[MODEL_CHILDREN] = value;
	}
	
	ModelCheckbox getModelCheckbox(XmlElement element) {
		return (ModelCheckbox) get(element, MODEL_CHECKBOX);
	}
	
	void setModelCheckbox(XmlElement element, Object value) {
		set(element)[MODEL_CHECKBOX] = value;
	}
	
	Highlighter.Highlight getTextHighlight(XmlElement element) {
		return (Highlight) get(element, TEXT_HIGHLIGHT);
	}
	
	void setTextHighlight(XmlElement element, Object value) {
		set(element)[TEXT_HIGHLIGHT] = value;
	}
	
	void clear() {
		/* 
		 * nothing to do since this default impl uses XmlElement userObject[] directly and therefore,
		 * the state is thrown away with the XmlElement.
		 * If we'd store state in a map, we'd need to clear it now.
		 */
	}
}
