package com.dynatrace.profilediff.ui;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTextArea;

class TextTools {
	
	private static final Font FONT = new Font("Courier", Font.PLAIN, 12);
	private static final Insets INSETS = new Insets(2, 2, 2, 2);
    static final Object LF = System.getProperty("line.separator");

	static JTextArea createTextArea() {
		JTextArea area = new JTextArea();
		area.setMargin(INSETS);
		area.setFont(FONT);
		return area;
	}

	static String createLineNumberString(int min, int max) {
		StringBuilder buf = new StringBuilder();
		for (int i = min; i <= max; i++) {
			buf.append(i).append(LF);
		}
		return buf.toString();
	}
}
