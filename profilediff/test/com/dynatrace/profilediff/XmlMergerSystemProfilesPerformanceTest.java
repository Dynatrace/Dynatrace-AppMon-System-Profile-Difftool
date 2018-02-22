package com.dynatrace.profilediff;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.dynatrace.profilediff.StringMetricXmlDiffer.MetricResolver;
import com.dynatrace.profilediff.lib.StringMetrics;

@Ignore
public class XmlMergerSystemProfilesPerformanceTest extends DifferMergerTestBase {

	@Parameters(name="{1}+{3}")
	public static Collection<Object[]> parameters() {
		/*
		 * just for performance tuning of threshold - using SafeMerger, since it does a lot more diffing, and comparing different thresholds
		 */
		return Arrays.asList(
				  new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newDefault(), "DefaultDiffer", -1, StringMetricXmlDifferFactoryImpl.newDefault() }
				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDiffer", -1, StringMetricXmlDifferFactoryImpl.newDefault() }
  				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDifferThreshold=5", 5, StringMetricXmlDifferFactoryImpl.newDefault() }
  				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDifferThreshold=100", 100, StringMetricXmlDifferFactoryImpl.newDefault() }
				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDiffer", -1, StringMetricXmlDifferFactoryImpl.newSorting() }
  				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDifferThreshold=5", 5, StringMetricXmlDifferFactoryImpl.newSorting() }
  				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDifferThreshold=100", 100, StringMetricXmlDifferFactoryImpl.newSorting() }

				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", shufflingStringMetricDifferBaseFactory, "ShufflingStringMetricDiffer", -1, shufflingStringMetricDifferFactory }
			    , new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", shufflingSortingStringMetricDifferBaseFactory, "ShufflingSortingStringMetricDiffer", -1, shufflingSortingStringMetricDifferFactory }
				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", nonBinarySearchSortingStringMetricDifferBaseFactory, "NonBinarySearchSortingStringMetricDiffer", -1, nonBinarySearchSortingStringMetricDifferFactory }

				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newDefault(), "DefaultDiffer", -1, StringMetricXmlDifferFactoryImpl.newDefault() }
				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDiffer", -1, StringMetricXmlDifferFactoryImpl.newDefault() }
				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDifferThreshold=5", 5, StringMetricXmlDifferFactoryImpl.newDefault() }
				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDifferThreshold=100", 100, StringMetricXmlDifferFactoryImpl.newDefault() }
				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDiffer", -1, StringMetricXmlDifferFactoryImpl.newSorting() }
				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDifferThreshold=5", 5, StringMetricXmlDifferFactoryImpl.newSorting() }
				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDifferThreshold=100", 100, StringMetricXmlDifferFactoryImpl.newSorting() }

				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", shufflingStringMetricDifferBaseFactory, "ShufflingStringMetricDiffer", -1, shufflingStringMetricDifferFactory }
			    , new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", shufflingSortingStringMetricDifferBaseFactory, "ShufflingSortingStringMetricDiffer", -1, shufflingSortingStringMetricDifferFactory }
				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", nonBinarySearchSortingStringMetricDifferBaseFactory, "NonBinarySearchSortingStringMetricDiffer", -1, nonBinarySearchSortingStringMetricDifferFactory }
		);
	}

	public XmlMergerSystemProfilesPerformanceTest(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold, StringMetricXmlDifferFactory stringMetricXmlDifferFactory) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
		this.stringMetricXmlDifferFactory = stringMetricXmlDifferFactory;
	}

	private final StringMetricXmlDifferFactory stringMetricXmlDifferFactory;

	static XmlMerger3.IncludeElementCallback callback(boolean add, boolean remove) {
		return new XmlMerger3.IncludeElementCallback() {
			@Override
			public boolean addElement(XmlElement element) {
				return add;
			}

			@Override
			public boolean removeElement(XmlElement element) {
				return remove;
			}
		};
	}

	XmlLexer lexer;
	XmlDiffer differ;
	XmlMerger3 merger;

	@Before
	public void before() {
		lexer = new XmlLexer(SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, stringCache);
		differ = xmlDifferFactory.create(SYSTEM_PROFILE_IGNORED_ATTRIBUTES);
		merger = xmlMerger3Factory.create(lexer, differ);
		resetGlobalMaxDistances();
	}

	@Test
	public void case4() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/EmptyDefaultSystemProfileOct2014.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/GEL_Staging.profile.xml"));

		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));

		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(46, diffResult.nAdded);
		Assert.assertEquals(50, diffResult.nRemoved);
		Assert.assertEquals(50, diffResult.nAttributeChanged);

		Assert.assertEquals(46, xmlRight.root().nStructureChanged);
		Assert.assertEquals(50, xmlLeft.root().nStructureChanged);
		Assert.assertEquals(50, xmlRight.root().nAttributeChanged);
		Assert.assertEquals(50, xmlLeft.root().nAttributeChanged);

		Assert.assertEquals(9, xmlLeft.depth);
		Assert.assertEquals(9, xmlRight.depth);

		merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false,  callback(/*add*/ true, /*remove*/ true), stop);

		checkAttributesStorage(xmlLeft);
		checkAttributesStorage(xmlRight);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 1, differ, merger, /*removeElement*/ true);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 1, differ, merger, /*removeElement*/ false);

		diffResult = differ.diff(xmlLeft, xmlRight);
		if (isDifferImplStringMetric(differ)) {
			expectGlobalMaxDistances("XmlMergerSystemProfilesPerformanceTest.case4", 0, 0, 3, /*expectedMetricInvokeCount*/ 0);
		}
	}

	@Test
	public void case5() throws XMLStreamException, IOException {
		case5(lexer, differ);
		if (isDifferImplStringMetric(differ)) {
			expectGlobalMaxDistances("XmlMergerSystemProfilesPerformanceTest.case4", 0, 0, 7, /*expectedMetricInvokeCount*/ 0);
		}
	}

	@Test
	public void case5LevenshteinDefault() throws XMLStreamException, IOException {
		List<String> discriminatorAttributeNames = concatLists(new ArrayList<>(), SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, LEVENSHTEIN_DISCRIMINATOR_ATTRIBUTES);
		XmlLexer lexer = new XmlLexer(discriminatorAttributeNames, stringCache);
		XmlDiffer differ = stringMetricXmlDifferFactory.create(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		case5(lexer, differ);
		if (!isDifferImplShufflingStringMetric(differ)) {
			expectGlobalMaxDistances("XmlMergerSystemProfilesPerformanceTest.case5LevenshteinDefault", 5, 5, 7, /*expectedMetricInvokeCount*/ -1); // always maxmax=5 due to threshold optimization!
		}
	}

	@Test
	public void case5LevenshteinWholeDocument() throws XMLStreamException, IOException {
		List<String> discriminatorAttributeNames = concatLists(new ArrayList<>(), SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, LEVENSHTEIN_DISCRIMINATOR_ATTRIBUTES);
		XmlLexer lexer = new XmlLexer(discriminatorAttributeNames, stringCache);
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = stringMetricXmlDifferFactory.create(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		case5(lexer, differ);
		if (!isDifferImplShufflingStringMetric(differ)) {
			expectGlobalMaxDistances("XmlMergerSystemProfilesPerformanceTest.case5LevenshteinWholeDocument", 5, 5, 7, /*expectedMetricInvokeCount*/ -1); // always maxmax=5 due to try-null-metric optimization!
		}
	}

	private void case5(XmlLexer lexer, XmlDiffer differ) throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/dynaTrace Monitoring.20150105100033.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/dynaTrace Monitoring.20150105103732.profile.xml"));

		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));

		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(9, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		if (!isDifferImplShufflingStringMetric(differ)) {
			Assert.assertEquals(22, diffResult.nAttributeChanged);
		}
		Assert.assertEquals(10, xmlLeft.depth);
		Assert.assertEquals(10, xmlRight.depth);

		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true), stop);
		checkAttributesStorage(xmlLeft);
		checkAttributesStorage(xmlRight);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 1, differ, merger, /*removeElement*/ true);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 1, differ, merger, /*removeElement*/ false);
	}

	@Test
	public void case5DifferentRoots() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/dynaTrace Monitoring.20150105100033.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/dynaTrace Monitoring.20150105103732.profile.xml"));

		/*
		 * let's manipulate a root element to have different roots
		 */

		assert inputRight[4].startsWith("<dynatrace");
		assert inputRight[44313].startsWith("</dynatrace");

		inputRight[4] = inputRight[4].replace("<dynatrace", "<DIFFERENTROOT");
		inputRight[44313] = inputRight[44313].replace("</dynatrace", "</DIFFERENTROOT");

		assert inputRight[4].startsWith("<DIFFERENTROOT");
		assert inputRight[44313].startsWith("</DIFFERENTROOT");

		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));

		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		Assert.assertEquals(10, xmlLeft.depth);
		Assert.assertEquals(10, xmlRight.depth);

		try {
			merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true), stop);
			Assert.fail("Expecting IllegalStateException");
		} catch (IllegalStateException e) {
			System.out.println(e); // OK
		}
		checkAttributesStorage(xmlLeft);
		checkAttributesStorage(xmlRight);
	}
}
