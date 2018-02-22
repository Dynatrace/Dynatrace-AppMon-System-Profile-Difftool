package com.dynatrace.profilediff.togglemodel;

public interface ToggleModel<T> {
	
	void toggle(T element, boolean selected) throws ToggleVeto;
	
	void setSelectionInterface(SelectionInterface<T> selectionInterface);
	
	interface SelectionInterface<T> {
		
		void setSelected(T element, boolean selected);
		
		boolean isSelected(T element);
	}
}
