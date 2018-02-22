package com.dynatrace.profilediff;

import java.util.Set;

class AttributeSpecifier {
	
	static String stripKey(String specifier, String rawPath) {
		String name = specifier;
		int i = specifier.indexOf(XmlLexer.ATTRIBUTE_SEPARATOR);
		if (i != -1) {
			String pathSpecifier = specifier.substring(0, i);
			if (!matches(pathSpecifier, rawPath)) {
				return null;
			}
			return specifier.substring(i + 1);
		}
		return name;
	}
	
	static boolean matches(String pathSpecifier, String rawPath) {
		if (pathSpecifier.startsWith(XmlLexer.LEVEL_SEPARATOR)) {
			if (!rawPath.equals(pathSpecifier.substring(XmlLexer.LEVEL_SEPARATOR.length()))) { // complete path match
				return false;
			}
		} else  if (!rawPath.endsWith(pathSpecifier)) { // subpath match
			return false;
		}
		return true;
	}
	
	static boolean containsKey(Set<String> specifiers, String key, String rawPath) {
		if (specifiers.contains(key)) {
			return true; // quicker
		}
		for (String specifier : specifiers) {
			if (key.equals(stripKey(specifier, rawPath))) {
				return true;
			}
		}
		return false;
	}
	
	static boolean containsMatch(Set<String> pathSpecifiers, String rawPath) {
		if (pathSpecifiers.contains(XmlLexer.LEVEL_SEPARATOR + rawPath)) {
			return true; // quicker
		}
		for (String pathSpecifier : pathSpecifiers) {
			if (matches(pathSpecifier, rawPath)) {
				return true;
			}
		}
		return false;
	}
}
