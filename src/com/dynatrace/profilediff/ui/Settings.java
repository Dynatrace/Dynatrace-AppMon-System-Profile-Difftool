package com.dynatrace.profilediff.ui;

import com.dynatrace.common.conf.PropertiesInjector;
import com.dynatrace.common.conf.ResourceLoader;

class Settings {
	
	String[] discriminatorAttributes = {};
	String[] ignoreAttributes = {};
	
	String[] levenshteinPaths = {};
	
	boolean initialSelectionDeletion;
	boolean initialSelectionInsertion;
	boolean initialSelectionAttribute;
	
	boolean useStringMetricDiffer;
	boolean useSortingStringMetricDiffer;
	boolean useLevenshteinMetricGlobally;
	boolean useEqualityMetricGlobally;
	boolean useFastMerger;
	boolean useStringCache;
	boolean useSystemProfileToggleModel;
	boolean useHierarchicalResourceDecorationModel;
	int levenshteinThreshold;
	
	String scheme;
	String[] imageExt = {};
	int pathLevelThreshold;

	static Settings load() {
		return PropertiesInjector.getDefault().injectProperties(new Settings(), "settings.properties", ResourceLoader.getFileSystemOverride());
	}
}
