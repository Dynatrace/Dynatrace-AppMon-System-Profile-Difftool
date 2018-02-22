package com.dynatrace.common.swing.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@FunctionalInterface
public interface DocumentAdapter {
	
	public void changed(DocumentEvent e);
	
	public static DocumentListener adapt(DocumentAdapter adapter) {
		return new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				adapter.changed(e);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				adapter.changed(e);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				adapter.changed(e);
			}
		};
	}
}
