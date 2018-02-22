package com.dynatrace.profilediff.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import com.dynatrace.profilediff.StringMetricXmlDiffer.MetricResolver;
import com.dynatrace.profilediff.StringMetricXmlDifferFactory;
import com.dynatrace.profilediff.StringMetricXmlDifferFactoryImpl;
import com.dynatrace.profilediff.XmlDiffer;

public class ShuffleTestBase extends ModelTestBase {
	
	@Parameters(name = "{1} run #{2}")
	public static Collection<Object[]> parameters() {
		List<Object[]> params = new ArrayList<>();
		final int NORMAL_RUNS = 1;
		addParams(params, StringMetricXmlDifferFactoryImpl.newDefault(), "StringMetricXmlDiffer", NORMAL_RUNS);
		addParams(params, StringMetricXmlDifferFactoryImpl.newSorting(), "SortingStringMetricXmlDiffer", NORMAL_RUNS);
		addParams(params, nonBinarySearchSortingStringMetricDifferFactory, "NonBinarySearchSortingStringMetricXmlDiffer", NORMAL_RUNS);
		final int SHUFFLE_RUNS = 50;
		addParams(params, shufflingStringMetricDifferFactory, "ShufflingStringMetricXmlDiffer", SHUFFLE_RUNS);
		addParams(params, shufflingSortingStringMetricDifferFactory, "ShufflingSortingStringMetricXmlDiffer", SHUFFLE_RUNS);
		return params;
	}
	
	private static void addParams(List<Object[]> params, StringMetricXmlDifferFactory stringMetricXmlDifferFactory, String name, int max) {
		for (int i = 1; i <= max; i++) {
			Object[] param = { stringMetricXmlDifferFactory, name, i };
			params.add(param);
		}
	}
	
	private final StringMetricXmlDifferFactory stringMetricXmlDifferFactory;

	protected ShuffleTestBase(StringMetricXmlDifferFactory stringMetricXmlDifferFactory, String name, int run) {
		this.stringMetricXmlDifferFactory = stringMetricXmlDifferFactory;
	}
	
	protected XmlDiffer newStringMetricXmlDiffer(List<String> ignoreAttributeNames, MetricResolver metricResolver, int threshold) {
		return stringMetricXmlDifferFactory.create(ignoreAttributeNames, metricResolver, threshold);
	}
}
