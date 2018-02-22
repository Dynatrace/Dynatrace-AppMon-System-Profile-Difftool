package com.dynatrace.profilediff.ui;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	  OneWayModelTest.class
	, OneWayModelFilterDeletionTest.class
	, OneWayModelFilterInsertionTest.class
	, OneWayModelFilterAttrChangeTest.class
	, TwoWayModelTest.class
	, TwoWayModelFilterDeletionTest.class
	, TwoWayModelFilterInsertionTest.class
	, TwoWayModelFilterAttrChangeTest.class
})
public class AllModelTests {
}

