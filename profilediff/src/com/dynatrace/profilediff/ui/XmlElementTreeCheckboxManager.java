package com.dynatrace.profilediff.ui;

import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.profilediff.XmlElement;

class XmlElementTreeCheckboxManager extends TreeCheckboxManager {

	private final UserObjectLogic userObjectLogic;

	XmlElementTreeCheckboxManager(UserObjectLogic userObjectLogic) {
		this.userObjectLogic = userObjectLogic;
	}

	@Override
	protected ModelCheckbox getCheckboxState(Object element) {
		return userObjectLogic.getModelCheckbox(getCorrectPeer((XmlElement) element));
	}

	@Override
	protected void setCheckboxState(Object o, ModelCheckbox value) {
		userObjectLogic.setModelCheckbox(getCorrectPeer((XmlElement) o), value);
	}
	
	private static XmlElement getCorrectPeer(XmlElement element) {
		/*
		 * for attribute checkbox, we need to to store only one checkbox! (insertion)
		 */
		return element.peer != null && element.isDeletion() ? element.peer : element;
	}
}
