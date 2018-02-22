package com.dynatrace.profilediff;

import java.util.List;

@FunctionalInterface
public interface XmlDifferFactory {
	
	XmlDiffer create(List<String> ignoreAttributeNames);
	
}
