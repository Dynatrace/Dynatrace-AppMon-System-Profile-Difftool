package com.dynatrace.profilediff.togglemodel;

@SuppressWarnings("rawtypes")
public class NullToggleModel implements ToggleModel {
	
	private static final NullToggleModel INSTANCE = new NullToggleModel();
	
	@SuppressWarnings("unchecked")
	public static <T> ToggleModel<T> getInstance() {
		return INSTANCE;
	}
	
	private NullToggleModel() {
	}

	@Override
	public void toggle(Object element, boolean selected) throws ToggleVeto {
	}

	@Override
	public void setSelectionInterface(SelectionInterface selectionInterface) {
	}
}
