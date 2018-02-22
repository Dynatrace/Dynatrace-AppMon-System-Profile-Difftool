package com.dynatrace.profilediff;

import java.util.List;

import com.dynatrace.profilediff.StringMetricXmlDiffer.MetricResolver;

@FunctionalInterface
public interface StringMetricXmlDifferFactory {
	
	XmlDiffer create(List<String> ignoreAttributeNames, MetricResolver metricResolver, int threshold);
	
}
