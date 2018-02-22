package com.dynatrace.profilediff;

public class XmlDifferFactoryImpl {
	
	public static XmlDifferFactory newDefault() {
		return XmlDiffer::new;
	}
	
	public static XmlDifferFactory newStringMetric() {
		return StringMetricXmlDiffer::new;
	}
	
	public static XmlDifferFactory newSortingStringMetric() {
		return SortingStringMetricXmlDiffer::new;
	}
}
