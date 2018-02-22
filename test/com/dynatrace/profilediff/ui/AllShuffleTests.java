package com.dynatrace.profilediff.ui;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	  XmlDifferSensorRulesShuffleTest.class
	, XmlDifferSystemProfileShuffleTest.class
	, XmlDifferDepthShuffleTest.class
})
public class AllShuffleTests {
}
