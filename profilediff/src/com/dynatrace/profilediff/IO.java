package com.dynatrace.profilediff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class IO {
	
	public static String asString(String[] lines) {
		return asString(lines, 1, lines.length);
	}
	
	private static String asString(String[] lines, int from, int to) {
		StringBuilder buf = new StringBuilder();
		if (from < 1) {
			from = 1;
		}
		if (to > lines.length) {
			to = lines.length;
		}
		for (int i = from; i <= to; i++) {
//			buf.append(lines[i - 1]).append("\r\n");
			buf.append(lines[i - 1]).append("\n");
		}
		return buf.toString();
	}

	public static String[] readLines(Reader reader) throws IOException {
		try (BufferedReader in = new BufferedReader(reader)) {
			List<String> result = new ArrayList<>();
			String line;
			while ((line = in.readLine()) != null) {
				result.add(line);
			}
			return result.toArray(new String[result.size()]);
		}
	}
}
