package com.dynatrace.profilediff;

public class StringMetricXmlDifferFactoryImpl {
	
	public static StringMetricXmlDifferFactory newDefault() {
		return StringMetricXmlDiffer::new;
	}
	
	public static StringMetricXmlDifferFactory newSorting() {
		return SortingStringMetricXmlDiffer::new;
	}
}
