package com.dynatrace.profilediff;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dynatrace.profilediff.togglemodel.SystemProfileToggleModelTest;
import com.dynatrace.profilediff.ui.AllModelTests;
import com.dynatrace.profilediff.ui.AllShuffleTests;
import com.dynatrace.profilediff.ui.decorationmodel.HierarchicalResourceDecorationModelTest;

@RunWith(Suite.class)
@SuiteClasses({
	AttributeSpecifierTest.class
	, StringMetricXmlDifferTest.class
	, XmlParserTest.class
	, XmlLexerTest.class
	, XmlDifferTest.class
	, XmlDifferOrderTest.class
	, XmlMergerTest.class
	, XmlMerger2Test.class
	, XmlMerger3Test.class
	, XmlMergerSystemProfilesTest.class
	, XmlMergerBuildFilesTest.class
	, AllShuffleTests.class
	, AllModelTests.class
	, SystemProfileToggleModelTest.class
	, HierarchicalResourceDecorationModelTest.class
	, AllTests.PrintStat.class
})
public class AllTests {
	
	public static class PrintStat {
		@Test
		public void printStat() {
			TestBase.stringCache.printStats("stringCache");
		}
	}
}
