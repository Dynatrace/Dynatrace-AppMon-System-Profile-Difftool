package com.dynatrace.profilediff;

public class XmlMerger3FactoryImpl {
	
	public static XmlMerger3Factory newSafe() {
		return XmlMerger3::new;
	}
	
	public static XmlMerger3Factory newFast() {
		return FastXmlMerger3::new;
	}
}
