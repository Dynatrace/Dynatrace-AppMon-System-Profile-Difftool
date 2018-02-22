package com.dynatrace.profilediff.ui.decorationmodel;

import javax.swing.Icon;

public interface DecorationModel<T> {

	Icon getIcon(T element);
	
	String getText(T element);
}
