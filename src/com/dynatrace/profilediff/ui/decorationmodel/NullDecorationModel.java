package com.dynatrace.profilediff.ui.decorationmodel;

import javax.swing.Icon;

@SuppressWarnings("rawtypes")
public class NullDecorationModel implements DecorationModel  {
	
	private NullDecorationModel() {
	}
	
	private static final NullDecorationModel INSTANCE = new NullDecorationModel();
	
	@SuppressWarnings("unchecked")
	public static <T> DecorationModel<T> getInstance() {
		return INSTANCE;
	}

	@Override
	public Icon getIcon(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		return null;
	}
}
