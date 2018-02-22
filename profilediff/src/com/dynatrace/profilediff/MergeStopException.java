package com.dynatrace.profilediff;

@SuppressWarnings("serial")
public class MergeStopException extends RuntimeException {
	
	private final XmlStruct merged;
	
	MergeStopException(XmlStruct merged, String message) {
		super(message);
		this.merged = merged;
	}

	public XmlStruct getMerged() {
		return merged;
	}
}
