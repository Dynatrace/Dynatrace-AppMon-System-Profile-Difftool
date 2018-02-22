package com.dynatrace.profilediff;

@FunctionalInterface
public interface XmlMerger3Factory {
	
	XmlMerger3 create(XmlLexer lexer, XmlDiffer differ);
	
}
